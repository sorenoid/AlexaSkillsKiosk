package com.bandor.android.alexaskillskiosk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kitchen-sink static methods to do useful things
 */
public class Utils {
    private static final String TAG = "Utils";

    private static final TextPaint textPaint = new TextPaint();
    private static final Rect textRect = new Rect();
    private static final Rect boxRect = new Rect();
    public static double EPSILON = 1.0e-8;

    /**
     * Convert a value from abstract, world space to pixels
     *
     * @param world:    x or y value to convert
     * @param worldMin  min range in world space
     * @param worldMax  max range
     * @param screenMin min range in screen space
     * @param screenMax max range
     * @return pixel value
     */
    public static float worldToScreen(double world, double worldMin, double worldMax, int screenMin, int screenMax) {
        float screen;
        screen = (float) ((world - worldMin) * (screenMax - screenMin) / (worldMax - worldMin) + screenMin);
        return screen;
    }

    /**
     * Convert an x or y value pixel from the screen to world space
     *
     * @param horizontal: vs. vertical
     * @param screen:     pixel to convert
     * @param worldMin    min world range
     * @param worldMax    max
     * @param screenMin   min screen range
     * @param screenMax   max
     * @return world value
     */
    public static double screenToWorld(boolean horizontal, float screen, double worldMin, double worldMax, int screenMin, int screenMax) {
        double world;
        if (horizontal) {
            world = (screen - screenMin) * (worldMax - worldMin) / (screenMax - screenMin) + worldMin;
        } else {
            world = (screen - screenMax) * (worldMax - worldMin) / (screenMin - screenMax) + worldMin; // note y-values are "upside down"
        }
        return world;
    }

    /**
     * Consider two doubles equal if their difference is less than epsilon
     *
     * @param x1 doubles to compare
     * @param x2 double
     * @return true if they are within EPSILON
     */
    public static boolean doubleEquals(double x1, double x2) {
        return Math.abs(x1 - x2) < EPSILON;
    }

    /**
     * Draw a rectangle with a border
     *
     * @param r           the rectangle to draw
     * @param bkColor     fill color
     * @param borderColor line color
     * @param borderWidth width of line (0 ==> no line)
     */
    public static void drawRectWithBorder(Canvas canvas, Rect r, int bkColor, int borderColor, int borderWidth) {
        // draw the rect
        textPaint.setColor(bkColor);
        textPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(r, textPaint);

        if (0 < borderWidth) { // Draw the border
            textPaint.setColor(borderColor);
            textPaint.setStrokeWidth(borderWidth);
            textPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(r, textPaint);
        }
    }


    /**
     * Draw text inside a rectangle with handles -- force rect to stay inside bounds. give extra space for some handles.
     *
     * @param canvas        drawing surface
     * @param context       current parentActivity
     * @param strings       Multi lines of text to display
     * @param x             location to draw (subject to justification)
     * @param y             location
     * @param justification what x,y means (e.g. if this is TopLeft, then x,y is top left of rectangle)
     * @param textSizes     sizes of text (one size per line)
     * @param textColor     RGB color of text
     * @param bkColor       ARGB color of background
     * @param borderColor   ARGB color of border
     * @param borderWidth   line width of border
     * @param bounds        rectangle to stay within
     */
    public static void drawTextRect(Canvas canvas, Context context, List<String> strings, int x, int y, Justification justification, List<Integer> textSizes, int textColor, int bkColor, int borderColor, int borderWidth, int textPadding, Rect bounds, Rect drawRect) {
        // Antialias the text so it looks smooth
        textPaint.setAntiAlias(true);

        int height = 0;
        int width = 0;
        List<Integer> heights = new ArrayList<Integer>();
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            int textSize = textSizes.get(i);

            // Set text size (so we can measure)
            textPaint.setTextSize(textSize);

            // Measure text
            Utils.getTextBounds(s, textPaint, textRect);

            // get width and height with padding
            int w = textRect.width() + textPadding * 2 + borderWidth;
            int h = textRect.height() + textPadding * 2 + borderWidth;

            width = Math.max(width, w);
            height += h;

            heights.add(h);
        }

