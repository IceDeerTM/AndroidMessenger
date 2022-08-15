package com.deerdevelop.androidmessenger.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.adapter.active_dialog.ViewHolderFactory;
import com.deerdevelop.androidmessenger.adapter.dialog_preview.DialogPreviewAdapter;

public class DialogPreview {
    private String lastMessage, displayName, nickName, dateSend;
    private Bitmap photo;
    private boolean isOnline = false;

    public DialogPreview(String lastMessage, String displayName, String nickName, String dateSend, Bitmap photo) {
        this.lastMessage = lastMessage;
        this.displayName = displayName;
        this.photo = photo;
        this.nickName = nickName;
        this.dateSend = dateSend;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        DialogPreviewAdapter.DialogItemViewHolder dialogItemViewHolder = (DialogPreviewAdapter.DialogItemViewHolder) viewHolder;
        dialogItemViewHolder.dateSendTextView.setText(dateSend);
        dialogItemViewHolder.displayNameTextView.setText(displayName);
        dialogItemViewHolder.messageTextView.setText(lastMessage);
        dialogItemViewHolder.photoImageView.setImageBitmap(photo);
        dialogItemViewHolder.nickName = nickName;

        if (isOnline) dialogItemViewHolder.onlineStatus.setVisibility(View.VISIBLE);
        else dialogItemViewHolder.onlineStatus.setVisibility(View.INVISIBLE);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDateSend() {
        return dateSend;
    }

    public void setDateSend(String dateSend) {
        this.dateSend = dateSend;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
