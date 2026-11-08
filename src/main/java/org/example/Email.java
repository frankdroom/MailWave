package org.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email {
    private String subject;
    private String sender;
    private Date sentDate;
    private String body;
    private List<FileByteData> attachments;
    private boolean read;
    private boolean starred;


    public Email() {
        this.attachments = new ArrayList<>();
        this.read = false;
        this.starred = false;
    }

    public Email(String subject, String sender, Date sentDate, String body, List<FileByteData> attachments) {
        this(subject, sender, sentDate, body, attachments, false, false);
    }


    public Email(String subject, String sender, Date sentDate, String body, List<FileByteData> attachments, boolean read, boolean starred) {
        this.subject = subject;
        this.sender = sender;
        this.sentDate = sentDate;
        this.body = body;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        this.read = read;
        this.starred = starred;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<FileByteData> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileByteData> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }
}