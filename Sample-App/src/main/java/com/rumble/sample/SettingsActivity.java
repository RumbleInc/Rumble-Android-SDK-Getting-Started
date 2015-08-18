package com.rumble.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.rumble.R;
import com.rumble.sdk.core.common.Utils;
import com.rumble.sdk.core.service.SDKManagerService;

public class SettingsActivity extends PreferenceActivity {
    private String mApp_id;
    private boolean mIs_production;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mApp_id = preferences.getString("pref_app_id", "still not set !");
        mIs_production = preferences.getBoolean("pref_key_is_production", false);
    }

    @Override
    public void onBackPressed() {


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String app_id = preferences.getString("pref_app_id", "still not set !");
        boolean is_production = preferences.getBoolean("pref_key_is_production", false);
        if (mApp_id != app_id || mIs_production != is_production) {
            DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SDKManagerService.clearCache();
                    System.exit(0);
                }
            };
            showAlertDialog(this, "Important", "Critical Preferences have changed\n" +
                    "App will close now for changes to take effect", dialogListener);
        }
        else
        {
            finish();
        }
    }

    public void showAlertDialog(Context context, String title, String body,
                                DialogInterface.OnClickListener listener) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(context);
        dlgAlert.setMessage(body);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", listener);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }
}