package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.search.*;
import jakarta.mail.UIDFolder.FetchProfileItem;

public class EmailReceiver {

    private final String email;
    private final String appPassword;

    public EmailReceiver(String email, String appPassword) {
        this.email = email;
        this.appPassword = appPassword;
    }

    public List<Email> readEmails(Date from, Date to) {
        Store store = null;
        Folder folder = null;
        try {
            store = getImapStore();
            folder = getFolderFromStore(store, "INBOX");

            SearchTerm term = buildSearchTerm(from, to);
            Message[] messages = folder.search(term);
            folder.fetch(messages, getFetchProfile());

            List<Email> emails = new ArrayList<>(messages.length);
            for (Message message : messages) {
                emails.add(getEmailFromMessage(message));
            }
            return emails;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeFolder(folder);
            closeStore(store);
        }
        return Collections.emptyList();
    }

    private SearchTerm buildSearchTerm(Date from, Date to) {
        if (from != null && to != null) {
            return new AndTerm(
                    new ReceivedDateTerm(ComparisonTerm.GE, from),
                    new ReceivedDateTerm(ComparisonTerm.LE, to)
            );
        } else if (from != null) {
            return new ReceivedDateTerm(ComparisonTerm.GE, from);
        } else {
            // default: last 24h
            Date yesterday = new Date(new Date().getTime() - (1000L * 60 * 60 * 24));
            return new ReceivedDateTerm(ComparisonTerm.GT, yesterday);
        }
    }

    private Store getImapStore() throws Exception {
        Session session = Session.getInstance(getImapProperties());
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", email, appPassword);
        return store;
    }

    private Properties getImapProperties() {
        Properties props = new Properties();
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.ssl.trust", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.starttls.enable", "true");
        props.put("mail.imaps.connectiontimeout", "15000");
        props.put("mail.imaps.timeout", "15000");
        return props;
    }

    private Folder getFolderFromStore(Store store, String folderName) throws MessagingException {
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);
        return folder;
    }

    private FetchProfile getFetchProfile() {
        FetchProfile fetchProfile = new FetchProfile();
        fetchProfile.add(FetchProfileItem.ENVELOPE);
        fetchProfile.add(FetchProfileItem.CONTENT_INFO);
        fetchProfile.add("X-mailer");
        return fetchProfile;
    }

    private Email getEmailFromMessage(Message message) throws MessagingException, IOException {
        Email email = new Email();
        Address[] from = message.getFrom();
        email.setFrom(from != null ? ((InternetAddress) from[0]).getAddress() : null);
        email.setReceivedOn(message.getReceivedDate());
        email.setSubject(message.getSubject());

        Object content = message.getContent();
        if (content instanceof String) {
            email.getTextBuilder().append((String) content);
        } else if (content instanceof Multipart) {
            processMultipart((Multipart) content, email);
        }
        return email;
    }

    private void processMultipart(Multipart multipart, Email email) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);

            boolean isAttachment = Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                    || (part.getFileName() != null && !part.getFileName().isEmpty());

            if (isAttachment) {
                readAttachment(part, email);
            } else if (part.isMimeType("multipart/*")) {
                processMultipart((Multipart) part.getContent(), email);
            } else if (part.isMimeType("text/plain")) {
                email.getTextBuilder().append(part.getContent().toString());
            }
        }
    }

    private void readAttachment(BodyPart part, Email email) throws MessagingException, IOException {
        String filename = part.getFileName();
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            email.getAttachments().add(new FileByteData(filename, baos.toByteArray()));
        }
    }

    private void closeFolder(Folder folder) {
        if (folder != null && folder.isOpen()) {
            try { folder.close(true); } catch (MessagingException ignored) {}
        }
    }

    private void closeStore(Store store) {
        if (store != null && store.isConnected()) {
            try { store.close(); } catch (MessagingException ignored) {}
        }
    }
}