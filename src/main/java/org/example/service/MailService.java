package org.example.service;

import org.example.Email;
import org.example.EmailReceiver;
import org.example.EmailSender;

import java.util.ArrayList;
import java.util.List;

public class MailService {

    private final EmailReceiver receiver;
    private final EmailSender sender;
    private final MimeParserService parserService;
    private final EmailSearchService searchService;
    private final List<Email> cachedEmails = new ArrayList<>();

    public MailService(EmailReceiver receiver, EmailSender sender,
                       MimeParserService parserService, EmailSearchService searchService) {
        this.receiver = receiver;
        this.sender = sender;
        this.parserService = parserService;
        this.searchService = searchService;
    }

    /** Проверяет логин и пароль, не скачивая письма. Используется при входе. */
    public void testConnection() throws Exception {
        receiver.testConnection();
    }

    public List<Email> fetchEmails() throws Exception {
        List<Email> fetched = receiver.receiveEmails(parserService);
        cachedEmails.clear();
        cachedEmails.addAll(fetched);
        return new ArrayList<>(cachedEmails);
    }

    public void sendEmail(String to, String subject, String body, List<String> attachmentPaths) throws Exception {
        sender.send(to, subject, body, attachmentPaths);
    }

    public List<Email> filterEmails(String query) {
        return searchService.search(cachedEmails, query);
    }
}