        // Do horizontal justification
        switch (justification) {
            case TopCenter:
            case CenterCenter:
            case BottomCenter:
                x -= (width + 1) / 2;
                break;
            case TopRight:
            case CenterRight:
            case BottomRight:
                x -= width;
                break;
            default:
                break;
        }
        // Do vertical justification
        switch (justification) {
            case CenterLeft:
            case CenterCenter:
            case CenterRight:
                y -= (height + 1) / 2;
                break;
            case BottomLeft:
            case BottomCenter:
            case BottomRight:
                y -= height;
                break;
            default:
                break;
        }

        textRect.set(x, y, x + width + 1, y + height + 1);

        // Make sure the rectangle is within the graph bounds
        containRectInsideRect(bounds, borderWidth, textRect);

        boxRect.set(textRect);
            drawRectWithBorder(canvas, boxRect, bkColor, borderColor, borderWidth);

        y = textRect.top + textPadding + borderWidth / 2 + heights.get(0) / 2;
        x = (textRect.left + textRect.right) / 2;
        textPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            int textSize = textSizes.get(i);

            // Set text size (so we can measure)
            textPaint.setTextSize(textSize);

            // Draw the text
            textPaint.setColor(textColor);
            textPaint.setStrokeWidth(0);
            canvas.drawText(s, x, y, textPaint);

