package com.bandor.android.alexaskillskiosk;

import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.Arrays;

/**
 * Created by sorenoid on 6/24/16.
 */
public class SkillDetails {
    private String displayName;
    private String sentence;
    private String audio;
    private int[] colors;
    private String skillDesc;
    private String id;
    public final static String TAG = "ASK";

    public SkillDetails(SkillDetails skillDetails) {
        this.displayName = skillDetails.displayName;
        this.sentence = skillDetails.sentence;
        this.colors = new int[] { skillDetails.colors[0], skillDetails.colors[1]};
        this.id = skillDetails.id;
    }

    public SkillDetails(XmlPullParser parser) {
        int numAttribs = parser.getAttributeCount();
        colors = new int[2];

        for (int i = 0; i < numAttribs; i++) {
            String key = parser.getAttributeName(i);
            String txt = parser.getAttributeValue(i);

            if (TextUtils.equals("id", key)) {
                id = txt;
            } else if (TextUtils.equals("name", key)) {
                displayName = txt;

            } else if (TextUtils.equals("sentence", key)) {
                sentence = txt;
            } else if (TextUtils.equals("audio", key)) {
                audio = txt;
            } else if (TextUtils.equals("color1", key)) {
                colors[0] = Integer.valueOf(txt);

            } else if (TextUtils.equals("color2", key)) {
                colors[1] = Integer.valueOf(txt);

            }
        }
    }

    public SkillDetails(String id, String skillName, String skillDesc, String launchPhrase) {
        displayName = skillName;
        sentence = "Alexa, open " + launchPhrase;
        this.skillDesc = skillDesc;
        this.id = id;
        this.audio = "flash-briefing.m4a";
        this.colors = new int[2];
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public int[] getColors() {
        //Log.d(TAG,"returning colors of " + colors[0] + " and " + colors[1]);
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }


    public void setColors(int color, int color1) {
        colors[0] = color;
        colors[1] = color1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SkillDetails{" +
                "displayName='" + displayName + '\'' +
                ", sentence='" + sentence + '\'' +
                ", audio='" + audio + '\'' +
                ", colors=" + Arrays.toString(colors) +
                ", id='" + id + '\'' +
                '}';
    }

    public String getAudioFile() {
        return audio;
    }
}
