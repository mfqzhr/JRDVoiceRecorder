package com.tyjradio.jrdvoicerecorder.bean;


import com.tyjradio.jrdvoicerecorder.bean.MessageBody;
import com.alibaba.fastjson.annotation.JSONField;

public class MessageBean {


    private int MessageType = 0;
    private int Direction = 1;
    private MessageBody messageBody = null;

    @JSONField(name="MessageType")
    public int getMessageType() {
        return MessageType;
    }

    @JSONField(name="MessageType")
    public void setMessageType(int messageType) {
        MessageType = messageType;
    }

    @JSONField(name="Direction")
    public int getDirection() {
        return Direction;
    }

    @JSONField(name="Direction")
    public void setDirection(int direction) {
        Direction = direction;
    }

    @JSONField(name="MessageBody")
    public MessageBody getMessageBody() {
        return messageBody;
    }

    @JSONField(name="MessageBody")
    public void setMessageBody(MessageBody pmessageBody) {
        messageBody = pmessageBody;
    }





}
