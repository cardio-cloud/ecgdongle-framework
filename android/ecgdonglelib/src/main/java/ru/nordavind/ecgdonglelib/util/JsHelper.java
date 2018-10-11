package ru.nordavind.ecgdonglelib.util;

import org.json.JSONArray;
import org.json.JSONException;

import ru.nordavind.ecgdonglelib.scan.Lead;
import ru.nordavind.ecgdonglelib.storage.mitbih.MitBihFormat;

/**
 * Created by babay1 on 26.07.2015.
 */
public class JsHelper {

    public static JSONArray toJSA(int[] arr) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arr.length; i++) {
            jsonArray.put(arr[i]);
        }
        return jsonArray;
    }

    public static JSONArray toJSA(long[] arr) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arr.length; i++) {
            jsonArray.put(arr[i]);
        }
        return jsonArray;
    }

    public static JSONArray toJSA(String[] arr) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arr.length; i++) {
            jsonArray.put(arr[i]);
        }
        return jsonArray;
    }

    public static JSONArray toJSA(Lead[] arr) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arr.length; i++) {
            jsonArray.put(arr[i].name());
        }
        return jsonArray;
    }

    public static JSONArray toJSA(MitBihFormat[] arr) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < arr.length; i++) {
            jsonArray.put(arr[i].getName());
        }
        return jsonArray;
    }


    public static int[] intsFromJSA(JSONArray src) throws JSONException {
        int[] res = new int[src.length()];
        for (int i = 0; i < src.length(); i++) {
            res[i] = src.getInt(i);
        }
        return res;
    }

    public static long[] longsFromJSA(JSONArray src) throws JSONException {
        long[] res = new long[src.length()];
        for (int i = 0; i < src.length(); i++) {
            res[i] = src.getLong(i);
        }
        return res;
    }

    public static String[] stringsFromJSA(JSONArray src) throws JSONException {
        String[] res = new String[src.length()];
        for (int i = 0; i < src.length(); i++) {
            res[i] = src.getString(i);
        }
        return res;
    }

    /**
     * reads mit-bih format from JsonArray
     *
     * @param src
     * @return
     * @throws JSONException
     */
    public static MitBihFormat[] mbfsFromJSA(JSONArray src) throws JSONException {
        MitBihFormat[] res = new MitBihFormat[src.length()];
        for (int i = 0; i < src.length(); i++) {
            res[i] = MitBihFormat.getByName(src.getString(i));
        }
        return res;
    }
}
