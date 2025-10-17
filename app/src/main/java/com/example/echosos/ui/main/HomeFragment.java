package com.example.echosos.ui.main;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        TextView tv = new TextView(requireContext());
        tv.setText("EchoSOS — Home");
        tv.setGravity(Gravity.CENTER);
        return tv;
    }
}