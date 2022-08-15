package com.deerdevelop.androidmessenger.adapter.active_dialog;

import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.CallBackLongClickListener;
import com.deerdevelop.androidmessenger.model.MessageAdapterType;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {
    private ArrayList<MessageAdapterType> messages;
    public static CallBackLongClickListener callBackLongClickListener;

    public MessageAdapter(ArrayList<MessageAdapterType> messages, CallBackLongClickListener _callBackLongClickListener) {
        this.messages = messages;
        callBackLongClickListener = _callBackLongClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getItemViewType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolderFactory.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        messages.get(position).onBindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}