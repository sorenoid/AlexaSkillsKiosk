package com.bandor.android.alexaskillskiosk;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class KioskActivity extends AppCompatActivity {

    KioskController controller;
    Menu menu;

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

        //set up the searchable "database" of skills
        String[] columns = new String[]{"_id", "text"};
        Object[] temp = new Object[]{0, "default"};


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
        } else {
            showProgressView(getString(R.string.loading_skills));

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                loadPossibleSkills(query);
                return true;
            }

            private void loadPossibleSkills(String query) {
                SkillsData.fillCursor(KioskActivity.this, query, search);
            }

        });

        return true;
    }

    private void createController() {
        controller = new KioskController(this);
    }

    private void init() {
        SkillsData.colorize(this);
        controller.invalidate();
        hideProgressView();
    }

    /**
     * Show translucent full-screen layout (to eat any touch events) with an indeterminate progress "bar" and optional message
     *
     * @param message optional message to show underneath the spinner
     */
    public void showProgressView(String message) {
        Log.w("VST-GAa", "showing progress view");
        ViewGroup progressLayout = (ViewGroup) findViewById(R.id.progress_layout);
        if (null != progressLayout) {
            progressLayout.setVisibility(View.VISIBLE);

            // Message
            TextView messageTv = (TextView) progressLayout.findViewById(R.id.progress_message);
            messageTv.setText((null != message) ? message : "");

            // Set a click listener to eat all the touch events
            progressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                }
            });
        }
    }

    /**
     * Hide the progress bar shown above
     */
    public void hideProgressView() {
        ViewGroup progressLayout = (ViewGroup) findViewById(R.id.progress_layout);
        if (null != progressLayout) {
            progressLayout.setVisibility(View.GONE);
        }
    }


    /**
     * See if the progress "bar" is up
     *
     * @return true if progress view is showing
     */
    public boolean isProgressShowing() {
        boolean showing = false;
        ViewGroup progressLayout = (ViewGroup) findViewById(R.id.progress_layout);
        if (null != progressLayout) {
            showing = (View.VISIBLE == progressLayout.getVisibility());
        }

        return showing;
    }



}
