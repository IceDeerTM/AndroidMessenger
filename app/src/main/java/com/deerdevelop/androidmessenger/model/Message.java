package com.deerdevelop.androidmessenger.model;

public class Message {
    protected String messageId, dateSend, textMessage, senderNickName, receiverNickName;

    public Message(String messageId, String dateSend, String textMessage, String senderNickName, String receiverNickName) {
        this.messageId = messageId;
        this.dateSend = dateSend;
        this.textMessage = textMessage;
        this.senderNickName = senderNickName;
        this.receiverNickName = receiverNickName;
    }

    public Message(Message message) {
        this.messageId = message.messageId;
        this.dateSend = message.dateSend;
        this.textMessage = message.textMessage;
        this.senderNickName = message.senderNickName;
        this.receiverNickName = message.receiverNickName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDateSend() {
        return dateSend;
    }

    public void setDateSend(String dateSend) {
        this.dateSend = dateSend;
    }

    public String getReceiverNickName() {
        return receiverNickName;
    }

    public void setReceiverNickName(String receiverNickName) {
        this.receiverNickName = receiverNickName;
    }

    public String getSenderNickName() {
        return senderNickName;
    }

    public void setSenderNickName(String senderNickName) {
        this.senderNickName = senderNickName;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }
}
