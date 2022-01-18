package com.example.wechat.chat;

public class MessageModel {
    private String message;

    private String messageFrom;

    private long messageTime;

    private String messageType;

    private String message_id;

    public MessageModel() {
    }



    public MessageModel(String message, String messageFrom, long messageTime, String messageType, String message_id, String chatUserId) {
        this.message = message;
        this.messageFrom = messageFrom;
        this.messageTime = messageTime;
        this.messageType = messageType;
        this.message_id = message_id;

    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }


}
