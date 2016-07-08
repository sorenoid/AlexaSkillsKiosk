package com.bandor.android.alexaskillskiosk;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
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
    private static List<SkillDetails> SkillsList;
    private static Map<String, SkillDetails> SkillsMap = new LinkedHashMap<>();
    private static Map<String, String> SearchMap = new LinkedHashMap<>();

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
     *
     * @param id
     * @param skillName
     * @param skillDesc
     * @param launchPhrase
     */
    public static void loadSkill(String id, String skillName, String skillDesc, String launchPhrase) {
        synchronized (SkillsMap) {
            SkillDetails skillDetails = new SkillDetails(id, skillName, skillDesc, launchPhrase);
            //Log.d(TAG, "adding skill: " + skillDetails);
            SkillsMap.put(id, skillDetails);
            SearchMap.put(skillName, id);
        }
    }

    public static void reload(Activity activity) {
        SkillsMap.clear();
        SearchMap.clear();
        load(activity);
    }

    public static void fillCursor(KioskActivity kioskActivity, String substr, final SearchView search) {
        if (!SkillsMap.isEmpty()) {
            List<SkillDetails> matches = new LinkedList<SkillDetails>();
            String[] columns = new String[]{"_id", "view_id", "text"};
            Object[] temp = new Object[]{0, "default", "default"};

            MatrixCursor searchableSkills = new MatrixCursor(columns);
            int i = 0;
            List<SkillDetails> subsetOfSkills = new LinkedList<>();
            if (!TextUtils.isEmpty(substr)) {
                for (String name : SearchMap.keySet()) {
                    String lowerName = name.toLowerCase();
                    String lowerSubStr = substr.toLowerCase();
                    if (lowerName.contains(lowerSubStr)) {
                        SkillDetails skillDetails = SkillsMap.get(SearchMap.get(name));
                        temp[0] = i;
                        temp[1] = skillDetails.getId();
                        temp[2] = skillDetails.getDisplayName();

                        searchableSkills.addRow(temp);
                        subsetOfSkills.add(skillDetails);
                        i++;
                    }
                }
            }

            // SearchView
            //SearchManager manager = (SearchManager) kioskActivity.getSystemService(Context.SEARCH_SERVICE);
            search.setSuggestionsAdapter(new SkillsSuggestionAdapter(kioskActivity, searchableSkills, subsetOfSkills));

        }

    }

}
