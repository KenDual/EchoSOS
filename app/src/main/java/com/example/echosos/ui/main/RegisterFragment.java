package com.example.echosos.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.data.dao.UserDao;
import com.example.echosos.data.model.User;
import com.example.echosos.utils.Prefs;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {
    private TextInputEditText etName, etPhone, etEmail, etAddress, etPin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_register, c, false);
        etName = v.findViewById(R.id.etName);
        etPhone = v.findViewById(R.id.etPhone);
        etEmail = v.findViewById(R.id.etEmail);
        etAddress = v.findViewById(R.id.etAddress);
        etPin = v.findViewById(R.id.etPin);
        v.findViewById(R.id.btnSave).setOnClickListener(x -> save());
        return v;
    }

    private void save() {
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
        if (!TextUtils.isEmpty(pin) && pin.length() < 4) {
            msg("PIN ≥ 4 digits");
            return;
        }

        User u = new User();
        u.setName(name);
        u.setPhone(phone);
        u.setEmail(email);
        u.setAddress(addr);
        u.setPinCode(pin);
        long id = new UserDao(requireContext()).insert(u);
        Prefs.setUserId(requireContext(), id);
        msg(getString(R.string.saved));
        // đến Profile
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ProfileFragment()).commit();
    }

    private static String s(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void msg(String m) {
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show();
    }
}