package com.example.fitnesstrackingapp;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.util.*;

public class InternalStorageHelper {
    private static final String FILE_NAME = "events.json";
    private Context context;

    public InternalStorageHelper(Context context) {
        this.context = context;
    }

    // Đọc & sắp xếp event theo timestamp giảm dần :contentReference[oaicite:11]{index=11}
    public List<Event> readEvents() {
        List<Event> events = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                events.add(new Event(o.getString("id"), o.getString("title"),
                        o.getString("time"), o.getLong("timestamp")));
            }
            // sort by timestamp desc
            Collections.sort(events, (a,b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        } catch (FileNotFoundException e) {
            // không có file → trả về rỗng
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }
        return events;
    }

    public void saveEvents(List<Event> events) {
        JSONArray arr = new JSONArray();
        try {
            for (Event t : events) {
                JSONObject o = new JSONObject();
                o.put("id", t.getId());
                o.put("title", t.getTitle());
                o.put("time", t.getTime());
                o.put("timestamp", t.getTimestamp());
                arr.put(o);
            }
            try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
                fos.write(arr.toString().getBytes());
            }
        } catch (IOException|JSONException e) {
            e.printStackTrace();
        }
    }

    // Xóa event cục bộ
    public void deleteEvent(String id) {
        List<Event> events = readEvents();
        Iterator<Event> it = events.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(id)) {
                it.remove();
                break;
            }
        }
        saveEvents(events);
    }
}


