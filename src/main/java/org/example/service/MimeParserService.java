package org.example.service;

import org.example.Email;
import org.example.FileByteData;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MimeParserService {

    public Email parseMessage(MimeMessage message) throws MessagingException, IOException {
        String subject = message.getSubject();
        String from = getAddressString(message.getFrom());
        Date sentDate = message.getSentDate();

        StringBuilder bodyBuilder = new StringBuilder();
        List<FileByteData> attachments = new ArrayList<>();

        if (message.isMimeType("text/plain")) {
            bodyBuilder.append(message.getContent().toString());
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            parseMultipart(multipart, bodyBuilder, attachments);
        }

        return new Email(
                subject != null ? subject : "(Без темы)",
                from != null ? from : "Неизвестный отправитель",
                sentDate != null ? sentDate : new Date(),
                bodyBuilder.toString(),
                attachments
        );
    }

    private void parseMultipart(Multipart multipart, StringBuilder bodyBuilder, List<FileByteData> attachments)
            throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                String fileName = bodyPart.getFileName();
                try (InputStream is = bodyPart.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                    byte[] data = is.readAllBytes();
                    attachments.add(new FileByteData(fileName, data));
                }
            } else if (bodyPart.isMimeType("text/plain")) {
                bodyBuilder.append(bodyPart.getContent().toString());
            } else if (bodyPart.isMimeType("multipart/*")) {
                parseMultipart((Multipart) bodyPart.getContent(), bodyBuilder, attachments);
            }
        }
    }

    private String getAddressString(Address[] addresses) {
        if (addresses == null || addresses.length == 0) return null;
        return addresses[0].toString();
    }
}