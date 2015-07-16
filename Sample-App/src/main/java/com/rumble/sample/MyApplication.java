package com.rumble.sample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Spannable;

import com.rumble.sdk.analytics.service.AnalyticsService;
import com.rumble.sdk.core.common.RumbleEventBus;
import com.rumble.sdk.core.messages.BaseMessage;

import java.util.ArrayList;

public class MyApplication extends Application {


    static MyApplication mInstance;

    /***
     * Accumulates all messages since application start untill this method is first
     * called, then it unregister it self from eventBus
     * @return
     */
    public static ArrayList<Spannable> getEventBusMessages() {
        RumbleEventBus.unregisterEventBus(mInstance);
        return mEventBusMessages;
    }

    public static ArrayList<Spannable> mEventBusMessages = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        RumbleEventBus.registerEventBus(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String appID = preferences.getString("pref_app_id", "still not set !");
        boolean isProduction = preferences.getBoolean("pref_key_is_production",false);

        AnalyticsService.integrate(this, appID, isProduction);
    }

    /***
     * Accumulate all event bus messages since AnalyticsService.integrate() call.
     * Built for this sample app only and is NOT best practice.
     * Reason: hold all messages in the meantime while MainActivity is loading. Once
     * loaded MainActivity retrieve the list and present in its console
     * @param event
     */
    public void onEvent(BaseMessage event) {
        mEventBusMessages.add(0, event.getText());
    }

}
