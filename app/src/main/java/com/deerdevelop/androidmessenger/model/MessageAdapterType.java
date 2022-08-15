package com.deerdevelop.androidmessenger.model;

import androidx.recyclerview.widget.RecyclerView;

public interface MessageAdapterType {
    int MESSAGE_USER = 0;
    int MESSAGE_INTERLOCUTOR = 1;

    int getItemViewType();

    void onBindViewHolder(RecyclerView.ViewHolder viewHolder);

    String getMessageId();

    void setMessageId(String messageId);

    String getDateSend();

    void setDateSend(String dateSend);

    String getReceiverNickName();

    void setReceiverNickName(String receiverNickName);

    String getSenderNickName();

    void setSenderNickName(String senderNickName);

    String getTextMessage();

    void setTextMessage(String textMessage);
}
