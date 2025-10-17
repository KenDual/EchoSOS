package com.example.echosos.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.data.dao.UserDao;
import com.example.echosos.data.model.User;
import com.example.echosos.utils.Prefs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {
    private UserDao dao;
    private User user;
    private TextView tvName, tvPhone, tvEmail, tvAddress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_profile, c, false);
        tvName = v.findViewById(R.id.tvName);
        tvPhone = v.findViewById(R.id.tvPhone);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvAddress = v.findViewById(R.id.tvAddress);
        v.findViewById(R.id.btnEdit).setOnClickListener(x -> editDialog());
        dao = new UserDao(requireContext());
        long uid = Prefs.getUserId(requireContext());
        if (uid < 0) {
            // Chưa đăng ký → chuyển sang Register
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RegisterFragment()).commit();
        } else {
            user = dao.findById(uid);
            bind();
        }
        return v;
    }

    private void bind() {
        if (user == null) return;
        tvName.setText(user.getName());
        tvPhone.setText(user.getPhone());
        tvEmail.setText(TextUtils.isEmpty(user.getEmail()) ? "-" : user.getEmail());
        tvAddress.setText(TextUtils.isEmpty(user.getAddress()) ? "-" : user.getAddress());
    }

    private void editDialog() {
        View form = getLayoutInflater().inflate(R.layout.fragment_register, null, false);
        TextInputEditText etName = form.findViewById(R.id.etName);
        TextInputEditText etPhone = form.findViewById(R.id.etPhone);
        TextInputEditText etEmail = form.findViewById(R.id.etEmail);
        TextInputEditText etAddress = form.findViewById(R.id.etAddress);
        TextInputEditText etPin = form.findViewById(R.id.etPin);
        etName.setText(user.getName());
        etPhone.setText(user.getPhone());
        etEmail.setText(user.getEmail());
        etAddress.setText(user.getAddress());
        etPin.setText(user.getPinCode());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_profile)
                .setView(form)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = s(etName), phone = s(etPhone), email = s(etEmail), addr = s(etAddress), pin = s(etPin);
                    if (TextUtils.isEmpty(name)) {
                        msg("Name required");
                        return;
                    }
                    if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches()) {
                        msg("Invalid phone");
                        return;
                    }
                    if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        msg("Invalid email");
                        return;
                    }
                    user.setName(name);
                    user.setPhone(phone);
                    user.setEmail(email);
                    user.setAddress(addr);
                    user.setPinCode(pin);
                    dao.update(user);
                    bind();
                    msg(getString(R.string.saved));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static String s(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void msg(String m) {
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show();
    }
}