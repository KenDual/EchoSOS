package com.example.echosos.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.echosos.R;
import com.example.echosos.data.dao.EmergencyContactDao;
import com.example.echosos.data.dao.SosHistoryDao;
import com.example.echosos.data.dao.UserDao;
import com.example.echosos.data.model.EmergencyContact;
import com.example.echosos.data.model.SosEvent;
import com.example.echosos.services.location.LocationService;
import com.example.echosos.services.recording.RecorderService;
import com.example.echosos.utils.LocationFetcher;
import com.example.echosos.utils.Permissions;
import com.example.echosos.utils.Prefs;
import com.example.echosos.utils.SmsSender;
import com.example.echosos.utils.SosMessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int REQ_SOS = 1001;

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
        if (req == REQ_SOS && Permissions.hasAll(requireContext(), mergeSosPerms())) {
            startSosCountdown();
        }
    }

    private void startSosCountdown() {
        // 0) Bắt buộc có user hợp lệ để tránh vỡ FK
        long userId = Prefs.getUserId(requireContext());
        if (userId <= 0 || new UserDao(requireContext()).findById(userId) == null) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cần đăng ký tài khoản")
                    .setMessage("Hãy tạo hồ sơ trước khi dùng SOS.")
                    .setPositiveButton("Đăng ký", (d, w) -> {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new RegisterFragment())
                                .addToBackStack(null).commit();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }

        // 1) Quyền: LOCATION + SMS + RECORD_AUDIO
        String[] need = mergeSosPerms();
        if (!Permissions.hasAll(requireContext(), need)) {
            requestPermissions(need, REQ_SOS);
            return;
        }

        // 2) Hiện countdown
        final TextView tv = new TextView(requireContext());
        tv.setPadding(32, 32, 32, 32);
        tv.setText(getString(R.string.sos_countdown, 5));

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sos)
                .setView(tv)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();
        dlg.show();

        Vibrator vib = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        new CountDownTimer(5000, 1000) {
            int sec = 5;

            @Override
            public void onTick(long ms) {
                sec--;
                tv.setText(getString(R.string.sos_countdown, sec));
                if (vib != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //noinspection deprecation
                        vib.vibrate(50);
                    }
                }
                if (!dlg.isShowing()) cancel();
            }

            @Override
            public void onFinish() {
                if (!dlg.isShowing()) return;
                dlg.dismiss();

                // 3) Lấy vị trí 1 lần
                LocationFetcher.getCurrentFix(requireContext(), fix -> {
                    // 4) Build nội dung SOS
                    String msg = SosMessageBuilder.build(requireContext(), fix);

                    // 5) Lấy danh bạ khẩn cấp
                    EmergencyContactDao cDao = new EmergencyContactDao(requireContext());
                    List<String> phones = new ArrayList<>();
                    for (EmergencyContact c : cDao.getByUser(userId)) phones.add(c.getPhone());

                    // 6) Gửi SMS
                    SmsSender.sendBulk(requireContext(), phones, msg, new SmsSender.Callback() {
                        boolean allOk = true;

                        @Override public void onEachResult(String phone, boolean ok) { allOk &= ok; }

                        @Override public void onAllDone() {
                            // 7) Lưu lịch sử SOS (bọc try để không văng app)
                            long eventId = -1;
                            try {
                                SosEvent e = new SosEvent();
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
                                eventId = new SosHistoryDao(requireContext()).insert(e);
                            } catch (SQLiteConstraintException ex) {
                                android.util.Log.e("SOS", "FK failed userId=" + userId, ex);
                                android.widget.Toast.makeText(requireContext(),
                                        "Không thể lưu lịch sử SOS (user không hợp lệ)", android.widget.Toast.LENGTH_LONG).show();
                                return;
                            }

                            // 8) Start services an toàn cho mọi API
                            Context app = requireContext().getApplicationContext();

                            Intent loc = new Intent(app, LocationService.class);
                            ContextCompat.startForegroundService(app, loc);

                            Intent rec = new Intent(app, RecorderService.class)
                                    .putExtra(RecorderService.EXTRA_USER_ID, userId)
                                    .putExtra(RecorderService.EXTRA_EVENT_ID, eventId);
                            ContextCompat.startForegroundService(app, rec);

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

    private String[] mergeSosPerms() {
        return Permissions.merge(
                Permissions.LOCATION,
                Permissions.sms(),
                new String[]{ Manifest.permission.RECORD_AUDIO }
        );
    }
}
