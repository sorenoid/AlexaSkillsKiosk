package com.bandor.android.alexaskillskiosk;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sorenoid on 6/24/16.
 */
public class SkillDetails implements Parcelable {
    private String displayName;
    private String vendor;
    private String audio;
    private int[] colors;
    private String skillDesc;
    private String id;
    private List<String> exampleInteractions = new LinkedList<>();
    private Integer avgRating;
    public final static String TAG = "ASK";
    private String sentence;

    public SkillDetails(SkillDetails skillDetails) {
        this.displayName = skillDetails.displayName;
        this.vendor = skillDetails.vendor;
        this.colors = new int[] { skillDetails.colors[0], skillDetails.colors[1]};
        this.id = skillDetails.id;
    }



    public SkillDetails(String id, String skillName, String skillDesc, String vendor, Integer avgRating, List<String> exampleInteractions, String launchPhrase) {
        displayName = skillName;
        this.vendor = vendor;
        this.skillDesc = skillDesc;
        this.id = id;
        this.audio = "flash-briefing.m4a";
        this.colors = new int[2];
        this.exampleInteractions.addAll(exampleInteractions);
        if (null != avgRating) {
            this.avgRating = avgRating;
        }
        if (null != launchPhrase) {
            this.sentence = "Alexa, open " + launchPhrase;
        } else {
            this.sentence = "Alexa, open " + skillName;
        }

    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getAudioFile() {
        return audio;
    }

    public String getVendor() {
        return vendor;
    }

    @Override
    public String toString() {
        return "SkillDetails{" +
                "displayName='" + displayName + '\'' +
                ", vendor='" + vendor + '\'' +
                ", audio='" + audio + '\'' +
                ", colors=" + Arrays.toString(colors) +
                ", skillDesc='" + skillDesc + '\'' +
                ", id='" + id + '\'' +
                ", exampleInteractions=" + exampleInteractions +
                ", avgRating=" + avgRating +
                '}';
    }

    /// Parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(vendor);
        dest.writeString(id);
        dest.writeInt(colors[0]);
        dest.writeInt(colors[1]);
        dest.writeString(sentence);
        dest.writeString(skillDesc);
        dest.writeInt(null != avgRating ? avgRating : -1);
        dest.writeInt(exampleInteractions.size());
        for (String example: exampleInteractions) {
            dest.writeString(example);
        }
    }

    protected SkillDetails(Parcel in) {
        displayName = in.readString();
        vendor = in.readString();
        id = in.readString();
        colors = new int[2];
        colors[0] = in.readInt();
        colors[1] = in.readInt();
        sentence = in.readString();
        skillDesc = in.readString();
        avgRating = in.readInt();
        if (-1 == avgRating) {
            avgRating = null;
        }
        int numExamples = in.readInt();
        for (int i = 0; i < numExamples; i++) {
            exampleInteractions.add(in.readString());
        }
    }

    public static final Parcelable.Creator<SkillDetails> CREATOR = new Parcelable.Creator<SkillDetails>() {
        @Override
        public SkillDetails createFromParcel(Parcel in) {
            return new SkillDetails(in);
        }

        @Override
        public SkillDetails[] newArray(int size) {
            return new SkillDetails[size];
        }
    };

    public List<String> getExamples() {
        return exampleInteractions;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getDescription() {
        return skillDesc;
    }
}
