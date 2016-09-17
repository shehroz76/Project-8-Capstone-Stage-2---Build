package com.pradeep.myreddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Helpers {

    public static String stringifyParams(Map<String, String> params) {
        int pos = 0;

        String paramsString = "";

        for (String key : params.keySet()) {
            if (pos == 0) {
                paramsString  += key + "=" + params.get(key);
            } else {
                paramsString  += "&" + key + "=" + params.get(key);
            }

            pos++;
        }

        return paramsString;
    }

    public static Map<String, String> parseUrlQueryParams(String url) {
        url = url.substring(url.indexOf("?") + 1, url.length());
        String[] split = url.split("&");

        Map<String, String> params = new HashMap<String, String>();
        for (String item : split) {
            String[] split2 = item.split("=");
            params.put(split2[0], split2[1]);
        }

        return params;
    }

    public static void writeToPrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(key, value).apply();
    }

    public static String readFromPrefs(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, null);
    }

    public static void clearPrefs(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }

    public static String humanizeTimestamp(String timestampSeconds) {
        Date createdAt = new Date(Double.valueOf(timestampSeconds).longValue() * 1000);
        long elapsed = System.currentTimeMillis() - createdAt.getTime();

        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        long diffHours = TimeUnit.MILLISECONDS.toHours(elapsed);
        long diffDays = TimeUnit.MILLISECONDS.toDays(elapsed);
        long diffWeeks = diffDays / 7;

        if (diffWeeks > 0) {
            return diffWeeks + "w";
        }
        else if (diffDays > 0) {
            return diffDays + "d";
        }
        else if (diffHours > 0) {
            return diffHours + "h";
        }
        else if (diffMinutes > 0) {
            return diffMinutes + "m";
        }
        else {
            return diffSeconds + "s";
        }
    }

    public static float convertDpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static CharSequence trimTrailingWhitespace(CharSequence text) {
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.subSequence(0, text.length() - 1);
        }

        return text;
    }
}
