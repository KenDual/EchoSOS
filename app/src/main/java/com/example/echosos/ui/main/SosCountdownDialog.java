package com.example.echosos.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.echosos.R;

public class SosCountdownDialog extends DialogFragment {

    public interface Listener {
        void onCountdownFinished();
        void onCancelled();
    }

    private Listener listener;
    private CountDownTimer timer;
    private int seconds = 5; // 3-5 giây tùy chỉnh sau

    public SosCountdownDialog setListener(Listener l) {
        this.listener = l; return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_sos_countdown, null, false);
        TextView tv = v.findViewById(R.id.tvCountdown);
        v.findViewById(R.id.btnCancel).setOnClickListener(b ->
                new PinVerifyDialog().setListener(ok -> {
                    if (ok) {
                        if (timer != null) timer.cancel();
                        if (listener != null) listener.onCancelled();
                        dismissAllowingStateLoss();
                    }
                }).show(getParentFragmentManager(), "pin"));

        tv.setText(getString(R.string.sos_countdown, seconds));
        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            int s = seconds;
            @Override public void onTick(long m) {
                s--; tv.setText(getString(R.string.sos_countdown, s));
            }
            @Override public void onFinish() {
                if (listener != null) listener.onCountdownFinished();
                dismissAllowingStateLoss();
            }
        }.start();

        return new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.sos)
                .setView(v)
                .setCancelable(false)
                .create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
}
