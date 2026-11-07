package org.example.service;

import org.example.Email;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EmailSearchService {

    public List<Email> search(List<Email> emails, String query) {
        if (emails == null || emails.isEmpty()) {
            return Collections.emptyList();
        }
        if (query == null || query.trim().isEmpty()) {
            return emails;
        }

        String lowerQuery = query.trim().toLowerCase();

        return emails.stream()
                .filter(email -> matchesQuery(email, lowerQuery))
                .collect(Collectors.toList());
    }

    private boolean matchesQuery(Email email, String query) {
        boolean matchSubject = email.getSubject() != null && email.getSubject().toLowerCase().contains(query);
        boolean matchSender = email.getSender() != null && email.getSender().toLowerCase().contains(query);
        boolean matchBody = email.getBody() != null && email.getBody().toLowerCase().contains(query);

        return matchSubject || matchSender || matchBody;
    }
}