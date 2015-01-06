package it.casarsa.powermeter;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by fcasarsa on 06/01/2015.
 */
public class UserSettingActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // add the xml resource
        addPreferencesFromResource(R.xml.user_settings);


    }

}