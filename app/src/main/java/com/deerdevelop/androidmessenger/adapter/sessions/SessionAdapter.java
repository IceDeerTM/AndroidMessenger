package com.deerdevelop.androidmessenger.adapter.sessions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.model.Session;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionItemViewHolder> {

    private ArrayList<Session> sessions;

    public SessionAdapter(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @NotNull
    @Override
    public SessionAdapter.SessionItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_item, parent, false);
        return new SessionAdapter.SessionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SessionAdapter.SessionItemViewHolder holder, int position) {
        sessions.get(position).onBindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public final class SessionItemViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewDeviceInfo;
        public ImageView imageViewDevice;
        public String token;

        public SessionItemViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textViewDeviceInfo = itemView.findViewById(R.id.textViewDeviceInfo);
            imageViewDevice = itemView.findViewById(R.id.imageViewDevice);
            token = "";
        }
    }
}