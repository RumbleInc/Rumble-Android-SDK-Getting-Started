package com.rumble.sample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumble.R;
import com.rumble.sdk.analytics.common.AppSocialNetworks;
import com.rumble.sdk.analytics.events.AnalyticTrackingEvent;
import com.rumble.sdk.analytics.events.EventConstants;
import com.rumble.sdk.analytics.service.AnalyticsService;
import com.rumble.sdk.analytics.service.ContentBI;
import com.rumble.sdk.core.common.Constants;
import com.rumble.sdk.core.common.RumbleEventBus;
import com.rumble.sdk.core.common.RumbleLogger;
import com.rumble.sdk.core.common.Utils;
import com.rumble.sdk.core.interfaces.IAdRequest;
import com.rumble.sdk.core.interfaces.IArticle;
import com.rumble.sdk.core.messages.BaseMessage;
import com.rumble.sdk.core.messages.LogMessage;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    private ArrayAdapter<Spannable> mConsoleAdapter;
    ListView mConsole;
    private static ArrayList<Spannable> mConsoleMessagesList;
    private EditText mJsonEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RumbleLogger.v(Constants.TAG, "Main activity View Created");
        setContentView(R.layout.activity_main);

        mConsole = (ListView) findViewById(R.id.consoleList);
        mJsonEditText = (EditText) findViewById(R.id.txtEventJson);

        RumbleEventBus.registerEventBus(this);

        initToolbar();
        initConsoleList();
        setSampleJsonEventString();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.notification_bar_color));
        }
    }

    private void setSampleJsonEventString() {


        AnalyticTrackingEvent event = getEventUsingBuilder();
        mJsonEditText.setText(Utils.prettifyJson(event));
//        try {
//            mJsonEditText.setText(mapper.writerWithDefaultPrettyPrinter().
//                    writeValueAsString(event));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
    }


