package org.example;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class EmailSenderMessageTest {

    private static final Session SESSION = Session.getInstance(new Properties());
    private final EmailSender sender = new EmailSender("smtp.example.com", "587", "me@example.com", "secret");

    @Test
    void setsSenderRecipientAndSubject() throws Exception {
        Message message = sender.buildMessage(SESSION, "bob@example.com", "Hi", "Body", null);

        assertEquals("me@example.com", message.getFrom()[0].toString());
        assertEquals("bob@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Hi", message.getSubject());
    }

    @Test
    void messageWithoutAttachmentsHasOnlyTheTextPart() throws Exception {
        Message message = sender.buildMessage(SESSION, "bob@example.com", "Hi", "Body", List.of());

        assertEquals(1, ((MimeMultipart) message.getContent()).getCount());
    }

    @Test
    void nullBodyBecomesEmptyText() throws Exception {
        Message message = sender.buildMessage(SESSION, "bob@example.com", "Hi", null, null);

        assertEquals(1, ((MimeMultipart) message.getContent()).getCount());
    }

    @Test
    void addsEachExistingFileAsAnAttachment(@TempDir Path dir) throws Exception {
        File first = Files.writeString(dir.resolve("a.txt"), "aaa").toFile();
        File second = Files.writeString(dir.resolve("b.txt"), "bbb").toFile();

        Message message = sender.buildMessage(SESSION, "bob@example.com", "Hi", "Body",
                List.of(first.getAbsolutePath(), second.getAbsolutePath()));

        MimeMultipart content = (MimeMultipart) message.getContent();
        assertEquals(3, content.getCount());
        assertEquals("a.txt", content.getBodyPart(1).getFileName());
        assertEquals("b.txt", content.getBodyPart(2).getFileName());
    }

    @Test
    void emptyRecipientIsRejected() {
        assertThrows(AddressException.class,
                () -> sender.buildMessage(SESSION, "", "Hi", "Body", null));
    }
}
