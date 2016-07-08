package com.bandor.android.alexaskillskiosk;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sorenoid on 6/24/16.
 */
public class SkillsData {
    private static boolean loaded = false;
    public final static String TAG = "ASK";
    private static List<SkillDetails> items;
    private static Map<String, SkillDetails> SkillsMap = new LinkedHashMap<>();
    public static String BROADCAST_LOAD_COMPLETE = "broadcast_load_complete";

    public static SkillDetails getDetailById(String id) {
        SkillDetails detail = null;
        if (SkillsMap.containsKey(id)) {
            detail = new SkillDetails(SkillsMap.get(id));
        }
        return detail;
    }

    public static List<SkillDetails> getItems() {
        return new LinkedList<>(SkillsMap.values());
    }

    public static void load(Activity activity) {
        loaded = false;
        AssetManager assetManager = activity.getAssets();
        InputStream skillStream = null;
        try {
            skillStream = assetManager.open("skills-public.json");
            LoadSkillsTask loader = new LoadSkillsTask(activity);
            loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, skillStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean getLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean b) {
        loaded = b;
    }

    //note: called from non ui thread!
    public static synchronized void loadMap(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "SkillsMap");

        String name = parser.getName();
        SkillDetails dItem = null;
        int eventType = parser.getEventType();
        do {
            name = parser.getName();
           // Log.d(TAG, "loading skils map with name " + name);
            if (eventType == parser.START_DOCUMENT) {
             //   Log.d(TAG, "start doc");
            } else if (eventType == parser.END_DOCUMENT) {
               // Log.d(TAG, "end doc");
            } else if (eventType == parser.START_TAG) {

                int numAttribs = parser.getAttributeCount();
                if (TextUtils.equals(name, "Skill")) {
                    dItem = new SkillDetails(parser);

                }
//                for (int i = 0; i < numAttribs; i++) {
//                    Log.d(TAG, "START TAG event name " + name + " attribute "
//                            + i + " has key " + parser.getAttributeName(i) + " , value " + parser.getAttributeValue(i));
//                }

            } else if (eventType == parser.END_TAG) {
                Log.d(TAG, "end tag");
                //LogUtils.Log(LogUtils.Area.SensorMap, null, "END TAG event name " + name + " text of skill event " + parser.getText());
                if (TextUtils.equals(name, "Skill")) {
                    if (null != dItem) {
                        // LogUtils.Log(LogUtils.Area.Link, null, "adding skill " + skill.id + " with auto id " + skill.autoId + " to map");
                        //if (skill.dataType.equals(Sensor.DataType.digital)) {
                       // Log.d(TAG, "loading detail " + dItem);
                        SkillsMap.put(dItem.getId(), dItem);
                    }

                    dItem = null;
                }

            } else if (eventType == parser.TEXT) {
                Log.d(TAG, "text");
                //  LogUtils.Log(LogUtils.Area.SensorMap, null, "text event name " + name + " text of skill event " + parser.getText());

            }
            eventType = parser.nextToken();
        } while (eventType != parser.END_DOCUMENT);

    }

    public static synchronized void colorize(Context context) {
        TypedArray primaryColors = context.getResources().obtainTypedArray(R.array.skills_colors_primary);
        TypedArray secondaryColors = context.getResources().obtainTypedArray(R.array.skills_colors_secondary);

        int colorIndex = 0;
        int xIndex = primaryColors.length() - 1;

        // Sort the datasets by the timestamps of their skills
        List<String> detailOrder = SkillsData.sortByTimeStamp(/* descending */false);
        for (String id : detailOrder) {

            SkillDetails detail = SkillsMap.get(id);
            int index = (colorIndex % xIndex);
            detail.setColors(primaryColors.getColor(index, 0), secondaryColors.getColor(index, 0));
           // Log.d(TAG, "setting colors for detail with id " + id + " to " + detail.getColors()[0]);
            colorIndex++;
        }

        primaryColors.recycle();
        secondaryColors.recycle();
    }


    private static List<String> sortByTimeStamp(boolean b) {
        return new LinkedList<>(SkillsMap.keySet());
    }

    /**
     * add a skill to the map. called from bg thread.
     * @param id
     * @param skillName
     * @param skillDesc
     * @param launchPhrase
     */
    public static void loadSkill(String id, String skillName, String skillDesc, String launchPhrase) {
        synchronized (SkillsMap) {
            SkillDetails skillDetails = new SkillDetails(id, skillName, skillDesc, launchPhrase);
            Log.d(TAG, "adding skill: " + skillDetails);
            SkillsMap.put(id, skillDetails);
        }
    }

    public static void reload(Activity activity) {
        SkillsMap.clear();
        load(activity);
    }
}
