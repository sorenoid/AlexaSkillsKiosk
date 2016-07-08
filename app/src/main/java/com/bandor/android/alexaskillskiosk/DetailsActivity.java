package com.bandor.android.alexaskillskiosk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sorenoid on 7/8/16.
 */

public class DetailsActivity extends Activity {

    SkillDetails skill;
    LayoutInflater inflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        skill = getIntent().getParcelableExtra(KioskController.EXTRA_DETAILS);
        setContentView(R.layout.skill_details_activity);


        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        View view = findViewById(R.id.skill_details);
        view.setBackgroundColor(skill.getColors()[0]);

        TextView textView = (TextView) view.findViewById(R.id.vendor_name);
        textView.setText(skill.getVendor());

        textView = (TextView) view.findViewById(R.id.name);
        textView.setText(skill.getDisplayName());

        textView = (TextView) view.findViewById(R.id.short_desc);
        textView.setText(skill.getDescription());

        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.examples);

        for (String example: skill.getExamples()) {
            ViewGroup exampleBody = (ViewGroup) inflater.inflate(R.layout.example_interaction, null);
            TextView exampleText = (TextView) exampleBody.findViewById(R.id.example_text);
            exampleText.setText(example);
            viewGroup.addView(exampleBody);
        }

        if (skill.getExamples().isEmpty()) {
            ViewGroup exampleBody = (ViewGroup) inflater.inflate(R.layout.example_interaction, null);
            TextView exampleText = (TextView) exampleBody.findViewById(R.id.example_text);
            exampleText.setText(skill.getSentence());
            viewGroup.addView(exampleBody);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
