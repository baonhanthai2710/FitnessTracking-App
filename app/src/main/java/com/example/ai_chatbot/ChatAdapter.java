package com.example.ai_chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final List<ChatMessage> data;
    public ChatAdapter(List<ChatMessage> data) {
        this.data = data;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        // Chọn layout và ID TextView tương ứng
        int layout = viewType == 0 ? R.layout.item_user : R.layout.item_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v, viewType);
    }

    @Override
    public void onBindViewHolder(VH holder, int pos) {
        ChatMessage msg = data.get(pos);
        holder.tv.setText(msg.getContent());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int pos) {
        return data.get(pos).isUser() ? 0 : 1;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(View itemView, int viewType) {
            super(itemView);
            // Chỉ định đúng ID TextView
            tv = viewType == 0
                    ? itemView.findViewById(R.id.text_user)
                    : itemView.findViewById(R.id.text_ai);
        }
    }
}
