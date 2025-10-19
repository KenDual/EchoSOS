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

    private void startSosCountdown() {
        // xin quyền cần thiết (LOCATION + SMS) tối thiểu cho Phase 3
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
            @Override public void onTick(long ms) {
                sec--;
                tv.setText(getString(R.string.sos_countdown, sec));
                if (vib != null) vib.vibrate(50);
                if (!dlg.isShowing()) cancel();
            }
            @Override public void onFinish() {
                if (!dlg.isShowing()) return;
                tv.setText(getString(R.string.sos_sending));
                dlg.dismiss();
                // TODO: gửi SMS đến danh sách liên hệ + ghi lịch sử (Phase 3 tiếp)
            }
        }.start();
    }
}
