package com.deerdevelop.androidmessenger.model;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.adapter.dialog_preview.DialogPreviewAdapter;
import com.deerdevelop.androidmessenger.adapter.sessions.SessionAdapter;

public class Session {
    private String deviceInfo, tokenOfDevice;

    public Session(String deviceInfo, String tokenOfDevice) {
        this.deviceInfo = deviceInfo;
        this.tokenOfDevice = tokenOfDevice;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        SessionAdapter.SessionItemViewHolder sessionItemViewHolder = (SessionAdapter.SessionItemViewHolder) viewHolder;
        sessionItemViewHolder.textViewDeviceInfo.setText(deviceInfo);
        sessionItemViewHolder.token = tokenOfDevice;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getTokenOfDevice() {
        return tokenOfDevice;
    }

    public void setTokenOfDevice(String tokenOfDevice) {
        this.tokenOfDevice = tokenOfDevice;
    }
}