            y += heights.get(i) - borderWidth;
        }

        // If caller wants the bounds, caller can have them
        if (null != drawRect) {
            drawRect.set(boxRect);
        }
    }

    /**
     * Draw text wrapped in a rectangle
     *
     * @param canvas    drawing surface
     * @param s         text to draw
     * @param x         position
     * @param y         position
     * @param bounds    parent object bounds
     * @param align     left/right/center
     * @param textSize  size of text in pixels
     * @param textColor color
     * @param bkColor   background color
     * @param pad       paddign around text in pixels
     * @param drawRect  rectangle to stay within
     * @return true if drawRect had to change to stay within bounds
     */
    public static boolean drawWrappedText(Canvas canvas, String s, int x, int y, Rect bounds, @SuppressWarnings("SameParameterValue") Alignment align, int textSize, int textColor, int bkColor, int pad, Rect drawRect) {
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true); // Anti-alias the text so it looks smooth

        // Use StaticLayout to do heavy lifting of calculating the word wrap
        StaticLayout sl = new StaticLayout(s, textPaint, bounds.width() - 2 * pad, align, /*spacingMult*/1.0f, /*spacingAdd*/0.0f, /*includePad*/false);
        boolean singleLine = (1 == sl.getLineCount());

        int width = singleLine ? (int) textPaint.measureText(s) + 2 * pad : bounds.width(); // static layout always returns the maxWidth -- even in the single-line case where that's just not right
        int height = sl.getHeight() + 2 * pad;

        if (singleLine) {
            switch (align) {
                case ALIGN_NORMAL:
                    drawRect.set(x, y, x + width, y + height);
                    break;
                case ALIGN_CENTER:
                    x -= width / 2;
                    drawRect.set(x, y, x + width, y + height);
                    break;
                case ALIGN_OPPOSITE:
                default:
                    drawRect.set(x - width, y, x, y + height);
                    break;
            }
        } else {
            drawRect.set(bounds.left, y, bounds.right, y + height);
        }

        // Make sure the rectangle is within the graph bounds
        boolean changed = containRectInsideRect(bounds, 0, drawRect);

        // Draw background rectangle
        textPaint.setColor(bkColor);
        textPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(drawRect, textPaint);

        // Draw text
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.LEFT);
        if (singleLine) { // Use regular draw text method so we can center in helper (use drawRect for position because constraint above may have changed it)
            canvas.drawText(s, 0, s.length(), drawRect.left + pad, drawRect.top + height - 3 * pad / 2, textPaint); // HACK "3" is to get the descenders (like from "g") up just a bit.
        } else { // Draw the static text layout
            canvas.save(); // save/restore so translate doesn't muck up later calls
            canvas.translate(pad, y + pad);
            sl.draw(canvas);
            canvas.restore();
        }

        return changed;
    }

    /**
     * Contain one rectangle inside another
     *
     * @param bounds      Larger rectangle to stay within
     * @param borderWidth stroke width of border
     * @param r           [out] smaller rectangle to be contained inside bounds
     * @return true if r was changed
     */
    public static boolean containRectInsideRect(Rect bounds, int borderWidth, Rect r) {
        boolean changed = false;

        int left = r.left - borderWidth / 2;
        int right = r.right + (borderWidth + 1) / 2;
        int top = r.top - borderWidth / 2;
        int bottom = r.bottom + (borderWidth + 1) / 2;

        // Make sure textRect stays within bounds
        if (left < bounds.left) {
            r.offset(bounds.left - left, 0);
            changed = true;
        }
        if (top < bounds.top) {
            r.offset(0, bounds.top - top);
            changed = true;
        }
        if (right > bounds.right) {
            r.offset(bounds.right - right, 0);
            changed = true;
        }
        if (bottom > bounds.bottom) {
            r.offset(0, bounds.bottom - bottom);
            changed = true;
        }
        return changed;
    }

    /**
     * Android has bugs with measuring text:
     * 1) Won't include leading or trailing spaces.  We'll add a '.' if needed at front or back
     * 2) Will not set bounds to 0 for empty text
     *
     * @param text   String to measure
     * @param paint  Current paint to use (for text size etc.)
     * @param bounds [out] the bounding rectangle
     */
    public static void getTextBounds(String text, Paint paint, Rect bounds) {
        bounds.set(0, 0, 0, 0);
        if (!TextUtils.isEmpty(text)) {
            StringBuilder sb = new StringBuilder();
            if (' ' == text.charAt(0)) {
                sb.append('.');
            }
            sb.append(text);
            if (' ' == text.charAt(text.length() - 1)) {
                sb.append('.');
            }
            paint.getTextBounds(sb.toString(), 0, sb.length(), bounds);
        }
    }

    /**
     * If the string is too long, truncate and add ellipsis
     *
     * @param context to get ellipsis
     * @param text    text to change
     * @param width   text must fit inside
     * @param paint   to get text size etc.
     * @return display string
     */
    public static String ellipsizeStringIfNeeded(Context context, String text, int width, Paint paint) {
        String s = text;
        int sWidth = (int) paint.measureText(s);
        if (sWidth > width) { // do as little as possible if text fits
            String ellipsis = context.getString(R.string.ellipsis);
            if ((int) paint.measureText(ellipsis) > width) { // edge case of tiny width -- just return an empty string
                s = "";
            } else {
                s += ellipsis;
                int eLen = ellipsis.length();
                while (sWidth > width) {
                    if (eLen < s.length()) {
                        s = s.substring(0, s.length() - 1 - eLen);
                    } else {
                        break;
                    }
                    s += ellipsis;
                    sWidth = (int) paint.measureText(s);
                }
            }
        }

        return s;
    }

    /**
     * Convert an input stream to a string (e.g. for we can log network responses)
     *
     * @param is input stream
     * @return string (or "" if error)
     */
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            Utils.logException(TAG, "", e);
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Utils.logException(TAG, "", e);
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    /**
     * On the initial move after a down event, prevent tiny deltas from triggering the move
     *
     * @param moved Has the user already moved this down-move-up cycle?  (If so, this move always counts)
     * @param downX initial location of down event
     * @param downY location
     * @param event Current event
     * @return true if event is far enough away from down event to count as an actual move
     */
    public static boolean actualMove(Context context, boolean moved, float downX, float downY, MotionEvent event) {
        boolean ok = true;
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());     // Convert to pixels

        if (!moved) {
            float deltaX = Math.abs(downX - event.getX());
            float deltaY = Math.abs(downY - event.getY());
            ok = (px < deltaX) || (px < deltaY); // If one or the other is greater than the "slop" amount, it counts as a move
        }
        return ok;
    }

    /**
     * Get the portrait/landscape value that the screen currently is
     *
     * @param activity base activity
     * @return screen orientation
     */
    public static int getScreenOrientation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width || (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    /**
     * See if int x is between left and right (inclusive)
     *
     * @param left  bound
     * @param x     value to test
     * @param right bound
     * @return true if between
     */
    public static boolean between(int left, int x, int right) {
        return ((left <= x) && (x <= right)) || ((right <= x) && (x <= left));
    }

    /**
     * See if double x is between left and right (inclusive)
     *
     * @param left  bound
     * @param x     value to test
     * @param right bound
     * @return true if between
     */
    public static boolean between(double left, double x, double right) {
        return ((left <= x) && (x <= right)) || ((right <= x) && (x <= left));
    }

    /**
     * Get the number of digits after the decimal place in this string -- handle non-dot decimal separators
     *
     * @param s e.g. "1.23"
     * @return num places (e.g. 2)
     */
    public static int getNumPlaces(String s) {
        int num = 0;
        String sep = getDecimalSeparator();
        int idx = s.indexOf(sep);
        if (-1 != idx) {
            num = s.length() - idx - 1;
        }

        return num;
    }

    /**
     * Return a value as a string in regular or scientific notation with 4 significant figures -- whichever is shorter
     *
     * @param value: Value to convert to string
     * @return string to display
     */
    public static String format(Double value, int sigFigs) {
        String s = "";
        if (null != value) {
            s = String.format("%." + sigFigs + "G", value);
        }
        return s;
    }

    /**
     * using the magnitude of the tick increment, RETURN the format string to use
     *
     * @param ticks: ArrayList of tick values
     * @return string
     */
    public static String getOptimalFormatStringForTicks(ArrayList<Double> ticks) {
        String format = "%.2f";
        int last = ticks.size() - 1;
        if (1 < ticks.size() && !Double.isNaN(ticks.get(0)) && !Double.isNaN(ticks.get(last)) && !ticks.get(0).equals(ticks.get(last))) { // prevent bad format string from degenerate cases that should never happen anyway
            double diff = Math.abs(ticks.get(last) - ticks.get(0));
            if (1e6 > diff) {
                int numFractDigitsToUse = 0;
                diff = diff / last; // calculate tick increment
                if (!Double.isInfinite(diff) && !Double.isNaN(diff) && (0 < diff)) { // figure number of decimal places to use based on the diff
                    double fMag = Math.log10(diff);
                    double fFloorMag = Math.floor(fMag);
                    if (fFloorMag <= 0) {
                        numFractDigitsToUse = (int) -fFloorMag;
                    }
                }
                format = "%." + numFractDigitsToUse + "f";
            } else { // Use scientific
                format = "%.2G";
            }
        }

        return format;
    }

    /**
     * Get (Non-recursively) all children of a root view
     *
     * @param v root view
     * @return all children
     */
    public static List<View> getAllChildrenBFS(View v) {
        List<View> visited = new ArrayList<View>();
        List<View> unvisited = new ArrayList<View>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);
            if (child instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) child;
                final int childCount = group.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    unvisited.add(group.getChildAt(i));
                }
            }
        }

        return visited;
    }

    /**
     * Use current Locale to get the decimal separator
     *
     * @return decimal separator
     */
    public static String getDecimalSeparator() {
        String separator = ",";
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) nf;
            DecimalFormatSymbols sym = df.getDecimalFormatSymbols();
            char decSeparator = sym.getDecimalSeparator();
            separator = "" + decSeparator;
        }
        return separator;
    }

    /**
     * Depending on the decimal separator, get the cell separator (a "," unless that's also the decimal separator, in which case return ";")
     *
     * @return cell separator
     */
    public static String getCellSeparator() {
        String cellSep = ",";
        if (",".equals(getDecimalSeparator())) {
            cellSep = ";";
        }
        return cellSep;
    }


    /**
     * Get the number of decimal places this format string is saying to display
     *
     * @return number of places
     */
    public static int extractNumPlaces(String format) {
        int num = 0;
        int start = format.indexOf('.') + 1;
        int end = format.indexOf('f');
        if (-1 == end) {
            end = format.indexOf('g');    // mac uses 'g' for scientific
        }
        if (-1 == end) {
            end = format.indexOf('E');    // But Windows & LabQuest use 'E'
        }
        if (start < end) {
            String sub = format.substring(start, end);
            try {
                num = Integer.valueOf(sub);
            } catch (NumberFormatException e) { // ignore
            }
        }
        return num;
    }

    /**
     * Return 1 if format1 is "better" than format2, -1 if vice versa and 0 if they're the same
     * "Better: in this case means either 'g' over 'f', or if that's the same, more places
     *
     * @param format1 format to test
     * @param format2 format to test
     * @return -1, 0, 1
     */
    public static int compareFormats(String format1, String format2) {
        int ret = 0;
        if (!TextUtils.equals(format1, format2)) {
            if (null == format2) {
                ret = 1;
            } else {
                boolean f1f = (-1 != format1.indexOf('f')); // vs. 'g'
                boolean f2f = (-1 != format2.indexOf('f'));
                if (f1f == f2f) { // If the same type of format, choose the one with more places
                    int num1 = extractNumPlaces(format1);
                    int num2 = extractNumPlaces(format2);
                    ret = (num1 > num2) ? 1 : -1;
                } else { // one is 'f', the other is 'g'.  Choose 'g'
                    ret = f1f ? -1 : 1;
                }
            }
        }

        return ret;
    }


    /**
     * To avoid an exception when trying to log an exception that has no message
     *
     * @param tag     TAG to use
     * @param prepend Optional string to prepend to message
     * @param e       Excdeption to get message from
     */
    public static void logException(String tag, String prepend, Exception e) {
        String msg = e.getMessage();
        Log.e(tag, ((null != prepend) ? prepend : "") + ((null != msg) ? msg : "excpetion no message"));
        e.printStackTrace();
    }

    /**
     * Write a Double (write NaN if null)
     *
     * @param out stream
     * @param d   value to write
     * @throws IOException
     */
    public static void writeDouble(DataOutputStream out, Double d) throws IOException {
        out.writeDouble((null != d) ? d : Double.NaN);
    }

    /**
     * Read a double.  If NaN, return null
     *
     * @param in stream
     * @return value read
     * @throws IOException
     */
    public static Double readDouble(DataInputStream in) throws IOException {
        Double d = in.readDouble();
        Double ret = Double.isNaN(d) ? null : d;
        return ret;
    }

    /**
     * Write a Long (handle null)
     *
     * @param out stream
     * @param l   value to write
     * @throws IOException
     */
    public static void writeLong(DataOutputStream out, Long l) throws IOException {
        out.writeInt((null != l) ? 1 : 0);
        out.writeLong((null != l) ? l : 0);
    }

    /**
     * Read a long.  Handle null.
     *
     * @param in stream
     * @return value read
     * @throws IOException
     */
    public static Long readLong(DataInputStream in) throws IOException {
        boolean b = (1 == in.readInt());
        long l = in.readLong();
        Long ret = b ? l : null;
        return ret;
    }

    /**
     * Write a Float (handle null)
     *
     * @param out stream
     * @param f   value to write
     * @throws IOException
     */
    public static void writeFloat(DataOutputStream out, Float f) throws IOException {
        out.writeInt((null != f) ? 1 : 0);
        out.writeFloat((null != f) ? f : 0);
    }

    /**
     * Read a Float.  Handle null.
     *
     * @param in stream
     * @return value read
     * @throws IOException
     */
    public static Float readFloat(DataInputStream in) throws IOException {
        boolean b = (1 == in.readInt());
        float f = in.readFloat();
        Float ret = b ? f : null;
        return ret;
    }

    /**
     * Write an Integer (handle null)
     *
     * @param out stream
     * @param i   value to write
     * @throws IOException
     */
    public static void writeInt(DataOutputStream out, Integer i) throws IOException {
        out.writeInt((null != i) ? 1 : 0);
        out.writeInt((null != i) ? i : 0);
    }

    /**
     * Read an Integer.  Handle null.
     *
     * @param in stream
     * @return value read
     * @throws IOException
     */
    public static Integer readInt(DataInputStream in) throws IOException {
        boolean b = (1 == in.readInt());
        int i = in.readInt();
        Integer ret = b ? i : null;
        return ret;
    }

    /**
     * Write a Boolean (handle null)
     *
     * @param out stream
     * @param b   value to write
     * @throws IOException
     */
    public static void writeBoolean(DataOutputStream out, Boolean b) throws IOException {
        out.writeInt((null != b) ? 1 : 0);
        out.writeInt((null != b) && b ? 1 : 0);
    }

    /**
     * Read a Boolean.  Handle null.
     *
     * @param in stream
     * @return value read
     * @throws IOException
     */
    public static Boolean readBoolean(DataInputStream in) throws IOException {
        boolean b = (1 == in.readInt());
        int i = in.readInt();
        Boolean ret = b ? (1 == i) : null;
        return ret;
    }

    /**
     * Write a String (handle null)
     *
     * @param out stream
     * @param s   value to write
     * @throws IOException
     */
    public static void writeString(DataOutputStream out, String s) throws IOException {
        out.writeUTF((null != s) ? s : "");
    }

    /**
     * Read a String.  Handle null.
     *
     * @param in stream
     * @return value read
     * @throws IOException
     */
    public static String readString(DataInputStream in) throws IOException {
        String ret = in.readUTF();
        return ret;
    }

    /**
     * Get next Long -- if exists
     *
     * @param reader the stream to read
     * @return the long, or null if empty
     */
    public static Long readJSONLong(JsonReader reader) {
        Long l = null;

        try {
            String s = reader.nextString();
            l = Long.parseLong(s);
        } catch (IOException e) {
            Utils.logException(TAG, "", e);
        } catch (NumberFormatException e) {
            // ignore
        }

        return l;
    }

    /**
     * Get next Double -- if exists
     *
     * @param reader the stream to read
     * @return the double, or null if empty
     */
    public static Double readJSONDouble(JsonReader reader) {
        Double d = null;

        try {
            String s = reader.nextString();
            d = Double.parseDouble(s);
        } catch (IOException e) {
            Utils.logException(TAG, "", e);
        } catch (NumberFormatException e) {
            // ignore
        }

        return d;
    }

    /**
     * Get next Integer -- if exists
     *
     * @param reader the stream to read
     * @return the integer, or null if empty
     */
    public static Integer readJSONInteger(JsonReader reader) {
        Integer i = null;

        try {
            String s = reader.nextString();
            i = Integer.parseInt(s);
        } catch (IOException e) {
            Utils.logException(TAG, "", e);
        } catch (NumberFormatException e) {
            // ignore
        }

        return i;
    }

    /**
     * Get next Boolean -- if exists
     *
     * @param reader the stream to read
     * @return the boolean, or null if empty
     */
    public static Boolean readJSONBoolean(JsonReader reader) {
        Boolean b = null;

        try {
            b = reader.nextBoolean(); // Would have liked to use nextString, but get JSON exception.  Weird that doubles and Ints don't then, eh?
        } catch (IOException e) {
            Utils.logException(TAG, "", e);
        }

        return b;
    }

    /**
     * Get next string
     *
     * @param reader JSON reader
     * @return String
     */
    public static String readJSONString(JsonReader reader) {
        String s = "";
        try {
            s = reader.nextString();
        } catch (IOException e) {
            Utils.logException(TAG, "", e);
        }
        return s;
    }


    public enum Justification {TopLeft, TopCenter, TopRight, CenterLeft, CenterCenter, CenterRight, BottomLeft, BottomCenter, BottomRight}
}
