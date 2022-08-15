package com.deerdevelop.androidmessenger.model;

import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.adapter.active_dialog.ViewHolderFactory;

public class MessageUser implements MessageAdapterType {

    private String messageId, dateSend, textMessage, senderNickName, receiverNickName;
    public MessageUser(String messageId, String dateSend, String textMessage, String senderNickName, String receiverNickName) {
        this.messageId = messageId;
        this.dateSend = dateSend;
        this.textMessage = textMessage;
        this.senderNickName = senderNickName;
        this.receiverNickName = receiverNickName;
    }

    public MessageUser(String textMessage, String senderNickName, String receiverNickName) {
        this.messageId = "unknown";
        this.dateSend = "unknown";
        this.textMessage = textMessage;
        this.senderNickName = senderNickName;
        this.receiverNickName = receiverNickName;
    }

    public MessageUser(Message message) {
        this.messageId = message.messageId;
        this.dateSend = message.dateSend;
        this.textMessage = message.textMessage;
        this.senderNickName = message.senderNickName;
        this.receiverNickName = message.receiverNickName;
    }

    @Override
    public int getItemViewType() {
        return MESSAGE_USER;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        ViewHolderFactory.UserMessageViewHolder userMessageViewHolder = (ViewHolderFactory.UserMessageViewHolder) viewHolder;
        userMessageViewHolder.textViewMessage.setText(textMessage);
        userMessageViewHolder.textViewDate.setText(dateSend);
        userMessageViewHolder.messageId = messageId;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String getDateSend() {
        return dateSend;
    }

    @Override
    public void setDateSend(String dateSend) {
        this.dateSend = dateSend;
    }

    @Override
    public String getReceiverNickName() {
        return receiverNickName;
    }

    @Override
    public void setReceiverNickName(String receiverNickName) {
        this.receiverNickName = receiverNickName;
    }

    @Override
    public String getSenderNickName() {
        return senderNickName;
    }

    @Override
    public void setSenderNickName(String senderNickName) {
        this.senderNickName = senderNickName;
    }

    @Override
    public String getTextMessage() {
        return textMessage;
    }

    @Override
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }
}