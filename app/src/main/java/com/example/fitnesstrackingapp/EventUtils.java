package com.example.fitnesstrackingapp;

import com.example.fitnesstrackingapp.Event;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventUtils {
    /**
     * Sắp xếp danh sách tasks theo timestamp.
     * @param events     Danh sách Task cần sắp xếp.
     * @param ascending true → cũ → mới; false → mới → cũ.
     */
    public static void sortByTimestamp(List<Event> events, boolean ascending) {
        Comparator<Event> comparator = Comparator.comparingLong(Event::getTimestamp);
        if (!ascending) {
            comparator = comparator.reversed();  // đảo thứ tự để mới nhất lên đầu :contentReference[oaicite:3]{index=3}
        }
        Collections.sort(events, comparator);      // sử dụng Collections.sort với Comparator :contentReference[oaicite:4]{index=4}
    }
}


