package com.conductor.aeroturex.dummy;

import android.util.Log;

import com.conductor.aeroturex.Inicio_sesion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContentServicios {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    private static final Map<String, DummyItem> ITEM_MAP = new HashMap<>();

    static {
        log("DummyContentServicios");
        try {
            JSONArray jArr = Inicio_sesion.datasource.getAllRegs("servicio");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jObj = jArr.getJSONObject(i);
                log("servicios : " + jObj.toString());
                addItem(createDummyItem(i, jObj), jObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void addItem(DummyItem item, JSONObject jObj) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position, JSONObject jObj) {
        String direccion = "";
        try {
            direccion = jObj.getString("dir");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new DummyItem(String.valueOf(position), (position + 1) + ". " + direccion, makeDetails(position, jObj));
    }

    private static String makeDetails(int position, JSONObject jObj) {
        String builder = "";

        try {
            builder = jObj.getString("barrio");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return builder;
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
            log("DummyItem");
        }

        @Override
        public String toString() {
            return content;
        }
    }

    private static void log(String s) {
        Log.d(DummyContentServicios.class.getSimpleName(), "######" + s + "######");
    }
}
