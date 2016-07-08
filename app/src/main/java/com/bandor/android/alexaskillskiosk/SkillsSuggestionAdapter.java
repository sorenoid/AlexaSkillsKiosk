package com.bandor.android.alexaskillskiosk;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sorenoid on 7/7/16.
 */
public class SkillsSuggestionAdapter extends android.support.v4.widget.CursorAdapter {
    private List<SkillDetails> items;
    //private TextView text;

    public SkillsSuggestionAdapter(Context context, Cursor cursor, List<SkillDetails> items) {
        super(context, cursor, false);
        this.items = items;
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        final TextView textView = (TextView) view.findViewById(R.id.search_text_view);
        textView.setText(items.get(cursor.getPosition()).getDisplayName());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ret = false;
                Intent detailsIntent = new Intent(context, DetailsActivity.class);
                String id = SkillsData.getIdFromName(textView.getText().toString());
                if (null != id) {
                    SkillDetails details = SkillsData.getDetailById(id);
                    detailsIntent.putExtra(KioskController.EXTRA_DETAILS, details);
                    context.startActivity(detailsIntent);
                }

            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_view, parent, false);
        //text = (TextView) view.findViewById(R.id.search_text_view);
        return view;
    }


}
