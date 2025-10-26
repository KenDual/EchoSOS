package com.example.echosos.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.example.echosos.R;
import com.example.echosos.data.local.DatabaseHelper;
import com.example.echosos.utils.Prefs;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        Context c = requireContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(c);

        // 1) Language switcher (VN/EN) — dùng Prefs
        ListPreference lang = new ListPreference(c);
        lang.setKey("pref_lang_code");
        lang.setTitle(getString(R.string.settings_title));
        lang.setEntries(new CharSequence[]{"Tiếng Việt", "English"});
        lang.setEntryValues(new CharSequence[]{"vi", "en"});
        lang.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        lang.setValue(Prefs.getLangCode(c));
        lang.setOnPreferenceChangeListener((p, v) -> {
            Prefs.setLangCode(c, String.valueOf(v));
            Toast.makeText(c, getString(R.string.language_changed_restart), Toast.LENGTH_SHORT).show();
            return true;
        });
        screen.addPreference(lang);

        // 2) SOS message template (tùy biến)
        EditTextPreference sosTpl = new EditTextPreference(c);
        sosTpl.setKey("pref_sos_template");
        sosTpl.setTitle(getString(R.string.sos_template));
        sosTpl.setDialogTitle(getString(R.string.sos_template));
        sosTpl.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        sosTpl.setOnBindEditTextListener(et -> {
            et.setMinLines(3);
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        });
        sosTpl.setText(Prefs.getSosTemplate(c));
        sosTpl.setOnPreferenceChangeListener((p, v) -> {
            Prefs.setSosTemplate(c, String.valueOf(v));
            return true;
        });
        sosTpl.setOnPreferenceClickListener(p -> {
            Toast.makeText(c, "Placeholders: {lat} {lng} {acc} {address} {maps}", Toast.LENGTH_LONG).show();
            return false;
        });
        screen.addPreference(sosTpl);

        // 3) Location update frequency (ms)
        ListPreference locInterval = new ListPreference(c);
        locInterval.setKey("pref_loc_interval");
        locInterval.setTitle(getString(R.string.location_interval));
        locInterval.setEntries(new CharSequence[]{"3s", "5s", "10s", "30s", "60s"});
        locInterval.setEntryValues(new CharSequence[]{"3000", "5000", "10000", "30000", "60000"});
        locInterval.setValue(String.valueOf(Prefs.getLocIntervalMs(c)));
        locInterval.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        locInterval.setOnPreferenceChangeListener((p, v) -> {
            try {
                Prefs.setLocIntervalMs(c, Long.parseLong(String.valueOf(v)));
                return true;
            } catch (Exception e) {
                Toast.makeText(c, "Invalid value", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        screen.addPreference(locInterval);

        // 4) Auto recording toggle
        SwitchPreferenceCompat autoRec = new SwitchPreferenceCompat(c);
        autoRec.setKey("pref_auto_record");
        autoRec.setTitle(getString(R.string.auto_record));
        autoRec.setChecked(Prefs.isAutoRecordEnabled(c));
        autoRec.setOnPreferenceChangeListener((p, v) -> {
            Prefs.setAutoRecordEnabled(c, (Boolean) v);
            return true;
        });
        screen.addPreference(autoRec);

        // 5) Clear data (thực thi xóa DB + Prefs + restart)
        Preference clear = new Preference(c);
        clear.setKey("pref_clear_data");
        clear.setTitle(getString(R.string.clear_data));
        clear.setSummary(getString(R.string.are_you_sure_clear));
        clear.setOnPreferenceClickListener(p -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.clear_data)
                    .setMessage(R.string.are_you_sure_clear)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.clear_data, (d, w) -> performClearData())
                    .show();
            return true;
        });
        screen.addPreference(clear);

        // 6) About
        Preference about = new Preference(c);
        about.setTitle(getString(R.string.about_title));
        about.setOnPreferenceClickListener(p -> {
            startActivity(new Intent(c, AboutActivity.class));
            return true;
        });
        screen.addPreference(about);

        setPreferenceScreen(screen);
    }

    // ===== Helpers =====
    private void performClearData() {
        try {
            // 1) Xoá dữ liệu tất cả bảng app-defined
            android.database.sqlite.SQLiteDatabase db =
                    new DatabaseHelper(requireContext()).getWritableDatabase();
            db.beginTransaction();
            try (android.database.Cursor c = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' " +
                            "AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'", null)) {
                while (c.moveToNext()) {
                    String table = c.getString(0);
                    db.execSQL("DELETE FROM " + table);
                }
            }
            // reset autoincrement
            db.execSQL("DELETE FROM sqlite_sequence");
            db.setTransactionSuccessful();
            db.endTransaction();

            // 2) Xoá SharedPreferences
            Prefs.clearAll(requireContext());

            // 3) Restart về Splash
            Intent i = new Intent(requireContext(), SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            Toast.makeText(requireContext(), getString(R.string.cleared), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Clear data failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
