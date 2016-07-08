package com.bandor.android.alexaskillskiosk;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Dynamically adds items (one for each skill) to the main layout
 */
public class KioskController {
    private static final String TAG = "ASKCTRL";
    public static final String EXTRA_DETAILS = "extraDetails";
    private final KioskActivity activity;
    private MediaPlayer mediaPlayer;
    private ViewGroup mainControllerLayout;

    /**
     * Construct a KioskController to handle the item views
     *
     * @param activity base activity
     */
    public KioskController(KioskActivity activity) {
        this.activity = activity;
        this.mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.reset();
            }
        });
    }

    /**
     * Connect this controller with its main layout
     */
    public void connectToLayout() {
        mainControllerLayout = (ViewGroup) activity.findViewById(R.id.main_kiosk_layout);
    }


    /**
     * Force items to relay themselves out with possibly new names
     */
    public void invalidate() {
        // Remove all items
        final ViewGroup topHorizontalLayout = (ViewGroup) mainControllerLayout.findViewById(R.id.top_horizontal_layout);
        topHorizontalLayout.removeAllViews();

        // Add views back in (with new names)
        reconcileItems();
    }

    /**
     * Add a new item based on this Detail (and set Itemlayout id to the Detail id)
     *
     * @param detail New SkillDetails
     */
    protected void appendItem(SkillDetails detail) {
        // Add new item views and set the item layout's id
        final ViewGroup topHorizontalLayout = (ViewGroup) mainControllerLayout.findViewById(R.id.top_horizontal_layout);
        appendTextItem(topHorizontalLayout, detail);
    }

    /**
     * Append a text item object to a top-level view
     *
     * @param parent top-level view
     * @param detail Detail to add
     * @return The main layout for this item
     */
    @SuppressWarnings("deprecation")
    private ViewGroup appendTextItem(ViewGroup parent, final SkillDetails detail) {
        final ViewGroup itemLayout = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.ask_item_text, null);
        // Set name
        TextView nameLayout = (TextView) itemLayout.findViewById(R.id.name);
        nameLayout.setText(detail.getDisplayName());

        // Set value
        final TextView vendorName = (TextView) itemLayout.findViewById(R.id.vendor_name);
        vendorName.setText(detail.getVendor());

        // Set background gradient
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, detail.getColors());
        itemLayout.setBackgroundDrawable(drawable);

        // Set the id layout of this item (so we can remove the item later if needed). This will never be actually shown to the user!
        TextView detailIdLayout = (TextView) itemLayout.findViewById(R.id.detail_id);
        detailIdLayout.setText(detail.getId());

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(activity, DetailsActivity.class);
                detailsIntent.putExtra(KioskController.EXTRA_DETAILS, detail);
                activity.startActivity(detailsIntent);
                //playSentenceAudio(detail);
            }
        });
        parent.addView(itemLayout);
        return itemLayout;
    }

    private void playSentenceAudio(SkillDetails detail) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                        mp.reset();
                    }
                });

            }

            String audioFile = detail.getAudioFile();
            AssetFileDescriptor descriptor = activity.getAssets().openFd(audioFile);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mediaPlayer.prepare();
            //mediaPlayer.setVolume(4f, 4f);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Return the item layout based on the Detail's id (or null if no such item exists)
     *
     * @param topLayout the top layout
     * @param id        Detail id
     * @return layout
     */
    private ViewGroup getItemLayoutByDetailId(ViewGroup topLayout, String id) {
        ViewGroup itemLayout = null;
        for (int i = 0; i < topLayout.getChildCount(); i++) {
            ViewGroup layout = (ViewGroup) topLayout.getChildAt(i);
            TextView DetailIdTextView = (TextView) layout.findViewById(R.id.detail_id);
            SkillDetails detail = SkillsData.getDetailById(DetailIdTextView.getText().toString());
            if ((null != detail) && TextUtils.equals(detail.getId(), id)) {
                itemLayout = layout;
                break;
            }
        }
        return itemLayout;
    }

    /**
     * If the Item Details have changed, tear down all items and then rebuild them
     */
    private void reconcileItems() {
        final ViewGroup topHorizontalLayout = (ViewGroup) mainControllerLayout.findViewById(R.id.top_horizontal_layout);

        boolean needsUpdate = false;

        for (SkillDetails detail  : SkillsData.getItems()) {
            ViewGroup itemLayout = getItemLayoutByDetailId(topHorizontalLayout, detail.getId());
            if (null == itemLayout) {
                if (null != detail) { // Bullet-proofing -- The Details might not have been built up yet
                    needsUpdate = true;
                    break;
                }
            }
        }

        if (!needsUpdate) { // See if any items are extra
            for (int i = 0; i < topHorizontalLayout.getChildCount(); i++) {
                View itemLayout = topHorizontalLayout.getChildAt(i);
                TextView detailIdTextView = (TextView) itemLayout.findViewById(R.id.detail_id);
                String detailId = detailIdTextView.getText().toString();
                SkillDetails detail = SkillsData.getDetailById(detailId);
                if (null == detail) {
                    needsUpdate = true;
                    break;
                }
            }
        }


        if (needsUpdate) { // Get rid of everything then build it back up
            topHorizontalLayout.removeAllViews();

            for (SkillDetails detail : SkillsData.getItems()) {
                if (null != detail) { // Bullet-proofing -- The Details might not have been built up yet
                    appendItem(detail);
                }
            }
        }
    }

}















