package org.example.service;

import jakarta.mail.MessagingException;
import org.example.Email;
import org.example.EmailReceiver;
import org.example.EmailSender;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** MailService проверяется на подделках приёмника и отправителя, без сети. */
class MailServiceTest {

    private final FakeReceiver receiver = new FakeReceiver();
    private final FakeSender sender = new FakeSender();
    private final MailService service =
            new MailService(receiver, sender, new MimeParserService(), new EmailSearchService());

    @Test
    void fetchReturnsEmailsFromReceiver() throws Exception {
        Email a = email("A");
        Email b = email("B");
        receiver.emails = List.of(a, b);

        assertEquals(List.of(a, b), service.fetchEmails());
        assertEquals(1, receiver.fetchCalls);
    }

    @Test
    void secondFetchReplacesTheCacheInsteadOfAppending() throws Exception {
        receiver.emails = List.of(email("old 1"), email("old 2"));
        service.fetchEmails();

        Email fresh = email("fresh");
        receiver.emails = List.of(fresh);
        service.fetchEmails();

        assertEquals(List.of(fresh), service.filterEmails(""));
    }

    @Test
    void returnedListIsACopyOfTheCache() throws Exception {
        receiver.emails = List.of(email("A"), email("B"));

        List<Email> returned = service.fetchEmails();
        returned.clear();

        assertEquals(2, service.filterEmails("").size());
    }

    @Test
    void failedFetchKeepsPreviouslyLoadedEmails() throws Exception {
        Email kept = email("kept");
        receiver.emails = List.of(kept);
        service.fetchEmails();

        receiver.failure = new MessagingException("connection lost");
        assertThrows(MessagingException.class, service::fetchEmails);

        assertEquals(List.of(kept), service.filterEmails(""));
    }

    @Test
    void filterBeforeFirstFetchIsEmpty() {
        assertTrue(service.filterEmails("").isEmpty());
        assertTrue(service.filterEmails("anything").isEmpty());
    }

    @Test
    void filterSearchesTheCachedEmails() throws Exception {
        Email invoice = email("Invoice");
        Email lunch = email("Lunch");
        receiver.emails = List.of(invoice, lunch);
        service.fetchEmails();

        assertEquals(List.of(invoice), service.filterEmails("invoice"));
        assertEquals(List.of(invoice, lunch), service.filterEmails(""));
        assertTrue(service.filterEmails("missing").isEmpty());
    }

    @Test
    void sendPassesAllArgumentsToTheSender() throws Exception {
        List<String> files = List.of("/tmp/a.txt");

        service.sendEmail("bob@example.com", "Hi", "Body", files);

        assertEquals(1, sender.sent.size());
        assertEquals("bob@example.com", sender.sent.get(0).to);
        assertEquals("Hi", sender.sent.get(0).subject);
        assertEquals("Body", sender.sent.get(0).body);
        assertEquals(files, sender.sent.get(0).attachments);
    }

    @Test
    void senderFailurePropagates() {
        sender.failure = new IllegalStateException("smtp down");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.sendEmail("bob@example.com", "Hi", "Body", List.of()));

        assertEquals("smtp down", error.getMessage());
    }

    @Test
    void testConnectionChecksCredentialsWithoutDownloadingEmails() throws Exception {
        service.testConnection();

        assertEquals(1, receiver.connectionChecks);
        assertEquals(0, receiver.fetchCalls);
    }

    @Test
    void testConnectionFailurePropagates() {
        receiver.failure = new MessagingException("Authentication failed");

        MessagingException error = assertThrows(MessagingException.class, service::testConnection);

        assertEquals("Authentication failed", error.getMessage());
    }

    private static Email email(String subject) {
        return new Email(subject, "sender@example.com", new Date(), "body", List.of());
    }

    private static class FakeReceiver extends EmailReceiver {
        List<Email> emails = new ArrayList<>();
        MessagingException failure;
        int fetchCalls;
        int connectionChecks;

        FakeReceiver() {
            super("imap.test", "993", "user", "pass");
        }

        @Override
        public List<Email> receiveEmails(MimeParserService parserService) throws MessagingException {
            fetchCalls++;
            if (failure != null) throw failure;
            return new ArrayList<>(emails);
        }

        @Override
        public void testConnection() throws MessagingException {
            connectionChecks++;
            if (failure != null) throw failure;
        }
    }

    private static class FakeSender extends EmailSender {
        final List<Sent> sent = new ArrayList<>();
        RuntimeException failure;

        FakeSender() {
            super("smtp.test", "587", "user", "pass");
        }

        @Override
        public void send(String to, String subject, String bodyText, List<String> attachmentPaths) {
            if (failure != null) throw failure;
            sent.add(new Sent(to, subject, bodyText, attachmentPaths));
        }
    }

    private static class Sent {
        final String to, subject, body;
        final List<String> attachments;

        Sent(String to, String subject, String body, List<String> attachments) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.attachments = attachments;
        }
    }
}
