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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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

    private View emptyView;   // from view_empty_state.xml
    private View loading;     // from view_loading.xml

    // Request READ_CONTACTS then open picker
    private final ActivityResultLauncher<String[]> permReq =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    res -> pickFromPhone());

    // Contact picker
    private final ActivityResultLauncher<Intent> pickContact =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getData() == null) return;
                Uri uri = r.getData().getData();
                if (uri == null) return;

                String name = null, phone = null, contactId = null;

                // Query minimal projection
                String[] contactProj = {
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME
                };
                try (Cursor c = requireContext().getContentResolver()
                        .query(uri, contactProj, null, null, null)) {
                    if (c != null && c.moveToFirst()) {
                        int idIdx = c.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
                        int nameIdx = c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
                        contactId = c.getString(idIdx);
                        name = c.getString(nameIdx);
                    }
                } catch (Exception e) {
                    toast(getString(R.string.sos_partial_fail));
                }

                if (!TextUtils.isEmpty(contactId)) {
                    String[] phoneProj = { ContactsContract.CommonDataKinds.Phone.NUMBER };
                    try (Cursor p = requireContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProj,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{ contactId }, null)) {
                        if (p != null && p.moveToFirst()) {
                            int phoneIdx = p.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            phone = p.getString(phoneIdx);
                        }
                    } catch (Exception e) {
                        toast(getString(R.string.invalid_phone));
                    }
                }

                showAddOrEditDialog(null, name, phone);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_contacts, c, false);

        RecyclerView rv = v.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new Adapter();
        rv.setAdapter(adapter);

        emptyView = v.findViewById(R.id.emptyState);
        loading   = v.findViewById(R.id.loadingOverlay);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos >= 0 && pos < data.size()) {
                    EmergencyContact e = data.get(pos);
                    dao.delete(e.getId());
                    reload(); // sẽ tự update empty
                    toast(getString(R.string.delete));
                }
            }
        }).attachToRecyclerView(rv);

        FloatingActionButton fab = v.findViewById(R.id.fabAdd);
        fab.setOnClickListener(x -> chooseAddMethod());

        dao = new EmergencyContactDao(requireContext());
        userId = Prefs.getUserId(requireContext());

        reload();
        return v;
    }

    private void reload() {
        setLoading(true);
        data.clear();
        if (userId > 0) data.addAll(dao.getByUser(userId)); // sorted: primary first
        if (adapter != null) adapter.notifyDataSetChanged();
        updateEmpty();
        setLoading(false);
    }

    private void chooseAddMethod() {
        String[] items = { getString(R.string.pick_from_phone), getString(R.string.add_manually) };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_contact)
                .setItems(items, (d, i) -> {
                    if (i == 0) {
                        if (!hasContactsPerm()) permReq.launch(Permissions.contacts());
                        else pickFromPhone();
                    } else {
                        showAddOrEditDialog(null, null, null);
                    }
                }).show();
    }

    private boolean hasContactsPerm() {
        return Permissions.hasAll(requireContext(), new String[]{ Manifest.permission.READ_CONTACTS });
    }

    private void pickFromPhone() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContact.launch(i);
    }

    /** Add (e == null) or Edit (e != null) */
    private void showAddOrEditDialog(@Nullable EmergencyContact e,
                                     @Nullable String namePre,
                                     @Nullable String phonePre) {
        View form = getLayoutInflater().inflate(R.layout.dialog_add_contact, null, false);
        TextInputEditText etName = form.findViewById(R.id.etName);
        TextInputEditText etPhone = form.findViewById(R.id.etPhone);
        TextInputEditText etRelation = form.findViewById(R.id.etRelation);
        Slider slider = form.findViewById(R.id.sliderPriority);

        if (e != null) {
            etName.setText(e.getName());
            etPhone.setText(e.getPhone());
            etRelation.setText(e.getRelation());
            slider.setValue(e.getPriority());
        } else {
            if (!TextUtils.isEmpty(namePre)) etName.setText(namePre);
            if (!TextUtils.isEmpty(phonePre)) etPhone.setText(phonePre);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(e == null ? R.string.add_contact : R.string.edit_contact)
                .setView(form)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = text(etName);
                    String phone = normalizePhone(text(etPhone));
                    String relation = text(etRelation);

                    if (TextUtils.isEmpty(name)) { toast(getString(R.string.required_name)); return; }
                    if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches()) { toast(getString(R.string.invalid_phone)); return; }

                    int prio = (int) slider.getValue();
                    if (e == null) {
                        EmergencyContact ec = new EmergencyContact(userId, name, phone, prio);
                        ec.setRelation(relation);
                        long newId = dao.insert(ec);
                        if (prio == 1) dao.setPrimary(userId, newId);
                    } else {
                        e.setName(name);
                        e.setPhone(phone);
                        e.setRelation(relation);
                        e.setPriority(prio);
                        dao.update(e);
                        if (prio == 1) dao.setPrimary(userId, e.getId());
                    }
                    reload();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private static String normalizePhone(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^0-9+]", ""); // keep digits and +
    }

    private void toast(String m) {
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean show) {
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmpty() {
        if (emptyView != null) emptyView.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ----------------- RecyclerView Adapter -----------------
    private class Adapter extends RecyclerView.Adapter<VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_contact, p, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int i) {
            EmergencyContact e = data.get(i);
            h.tvName.setText(e.getName());
            h.tvPhone.setText(e.getPhone());

            String meta = (e.getPriority() == 1
                    ? h.itemView.getContext().getString(R.string.primary)
                    : h.itemView.getContext().getString(R.string.secondary));
            if (!TextUtils.isEmpty(e.getRelation())) meta += " • " + e.getRelation();
            h.tvMeta.setText(meta);

            // Click to Edit
            h.itemView.setOnClickListener(v -> showAddOrEditDialog(e, null, null));

            // Long-press to set Primary
            h.itemView.setOnLongClickListener(v -> {
                dao.setPrimary(userId, e.getId());
                reload();
                toast(getString(R.string.primary));
                return true;
            });
        }

        @Override
        public int getItemCount() { return data.size(); }
    }

    private static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvPhone, tvMeta;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvMeta  = v.findViewById(R.id.tvMeta);
        }
    }
}
