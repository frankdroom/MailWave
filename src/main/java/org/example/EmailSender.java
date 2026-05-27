package org.example;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.FileDataSource;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class EmailSender {

    private final String email;
    private final String appPassword;

    public EmailSender(String email, String appPassword) {
        this.email = email;
        this.appPassword = appPassword;
    }

    public void sendEmail(String to, String subject, String text, List<File> attachments) throws MessagingException {
        Session session = Session.getInstance(getSmtpProperties(), new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, appPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        // Text body
        BodyPart textPart = new MimeBodyPart();
        textPart.setText(text);
        multipart.addBodyPart(textPart);

        // Attachments
        if (attachments != null) {
            for (File file : attachments) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                attachPart.setDataHandler(new DataHandler(source));
                attachPart.setFileName(file.getName());
                multipart.addBodyPart(attachPart);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    private Properties getSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        return props;
    }
}