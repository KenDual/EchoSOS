package com.example.echosos.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.echosos.R;
import com.example.echosos.utils.Prefs;

public class PinVerifyDialog extends DialogFragment {

    public interface Listener {
        void onResult(boolean ok);
    }

    private Listener listener;

    public PinVerifyDialog setListener(Listener l) {
        this.listener = l;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_pin_verify, null, false);
        EditText et = v.findViewById(R.id.etPin);

        return new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.pin_code)
                .setView(v)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    String current = Prefs.getPin(requireContext());
                    boolean ok = current == null || current.isEmpty() || current.equals(et.getText().toString());
                    if (listener != null) listener.onResult(ok);
                })
                .setNegativeButton(R.string.cancel, (d, w) -> {
                    if (listener != null) listener.onResult(false);
                })
                .create();
    }
}
