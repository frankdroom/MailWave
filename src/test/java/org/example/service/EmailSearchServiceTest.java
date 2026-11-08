package org.example.service;

import org.example.Email;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailSearchServiceTest {

    private final EmailSearchService service = new EmailSearchService();

    private final Email invoice = email("Invoice March", "alice@example.com", "Please pay until Friday");
    private final Email lunch = email("Lunch?", "bob@example.com", "Pizza at noon");
    private final Email russian = email("Привет", "carol@example.com", "Встреча в ПЯТНИЦУ");
    private final List<Email> all = List.of(invoice, lunch, russian);

    @Test
    void emptyOrNullListGivesEmptyResult() {
        assertTrue(service.search(null, "x").isEmpty());
        assertTrue(service.search(List.of(), "x").isEmpty());
    }

    @Test
    void blankQueryReturnsEverything() {
        assertEquals(all, service.search(all, null));
        assertEquals(all, service.search(all, ""));
        assertEquals(all, service.search(all, "   "));
    }

    @Test
    void matchesSubject() {
        assertEquals(List.of(invoice), service.search(all, "invoice"));
    }

    @Test
    void matchesSender() {
        assertEquals(List.of(lunch), service.search(all, "bob@"));
    }

    @Test
    void matchesBody() {
        assertEquals(List.of(lunch), service.search(all, "pizza"));
    }

    @Test
    void isCaseInsensitive() {
        assertEquals(List.of(invoice), service.search(all, "INVOICE MARCH"));
        assertEquals(List.of(russian), service.search(all, "привет"));
        assertEquals(List.of(russian), service.search(all, "пятницу"));
    }

    @Test
    void ignoresSpacesAroundTheQuery() {
        assertEquals(List.of(lunch), service.search(all, "   pizza  "));
    }

    @Test
    void returnsAllMatchesInOriginalOrder() {
        // "friday" есть только в invoice, а "example.com" — в отправителях всех писем
        assertEquals(all, service.search(all, "example.com"));
        assertEquals(List.of(invoice), service.search(all, "friday"));
    }

    @Test
    void noMatchGivesEmptyList() {
        assertTrue(service.search(all, "nothing like this").isEmpty());
    }

    @Test
    void emailsWithMissingFieldsDoNotBreakSearch() {
        Email empty = new Email();
        empty.setSubject(null);
        empty.setSender(null);
        empty.setBody(null);

        assertTrue(service.search(List.of(empty), "anything").isEmpty());
        assertEquals(List.of(invoice), service.search(List.of(empty, invoice), "invoice"));
    }

    private static Email email(String subject, String sender, String body) {
        return new Email(subject, sender, new Date(), body, List.of());
    }
}
