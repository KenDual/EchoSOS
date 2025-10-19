package com.example.echosos.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.utils.Permissions;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        Button btn = v.findViewById(R.id.btnSos);
        btn.setOnClickListener(view -> startSosCountdown());
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == 1001 && com.example.echosos.utils.Permissions.hasAll(requireContext(),
                com.example.echosos.utils.Permissions.merge(
                        com.example.echosos.utils.Permissions.LOCATION,
                        com.example.echosos.utils.Permissions.sms()))) {
            startSosCountdown(); // retry sau khi user cấp quyền
        }
    }

    private void startSosCountdown() {
        if (!Permissions.hasAll(requireContext(), Permissions.LOCATION)
                || !Permissions.hasAll(requireContext(), Permissions.sms())) {
            requestPermissions(Permissions.merge(Permissions.LOCATION, Permissions.sms()), 1001);
            return;
        }

        final TextView tv = new TextView(requireContext());
        tv.setPadding(32, 32, 32, 32);
        tv.setText(getString(R.string.sos_countdown, 5));

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sos)
                .setView(tv)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();
        dlg.show();

        Vibrator vib = (Vibrator) requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE);

        new CountDownTimer(5000, 1000) {
            int sec = 5;

            @Override
            public void onTick(long ms) {
                sec--;
                tv.setText(getString(R.string.sos_countdown, sec));
                if (vib != null) vib.vibrate(50);
                if (!dlg.isShowing()) cancel();
            }

            @Override
            public void onFinish() {
                if (!dlg.isShowing()) return;
                dlg.dismiss();

                // 1) Lấy vị trí 1 lần
                com.example.echosos.utils.LocationFetcher.getCurrentFix(requireContext(), fix -> {
                    // 2) Build nội dung SOS
                    String msg = com.example.echosos.utils.SosMessageBuilder.build(requireContext(), fix);

                    // 3) Lấy danh sách số liên hệ
                    com.example.echosos.data.dao.EmergencyContactDao cDao =
                            new com.example.echosos.data.dao.EmergencyContactDao(requireContext());
                    long userId = com.example.echosos.utils.Prefs.getUserId(requireContext());
                    java.util.List<String> phones = new java.util.ArrayList<>();
                    for (com.example.echosos.data.model.EmergencyContact c : cDao.getByUser(userId)) {
                        phones.add(c.getPhone());
                    }

                    // 4) Gửi SMS hàng loạt
                    com.example.echosos.utils.SmsSender.sendBulk(requireContext(), phones, msg,
                            new com.example.echosos.utils.SmsSender.Callback() {
                                boolean allOk = true;

                                @Override
                                public void onEachResult(String phone, boolean ok) {
                                    allOk &= ok;
                                }

                                @Override
                                public void onAllDone() {
                                    // 5) Ghi lịch sử SOS
                                    com.example.echosos.data.model.SosEvent e =
                                            new com.example.echosos.data.model.SosEvent();
                                    e.setUserId(userId);
                                    if (fix != null) {
                                        e.setLat(fix.lat);
                                        e.setLng(fix.lng);
                                        e.setAccuracy(fix.acc);
                                        e.setAddress(fix.address);
                                    }
                                    e.setMessage(msg);
                                    e.setSmsSent(allOk);
                                    e.setCreatedAt(System.currentTimeMillis());

                                    new com.example.echosos.data.dao.SosHistoryDao(requireContext()).insert(e);

                                    android.widget.Toast.makeText(
                                            requireContext(),
                                            allOk ? R.string.saved : R.string.sos_partial_fail,
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                });
            }
        }.start();
    }
}
