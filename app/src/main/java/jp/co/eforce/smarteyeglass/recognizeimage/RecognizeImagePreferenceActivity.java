package jp.co.eforce.smarteyeglass.recognizeimage;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by cho on 2016/10/13.
 */

public final class RecognizeImagePreferenceActivity extends PreferenceActivity {

    /** The ID for the Read Me dialog. */
    private static final int DIALOG_READ_ME = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        // Handles Read Me.
        Preference pref =
                findPreference(getText(R.string.preference_key_read_me));
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference pref) {
                showDialog(DIALOG_READ_ME);
                return true;
            }
        });

    }
}