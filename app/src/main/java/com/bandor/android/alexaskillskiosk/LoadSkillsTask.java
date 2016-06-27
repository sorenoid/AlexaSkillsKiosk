package com.bandor.android.alexaskillskiosk;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sorenoid on 6/26/16.
 */
public class LoadSkillsTask extends AsyncTask<InputStream, Void, Void> {

    Activity activity;

    LoadSkillsTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(InputStream... streams) {

        InputStream stream = streams[0];
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(stream, null);
            SkillsData.loadMap(parser);
            stream.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void nil) {
        SkillsData.setLoaded(true);
        Intent intent = new Intent();
        intent.setAction(SkillsData.BROADCAST_LOAD_COMPLETE);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        super.onPostExecute(nil);
    }
}
