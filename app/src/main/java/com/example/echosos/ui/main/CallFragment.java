package com.example.echosos.ui.main;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.data.dao.CallHistoryDao;
import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.data.model.CallLog;
import com.example.echosos.utils.Permissions;
import com.example.echosos.utils.Prefs;
import com.google.android.material.button.MaterialButton;

public class CallFragment extends Fragment {

    private static final int REQ_CALL = 2001;
    private String pendingPhone;
    private String pendingLabel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_call, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        bindCall(v, R.id.btnPolice, "Police", "112");
        bindCall(v, R.id.btnAmbulance, "Ambulance", "115");
        bindCall(v, R.id.btnFamily, "Family", "0768793319");
    }

    private void bindCall(View root, int btnId, String label, String phone) {
        MaterialButton btn = root.findViewById(btnId);
        btn.setOnClickListener(view -> tryCall(label, phone));
    }

    private void tryCall(String label, String phone) {
        if (!Permissions.hasAll(requireContext(), new String[]{Manifest.permission.CALL_PHONE})) {
            pendingLabel = label; pendingPhone = phone;
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
            return;
        }
        startCall(label, phone);
    }

    private void startCall(String label, String phone) {
        try {
            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            startActivity(i);
            logCall(label, phone, 0); // duration chưa biết -> 0
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.sos_partial_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void logCall(String label, String phone, int durationSec) {
        CallHistoryDao dao = new CallHistoryDao(new DatabaseHelper(requireContext()).getWritableDatabase());
        CallLog log = new CallLog();
        log.setUserId(Prefs.getUserId(requireContext()));
        log.setPhone(phone);
        log.setLabel(label);
        log.setDurationSec(durationSec);
        log.setCreatedAt(System.currentTimeMillis());
        dao.insert(log);
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == REQ_CALL && Permissions.hasAll(requireContext(), new String[]{Manifest.permission.CALL_PHONE})) {
            if (pendingPhone != null) startCall(pendingLabel, pendingPhone);
            pendingLabel = pendingPhone = null;
        }
    }
}
