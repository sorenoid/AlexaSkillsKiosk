package com.bandor.android.alexaskillskiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class KioskActivity extends AppCompatActivity {

    KioskController controller;

    private final BroadcastReceiver skillsDataLoadedRcvr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            init();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiosk);

        //data model
        SkillsData.load(this);

        //layout controller
        createController();
        controller.connectToLayout();
    }

    @Override
    protected void onResume() {
        //call here in case the broadcast (below) came in before we were registered for it.
       // SkillsData.reload(this);
        if (SkillsData.getLoaded()) {
            init();
        }

        IntentFilter intentFilter = new IntentFilter(SkillsData.BROADCAST_LOAD_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(skillsDataLoadedRcvr, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(skillsDataLoadedRcvr);
        super.onPause();
    }

    private void createController() {
        controller = new KioskController(this);
    }

    private void init() {
        SkillsData.colorize(this);
        controller.invalidate();
    }


}
