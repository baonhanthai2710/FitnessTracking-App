package com.example.fitnesstrackingapp;

import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;
import android.widget.Filter;
import android.widget.Filterable;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>
        implements Filterable {
    private List<Event> events, eventsFull;
    private OnEventActionListener actionListener;

    public interface OnEventActionListener {
        void onDelete(Event event);
    }

    public EventAdapter(List<Event> events, OnEventActionListener listener) {
        this.events = new ArrayList<>(events);
        this.eventsFull = new ArrayList<>(events);
        this.actionListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTime;
        ImageButton btnDelete;
        public ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDesc  = v.findViewById(R.id.tvDesc);
            tvTime  = v.findViewById(R.id.tvTime);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
    public void removeEvent(Event event) {
        int pos = events.indexOf(event);
        if (pos != -1) {
            events.remove(pos);
            notifyItemRemoved(pos);  // chỉ thông báo vị trí bị xóa :contentReference[oaicite:2]{index=2}
        }
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_event, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Event t = events.get(pos);
        h.tvTitle.setText(t.getTitle());
        h.tvDesc .setText(t.getDescription());
        h.tvTime .setText(new SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(new Date(t.getTimestamp())));
        h.btnDelete.setOnClickListener(v -> actionListener.onDelete(t));
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
            intent.putExtra("event", events.get(pos));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return events.size(); }

    // Filter theo title :contentReference[oaicite:12]{index=12}
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence cs) {
                List<Event> filtered = new ArrayList<>();
                if (cs == null || cs.length() == 0) {
                    filtered.addAll(eventsFull);
                } else {
                    String pat = cs.toString().toLowerCase().trim();
                    for (Event t : eventsFull) {
                        if (t.getTitle().toLowerCase().contains(pat)) {
                            filtered.add(t);
                        }
                    }
                }
                FilterResults r = new FilterResults();
                r.values = filtered;
                return r;
            }
            @Override protected void publishResults(CharSequence cs, FilterResults r) {
                events.clear();
                events.addAll((List<Event>) r.values);
                notifyDataSetChanged();
            }
        };
    }
}

