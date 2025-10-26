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
import com.example.echosos.utils.NetworkUtils;
import com.example.echosos.utils.Permissions;
import com.example.echosos.utils.Prefs;
import com.example.echosos.utils.SmsSender;
import com.example.echosos.utils.SosMessageBuilder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int REQ_SOS = 1001;

    private View loading; // overlay từ view_loading.xml

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        // Loading overlay
        loading = v.findViewById(R.id.loadingOverlay);

        // Nút SOS
        Button btn = v.findViewById(R.id.btnSos);
        btn.setOnClickListener(view -> precheckAndStart());

        // Switch Safe Mode
        MaterialSwitch sw = v.findViewById(R.id.switch_safe_mode);
        if (sw != null) {
            sw.setChecked(Prefs.isSafeMode(requireContext()));
            sw.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    Prefs.setSafeMode(requireContext(), true);
                    // Tắt service nhạy cảm ngay lập tức
                    requireContext().stopService(new Intent(requireContext(), LocationService.class));
                    requireContext().stopService(new Intent(requireContext(), RecorderService.class));
                    android.widget.Toast.makeText(requireContext(), getString(R.string.safe_mode_on), android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    Prefs.setSafeMode(requireContext(), false);
                    android.widget.Toast.makeText(requireContext(), getString(R.string.safe_mode_off), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == REQ_SOS && Permissions.hasAll(requireContext(), mergeSosPerms())) {
            precheckAndStart();
        }
    }

    // Tiền kiểm: Safe Mode + Quyền + Mạng
    private void precheckAndStart() {
        if (Prefs.isSafeMode(requireContext())) {
            android.widget.Toast.makeText(requireContext(), getString(R.string.safe_mode_block_sos), android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = Prefs.getUserId(requireContext());
        if (userId <= 0 || new UserDao(requireContext()).findById(userId) == null) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.need_register_first)
                    .setPositiveButton(R.string.register, (d, w) -> {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new RegisterFragment())
                                .addToBackStack(null).commit();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }

        String[] need = mergeSosPerms();
        if (!Permissions.hasAll(requireContext(), need)) {
            requestPermissions(need, REQ_SOS);
            return;
        }

        if (!NetworkUtils.isOnline(requireContext())) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.no_internet)
                    .setMessage(getString(R.string.no_internet))
                    .setPositiveButton(R.string.sos, (d, w) -> startSosCountdown())
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.open_wifi_settings, (d, w) ->
                            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)))
                    .show();
            return;
        }

        startSosCountdown();
    }

    private void startSosCountdown() {
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

            @Override public void onTick(long ms) {
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

            @Override public void onFinish() {
                if (!dlg.isShowing()) return;
                dlg.dismiss();

                // Chặn nếu vừa bật Safe Mode trong lúc đếm
                if (Prefs.isSafeMode(requireContext())) {
                    android.widget.Toast.makeText(requireContext(), getString(R.string.safe_mode_block_sos), android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                final long userId = Prefs.getUserId(requireContext());

                setLoading(true); // bắt đầu loading trước khi lấy vị trí
                LocationFetcher.getCurrentFix(requireContext(), fix -> {
                    String msg = SosMessageBuilder.build(requireContext(), fix);

                    EmergencyContactDao cDao = new EmergencyContactDao(requireContext());
                    List<String> phones = new ArrayList<>();
                    for (EmergencyContact c : cDao.getByUser(userId)) phones.add(c.getPhone());

                    SmsSender.sendBulk(requireContext(), phones, msg, new SmsSender.Callback() {
                        boolean allOk = true;

                        @Override public void onEachResult(String phone, boolean ok) { allOk &= ok; }

                        @Override public void onAllDone() {
                            long eventId;
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
                                setLoading(false);
                                android.widget.Toast.makeText(requireContext(),
                                        getString(R.string.sos_partial_fail), android.widget.Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Nếu bật Safe Mode sau khi gửi SMS -> không start service
                            if (Prefs.isSafeMode(requireContext())) {
                                setLoading(false);
                                android.widget.Toast.makeText(requireContext(), getString(R.string.safe_mode_on), android.widget.Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Context app = requireContext().getApplicationContext();
                            ContextCompat.startForegroundService(app, new Intent(app, LocationService.class));
                            if (Prefs.isAutoRecordEnabled(requireContext())) {
                                ContextCompat.startForegroundService(app,
                                        new Intent(app, RecorderService.class)
                                                .putExtra(RecorderService.EXTRA_USER_ID, userId)
                                                .putExtra(RecorderService.EXTRA_EVENT_ID, eventId));
                            }

                            setLoading(false);
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

    private void setLoading(boolean show) {
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
