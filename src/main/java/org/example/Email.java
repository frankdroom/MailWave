package org.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email {
    private String from;
    private String to;
    private String subject;
    private Date receivedOn;
    private StringBuilder textBuilder = new StringBuilder();
    private List<FileByteData> attachments = new ArrayList<>();
    private boolean read = false;
    private boolean starred = false;

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Date getReceivedOn() { return receivedOn; }
    public void setReceivedOn(Date receivedOn) { this.receivedOn = receivedOn; }

    public StringBuilder getTextBuilder() { return textBuilder; }
    public String getText() { return textBuilder.toString(); }

    public List<FileByteData> getAttachments() { return attachments; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }

    @Override
    public String toString() {
        return String.format("RECEIVED ON: %s%nFROM: %s%nSUBJECT: %s%nNUMBER OF ATTACHMENTS: %d%nTEXT: %s",
                receivedOn, from, subject, attachments.size(), textBuilder);
    }
}