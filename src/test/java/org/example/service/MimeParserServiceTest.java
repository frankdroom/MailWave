package org.example.service;

import jakarta.activation.DataHandler;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.example.Email;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Настоящие MIME-письма собираются в памяти (без сети), сериализуются и разбираются заново —
 * так же, как письма, пришедшие с сервера.
 */
class MimeParserServiceTest {

    private static final Session SESSION = Session.getInstance(new Properties());
    private final MimeParserService parser = new MimeParserService();

    @Test
    void parsesSimpleTextMessage() throws Exception {
        MimeMessage message = new MimeMessage(SESSION);
        message.setFrom(new InternetAddress("alice@example.com"));
        message.setSubject("Hello");
        message.setText("Just text", "UTF-8");

        Email email = parser.parseMessage(roundTrip(message));

        assertEquals("Hello", email.getSubject());
        assertEquals("alice@example.com", email.getSender());
        assertEquals("Just text", email.getBody().trim());
        assertTrue(email.getAttachments().isEmpty());
    }

    @Test
    void missingSubjectGetsPlaceholder() throws Exception {
        MimeMessage message = new MimeMessage(SESSION);
        message.setFrom(new InternetAddress("alice@example.com"));
        message.setText("body", "UTF-8");

        Email email = parser.parseMessage(roundTrip(message));

        assertEquals("(Без темы)", email.getSubject());
    }

    @Test
    void missingSenderGetsPlaceholder() throws Exception {
        MimeMessage message = new MimeMessage(SESSION);
        message.setSubject("No sender");
        message.setText("body", "UTF-8");

        Email email = parser.parseMessage(roundTrip(message));

        assertEquals("Неизвестный отправитель", email.getSender());
    }

    @Test
    void missingDateFallsBackToNow() throws Exception {
        MimeMessage message = new MimeMessage(SESSION);
        message.setSubject("No date");
        message.setText("body", "UTF-8");

        Email email = parser.parseMessage(roundTrip(message));

        assertNotNull(email.getSentDate());
    }

    @Test
    void extractsTextAndAttachmentFromMultipart() throws Exception {
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(textPart("Letter text"));
        mixed.addBodyPart(attachmentPart("report.txt", new byte[]{10, 20, 30}));

        Email email = parser.parseMessage(roundTrip(messageWith(mixed)));

        assertEquals("Letter text", email.getBody().trim());
        assertEquals(1, email.getAttachments().size());
        assertEquals("report.txt", email.getAttachments().get(0).getFileName());
        assertArrayEquals(new byte[]{10, 20, 30}, email.getAttachments().get(0).getData());
    }

    @Test
    void extractsSeveralAttachmentsInOrder() throws Exception {
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(textPart("see files"));
        mixed.addBodyPart(attachmentPart("one.bin", new byte[]{1}));
        mixed.addBodyPart(attachmentPart("two.bin", new byte[]{2, 2}));

        Email email = parser.parseMessage(roundTrip(messageWith(mixed)));

        assertEquals(2, email.getAttachments().size());
        assertEquals("one.bin", email.getAttachments().get(0).getFileName());
        assertEquals("two.bin", email.getAttachments().get(1).getFileName());
    }

    @Test
    void walksNestedMultiparts() throws Exception {
        MimeMultipart alternative = new MimeMultipart("alternative");
        alternative.addBodyPart(textPart("nested text"));
        MimeBodyPart wrapper = new MimeBodyPart();
        wrapper.setContent(alternative);

        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(wrapper);
        mixed.addBodyPart(attachmentPart("a.bin", new byte[]{7}));

        Email email = parser.parseMessage(roundTrip(messageWith(mixed)));

        assertEquals("nested text", email.getBody().trim());
        assertEquals(1, email.getAttachments().size());
    }

    @Test
    void joinsAllTextParts() throws Exception {
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(textPart("first part"));
        mixed.addBodyPart(textPart("second part"));

        Email email = parser.parseMessage(roundTrip(messageWith(mixed)));

        assertTrue(email.getBody().contains("first part"));
        assertTrue(email.getBody().contains("second part"));
    }

    @Test
    void keepsNonAsciiText() throws Exception {
        MimeMessage message = new MimeMessage(SESSION);
        message.setSubject("Тема письма", "UTF-8");
        message.setText("Привет, мир!", "UTF-8");

        Email email = parser.parseMessage(roundTrip(message));

        assertEquals("Тема письма", email.getSubject());
        assertEquals("Привет, мир!", email.getBody().trim());
    }

    private static MimeMessage messageWith(MimeMultipart content) throws Exception {
        MimeMessage message = new MimeMessage(SESSION);
        message.setFrom(new InternetAddress("sender@example.com"));
        message.setSubject("multipart");
        message.setContent(content);
        return message;
    }

    private static MimeBodyPart textPart(String text) throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setText(text, "UTF-8", "plain");
        return part;
    }

    private static MimeBodyPart attachmentPart(String fileName, byte[] data) throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        part.setDataHandler(new DataHandler(new ByteArrayDataSource(data, "application/octet-stream")));
        part.setDisposition(Part.ATTACHMENT);
        part.setFileName(fileName);
        return part;
    }

    /** Сериализует и разбирает письмо заново, как будто оно пришло с сервера. */
    private static MimeMessage roundTrip(MimeMessage original) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.writeTo(out);
        return new MimeMessage(SESSION, new ByteArrayInputStream(out.toByteArray()));
    }
}
