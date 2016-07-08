package com.bandor.android.alexaskillskiosk;

import android.content.Context;
import android.database.Cursor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sorenoid on 7/7/16.
 */
public class SkillsSuggestionAdapter extends android.support.v4.widget.CursorAdapter {
    private List<SkillDetails> items;
    private TextView text;

    public SkillsSuggestionAdapter(Context context, Cursor cursor, List<SkillDetails> items) {
        super(context, cursor, false);
        this.items = items;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text.setText(items.get(cursor.getPosition()).getDisplayName());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_view, parent, false);
        text = (TextView) view.findViewById(R.id.search_text_view);
        return view;
    }
}
