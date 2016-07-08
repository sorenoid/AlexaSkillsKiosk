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
import java.util.LinkedList;
import java.util.List;

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
                    String vendor = null;
                    String id = null;
                    Integer avgRating = null;
                    List<String> exampleInteractions = new LinkedList<>();
                    reader.beginArray();
                    while (reader.hasNext()) {
                       reader.beginObject();
                        while (reader.hasNext()) {
                            name = reader.nextName();
//                            "accountLinkingWhitelistedDomains": null,
//                                    "asin": "B017OBKT2Q",
//                                    "averageRating": 2.2,
//                                    "canDisable": true,
//                                    "capabilities": null,
//                                    "category": "Games",
//                                    "description": "Crystal Ball is a fortune teller skill. Alexa prompts you to first focus on a yes/no question, and then say when you're ready to hear the answer.",
//                                    "enablement": null,
//                                    "exampleInteractions": [
//                            "Alexa, launch Crystal Ball.",
//                                    "Alexa, ask Crystal Ball for the answer.",
//                                    "Alexa, ask Crystal Ball if I will win the lottery."
//                            ],
//                            "firstReleaseDate": 1446840611.325,
//                                    "homepageLinkText": null,
//                                    "homepageLinkUrl": null,
//                                    "id": "amzn1.echo-sdk-ams.app.713be741-ed23-4bd0-8090-4b5931b9f05f",
//                                    "imageAltText": "Crystal Ball icon",
//                                    "imageUrl": "https://github.com/dale3h/alexa-skills-list/raw/master/skills/B017OBKT2Q/skill_icon",
//                                    "inAppPurchasingSupported": false,
//                                    "launchPhrase": "crystal ball",
//                                    "name": "Crystal Ball",
//                                    "numberOfReviews": 7,
//                                    "pamsPartnerId": null,
//                                    "permissions": null,
//                                    "privacyPolicyUrl": null,
//                                    "shortDescription": null,
//                                    "skillTypes": null,
//                                    "stage": "live",
//                                    "termsOfUseUrl": null,
//                                    "vendorId": "M27RQ5Q898CEGD",
//                                    "vendorName": "LME Skills"
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
                            } else if ("vendorName".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    vendor = Utils.readJSONString(reader);
                                } else {
                                    reader.skipValue();
                                }
                            } else if ("averageRating".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    avgRating = Utils.readJSONInteger(reader);
                                } else {
                                    reader.skipValue();
                                }
                            } else if ("exampleInteractions".equals(name)) {
                                if (reader.peek() != JsonToken.NULL) {
                                    reader.beginArray();
                                    while(reader.hasNext()) {
                                        if (reader.peek() != JsonToken.NULL) {
                                            exampleInteractions.add(Utils.readJSONString(reader));
                                        } else {
                                            reader.skipValue();
                                        }

                                    }
                                    reader.endArray();
                                } else {
                                    reader.skipValue();
                                }
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        if (null != id && null != skillName && null != skillDesc && null != vendor) {
                            SkillsData.loadSkill(id, skillName, skillDesc, vendor, avgRating, exampleInteractions, launchPhrase);
                        }
                        skillName = id = skillDesc = vendor = launchPhrase = null;
                        avgRating = null;
                        exampleInteractions.clear();
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
