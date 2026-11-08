package org.example;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Без сети: проверяется расчёт диапазона писем и настройки соединения. */
class EmailReceiverTest {

    @Test
    void takesOnlyTheLastMessagesOfALargeMailbox() {
        assertEquals(401, EmailReceiver.firstMessageIndex(500, 100));
        assertEquals(9_901, EmailReceiver.firstMessageIndex(10_000, 100));
        assertEquals(2, EmailReceiver.firstMessageIndex(101, 100));
    }

    @Test
    void smallMailboxIsLoadedFromTheFirstMessage() {
        assertEquals(1, EmailReceiver.firstMessageIndex(100, 100));
        assertEquals(1, EmailReceiver.firstMessageIndex(37, 100));
        assertEquals(1, EmailReceiver.firstMessageIndex(1, 100));
    }

    @Test
    void defaultLimitIsOneHundred() {
        assertEquals(100, EmailReceiver.DEFAULT_MAX_MESSAGES);
    }

    @Test
    void usesImapsWithSslOnTheGivenHostAndPort() {
        Properties p = new EmailReceiver("imap.gmail.com", "993", "user", "pass").buildProperties();

        assertEquals("imaps", p.getProperty("mail.store.protocol"));
        assertEquals("imap.gmail.com", p.getProperty("mail.imaps.host"));
        assertEquals("993", p.getProperty("mail.imaps.port"));
        assertEquals("true", p.getProperty("mail.imaps.ssl.enable"));
    }

    @Test
    void hasTimeoutsSoTheConnectionNeverHangsForever() {
        Properties p = new EmailReceiver("h", "993", "u", "p").buildProperties();

        assertEquals("10000", p.getProperty("mail.imaps.connectiontimeout"));
        assertEquals("10000", p.getProperty("mail.imaps.timeout"));
        assertEquals("10000", p.getProperty("mail.imaps.writetimeout"));
    }

    @Test
    void readsMessageBodiesInLargeChunks() {
        Properties p = new EmailReceiver("h", "993", "u", "p").buildProperties();

        assertEquals("1048576", p.getProperty("mail.imaps.fetchsize"));
    }
}