/**************************************************************************/
/******************************* API demonstration ************************/
/**************************************************************************/
    public void sendOpenSession(View v) {
//        AnalyticsService.sendOpenSession();

        MyAdRequest adRequest = new MyAdRequest();
        MyArticle article = new MyArticle();
        AnalyticTrackingEvent event = null;

        Utils.showAlertDialog(this, "Event Json", Utils.prettifyJson(event), null);
    }

    public void sendCloseSession(View v) {
        AnalyticsService.sendCloseSession();
    }

    public void sendEventUsingJson(View v) {

        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AnalyticTrackingEvent event = null;
        try {
            event = mapper.readValue(mJsonEditText.getText().toString(), AnalyticTrackingEvent.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (event == null || StringUtils.isBlank(event.Name)) {
            RumbleEventBus.postEvent(new LogMessage("Event json format is invalid", LogMessage.LogType.enError));
            return;
        }
        AnalyticsService.sendEvent(event);
    }

    public void sendEventUsingBuilderPattern() {
        ObjectMapper mapper = new ObjectMapper();
        AnalyticTrackingEvent event = getEventUsingBuilder();
        AnalyticsService.sendEvent(event);
    }


    private AnalyticTrackingEvent getEventUsingBuilder() {
        return new AnalyticTrackingEvent.Builder().
                name("MyEvent").
                attribute("Attr1", "Value1").
                attribute("Attr2", "Value2").
                build();
    }

    public void toggleOptOut(View v) {
        AnalyticsService.setOptOut(!AnalyticsService.isOptOut());
    }

    public void contentBIExamples() {

        MyAdRequest adRequest = new MyAdRequest();
        MyArticle article = new MyArticle();
        AnalyticTrackingEvent event = null;

        event = ContentBI.sendAdClickedEvent(adRequest, article);
        event = ContentBI.sendPageViewEvent("Label 1");
        event = ContentBI.sendArticleBookmarkEvent(article);
        event = ContentBI.sendArticleLikeEvent(article, AppSocialNetworks.Facebook);
        event = ContentBI.sendArticleReadEvent(article, 5000);
        event = ContentBI.sendArticleShareEvent(article, AppSocialNetworks.Twitter);
        event = ContentBI.sendCommentsOpenedEvent(article, AppSocialNetworks.Facebook);
        event = ContentBI.sendVideoStartedEvent(article.getArticleId(), "http://video.com/lions123");
        event = ContentBI.sendSyndicationLinkClickEvent("http://syndication.com/link1", "Source 1");
        event = ContentBI.sendVideoADStartedEvent(article.getArticleId(),"http://video.com/lions123");

        ContentBI.addAggregatedAdEvent(EventConstants.ATTR_AD_FAILED,adRequest,article);
        ContentBI.addAggregatedAdEvent(EventConstants.ATTR_AD_FAILED,adRequest,article);
        ContentBI.addAggregatedAdEvent(EventConstants.ATTR_AD_FILLED,adRequest,article);
        ContentBI.addAggregatedAdEvent(EventConstants.ATTR_AD_REQUESTED,adRequest,article);
        ContentBI.addAggregatedAdEvent(EventConstants.ATTR_AD_REQUESTED,adRequest,article);
        ContentBI.addAggregatedAdEvent(EventConstants.ATTR_AD_TIMEOUT,adRequest,article);
        event = ContentBI.sendAggregatedAdEvent();

        //use this to present the output json in a dialog
        Utils.showAlertDialog(this, "Event Json", Utils.prettifyJson(event), null);
    }

/**************************************************************************/
/******************************* End of API demonstration *****************/
/**************************************************************************/

    public void onClearClicked(View v) {
        mConsoleAdapter.clear();
        mConsoleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        RumbleLogger.v(Constants.TAG, "Main activity started");

        AnalyticsService.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {

        AnalyticsService.stop();
        super.onStop();
        RumbleLogger.v(Constants.TAG, "Main activity Stopped");
        RumbleEventBus.unregisterEventBus(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        RumbleLogger.v(Constants.TAG, "Main activity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        RumbleLogger.v(Constants.TAG, "Main activity resumed");
        RumbleEventBus.registerEventBus(this);
        initConsoleList();
    }

    /***
     * eventBus listener captures events coming from SDK internals.
     * in this case, messages are displayed on the sample app console list
     * @param event
     */
    public void onEventMainThread(BaseMessage event) {
        mConsoleMessagesList.add(0, event.getText());
        mConsoleAdapter.notifyDataSetChanged();
    }


    private void initConsoleList() {
        if (mConsoleMessagesList == null) {
            mConsoleMessagesList = new ArrayList<Spannable>();

            //retrieve all messages that were accumulated since application
            // start until this activity is initiated
            mConsoleMessagesList.addAll(MyApplication.getEventBusMessages());
        }

        mConsoleAdapter = new ListAdapter(this, R.layout.item_console, mConsoleMessagesList);

        mConsole.setAdapter(mConsoleAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);

        toolbar.setTitle("Rumble SDK");
        toolbar.setSubtitle("Let us show you how it's done...");

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }
                return true;
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_launcher);

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_main);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RumbleLogger.v(Constants.TAG, "Main activity destroyed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyArticle implements IArticle {

        @Override
        public String getChannelId() {
            return "Channel 1";
        }

        @Override
        public String getArticleId() {
            return "6403";
        }

        @Override
        public boolean getIsPremium() {
            return true;
        }

        @Override
        public boolean getIsBookmarked() {
            return true;
        }

        @Override
        public boolean getIsFromBookmarkedSection() {
            return false;
        }

        @Override
        public boolean getIsBreakingNews() {
            return false;
        }

        @Override
        public String getArticleTitle() {
            return "Article title 1";
        }

        @Override
        public String getAuthorName() {
            return "Scott Roch";
        }

        @Override
        public String getChannelName() {
            return "Top News";
        }

        @Override
        public String getUrl() {
            return "http://bangordailynews.com/2015/07/11/news/mid-maine/vassalboro-crash-kills-two-during-police-chase/";
        }

        @Override
        public int getPosition() {
            return 4;
        }
    }

    class MyAdRequest implements IAdRequest
    {

        @Override
        public String getAdNetwork() {
            return "Dfp";
        }

        @Override
        public String getInvId() {
            return "202";
        }

        @Override
        public String getDeviceId() {
            return "12";
        }

        @Override
        public String getAdUnitId() {
            return "39151";
        }

        @Override
        public String getAdLocation() {
            return "Bottom";
        }

        @Override
        public String getSize() {
            return "320,50";
        }

        @Override
        public String getShowAt() {
            return null;
        }

        @Override
        public String getFrequency() {
            return null;
        }

        @Override
        public String getAdTag() {
            return "ca-app-pub-8641107121157775";
        }

        @Override
        public Map<String, Object> getTargetingParams() {
            return null;
        }
    }
}
