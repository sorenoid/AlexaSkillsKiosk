package com.bandor.android.alexaskillskiosk;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by sorenoid on 6/26/16.
 */
public class LoadSkillsTask extends AsyncTask<InputStream, Void, Void> {

    private static final String TAG = "LST";
    Activity activity;

    LoadSkillsTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(InputStream... streams) {
        String content = Utils.convertStreamToString(streams[0]);
        Reader stringReader = new StringReader(content);
        JsonReader reader = new JsonReader(stringReader);

        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                if ("apps".equals(name)) {
                    String skillName = null;
                    String skillDesc = null;
                    String launchPhrase = null;
                    String id = null;
                    reader.beginArray();
                    while (reader.hasNext()) {
                       reader.beginObject();
                        while (reader.hasNext()) {
                            name = reader.nextName();
                            if ("name".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    skillName = Utils.readJSONString(reader);
                                } else {
                                    reader.skipValue();
                                }
                            } else if ("shortDescription".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    skillDesc = Utils.readJSONString(reader);
                                } else {
                                    reader.skipValue();
                                }
                            } else if ("launchPhrase".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    launchPhrase = Utils.readJSONString(reader);
                                } else {
                                    reader.skipValue();
                                }
                            } else if ("id".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    id = Utils.readJSONString(reader);
                                } else {
                                    reader.skipValue();
                                }
                            }else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        if (null != id && null != skillName && null != skillDesc && null != launchPhrase) {
                            SkillsData.loadSkill(id, skillName, skillDesc, launchPhrase);
                        }
                        skillName = id = skillDesc = launchPhrase = null;
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
            reader.close();
        } catch (IOException e) {

            Utils.logException(TAG, "", e);
        }  catch (Exception e) {

            Utils.logException(TAG, "", e);
        }


        return null;
    }

    private void print(String preamble, String name) throws IOException {
        Log.d(TAG, preamble + " -> reader next name: " + name);
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
