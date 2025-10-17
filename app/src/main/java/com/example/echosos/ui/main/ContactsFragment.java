package com.example.echosos.ui.main;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echosos.R;
import com.example.echosos.data.dao.EmergencyContactDao;
import com.example.echosos.data.model.EmergencyContact;
import com.example.echosos.utils.Permissions;
import com.example.echosos.utils.Prefs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private EmergencyContactDao dao;
    private long userId;
    private final List<EmergencyContact> data = new ArrayList<>();
    private Adapter adapter;

    private final ActivityResultLauncher<String[]> permReq =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), res -> pickFromPhone());
    private final ActivityResultLauncher<Intent> pickContact =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getData() == null) return;
                Uri uri = r.getData().getData();
                if (uri == null) return;
                String name = null, phone = null;

                try (Cursor contactCursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                    if (contactCursor != null && contactCursor.moveToFirst()) {
                        // Lấy chỉ số cột một cách an toàn
                        int nameIndex = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        int idIndex = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);

                        if (nameIndex != -1) {
                            name = contactCursor.getString(nameIndex);
                        }

                        if (idIndex != -1) {
                            String contactId = contactCursor.getString(idIndex);

                            // Bước 2: Dùng ID để truy vấn số điện thoại
                            try (Cursor phoneCursor = requireContext().getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{contactId},
                                    null)) {

                                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                                    int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                    if (phoneIndex != -1) {
                                        phone = phoneCursor.getString(phoneIndex);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ghi lại log lỗi để debug nếu cần
                    // Log.e("ContactsFragment", "Error picking contact", e);
                    toast("Could not read contact data.");
                }
                showAddDialog(name, phone);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_contacts, c, false);
        RecyclerView rv = v.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new Adapter(data);
        rv.setAdapter(adapter);

        FloatingActionButton fab = v.findViewById(R.id.fabAdd);
        fab.setOnClickListener(x -> chooseAddMethod());
        dao = new EmergencyContactDao(requireContext());
        userId = Prefs.getUserId(requireContext());
        reload();
        return v;
    }

    private void reload() {
        data.clear();
        if (userId > 0) data.addAll(dao.getByUser(userId));
        adapter.notifyDataSetChanged();
    }

    private void chooseAddMethod() {
        String[] items = {getString(R.string.pick_from_phone), getString(R.string.add_manually)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_contact)
                .setItems(items, (d, i) -> {
                    if (i == 0) {
                        // picker
                        if (!hasContactsPerm()) permReq.launch(Permissions.contacts());
                        else pickFromPhone();
                    } else {
                        showAddDialog(null, null);
                    }
                }).show();
    }

    private boolean hasContactsPerm() {
        return Permissions.hasAll(requireContext(), new String[]{Manifest.permission.READ_CONTACTS});
    }

    private void pickFromPhone() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContact.launch(i);
    }

    private void showAddDialog(String namePre, String phonePre) {
        View form = getLayoutInflater().inflate(R.layout.dialog_add_contact, null, false);
        TextInputEditText etName = form.findViewById(R.id.etName);
        TextInputEditText etPhone = form.findViewById(R.id.etPhone);
        TextInputEditText etRelation = form.findViewById(R.id.etRelation);
        Slider slider = form.findViewById(R.id.sliderPriority);
        if (!TextUtils.isEmpty(namePre)) etName.setText(namePre);
        if (!TextUtils.isEmpty(phonePre)) etPhone.setText(phonePre);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_contact)
                .setView(form)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = s(etName), phone = s(etPhone), relation = s(etRelation);
                    if (TextUtils.isEmpty(name)) {
                        toast("Name required");
                        return;
                    }
                    if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches()) {
                        toast("Invalid phone");
                        return;
                    }
                    EmergencyContact ec = new EmergencyContact(userId, name, phone, (int) slider.getValue());
                    ec.setRelation(relation);
                    dao.insert(ec);
                    reload();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static String s(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void toast(String m) {
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show();
    }

    // ----------------- RecyclerView Adapter -----------------
    static class Adapter extends RecyclerView.Adapter<VH> {
        private final List<EmergencyContact> data;

        Adapter(List<EmergencyContact> d) {
            data = d;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_contact, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int i) {
            EmergencyContact e = data.get(i);
            h.tvName.setText(e.getName());
            h.tvPhone.setText(e.getPhone());
            String meta = (e.getPriority() == 1 ? "Primary" : "Secondary") +
                    (TextUtils.isEmpty(e.getRelation()) ? "" : " • " + e.getRelation());
            h.tvMeta.setText(meta);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvMeta;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvMeta = v.findViewById(R.id.tvMeta);
        }
    }
}
