package org.example;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Модель письма: значения по умолчанию и защита от null. */
class EmailTest {

    @Test
    void newEmailIsUnreadUnstarredAndHasNoAttachments() {
        Email email = new Email();

        assertFalse(email.isRead());
        assertFalse(email.isStarred());
        assertNotNull(email.getAttachments());
        assertTrue(email.getAttachments().isEmpty());
    }

    @Test
    void shortConstructorKeepsFieldsAndLeavesFlagsOff() {
        Date date = new Date(1_000L);
        FileByteData file = new FileByteData("a.txt", new byte[]{1});

        Email email = new Email("Subject", "alice@example.com", date, "Body", List.of(file));

        assertEquals("Subject", email.getSubject());
        assertEquals("alice@example.com", email.getSender());
        assertEquals(date, email.getSentDate());
        assertEquals("Body", email.getBody());
        assertEquals(List.of(file), email.getAttachments());
        assertFalse(email.isRead());
        assertFalse(email.isStarred());
    }

    @Test
    void fullConstructorSetsFlags() {
        Email email = new Email("s", "f", new Date(), "b", List.of(), true, true);

        assertTrue(email.isRead());
        assertTrue(email.isStarred());
    }

    @Test
    void nullAttachmentsInConstructorBecomeEmptyList() {
        Email email = new Email("s", "f", new Date(), "b", null);

        assertNotNull(email.getAttachments());
        assertTrue(email.getAttachments().isEmpty());
    }

    @Test
    void nullAttachmentsInSetterBecomeEmptyList() {
        Email email = new Email();
        email.setAttachments(List.of(new FileByteData("a.txt", new byte[0])));

        email.setAttachments(null);

        assertTrue(email.getAttachments().isEmpty());
    }

    @Test
    void flagsCanBeToggled() {
        Email email = new Email();

        email.setRead(true);
        email.setStarred(true);
        assertTrue(email.isRead());
        assertTrue(email.isStarred());

        email.setRead(false);
        email.setStarred(false);
        assertFalse(email.isRead());
        assertFalse(email.isStarred());
    }
}
