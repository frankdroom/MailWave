package org.example;

import org.example.service.MimeParserService;

import jakarta.mail.FetchProfile;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailReceiver {

    /** Сколько последних писем загружать за один раз. */
    public static final int DEFAULT_MAX_MESSAGES = 100;

    private final String host;
    private final String port;
    private final String username;
    private final String password;

    public EmailReceiver(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Быстрая проверка логина и пароля: подключается, открывает INBOX и сразу закрывает.
     * Письма при этом НЕ скачиваются.
     */
    public void testConnection() throws MessagingException {
        Store store = null;
        Folder inbox = null;
        try {
            store = connect();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
        } finally {
            closeQuietly(inbox);
            closeQuietly(store);
        }
    }

    public List<Email> receiveEmails(MimeParserService parserService) throws MessagingException {
        return receiveEmails(parserService, DEFAULT_MAX_MESSAGES);
    }

    /** Загружает не более {@code maxMessages} самых новых писем, сначала новые. */
    public List<Email> receiveEmails(MimeParserService parserService, int maxMessages) throws MessagingException {
        Store store = null;
        Folder inbox = null;
        try {
            store = connect();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int total = inbox.getMessageCount();
            if (total == 0) {
                return new ArrayList<>();
            }

            // Берём только последние письма. Раньше качался весь ящик (inbox.getMessages()),
            // на большом ящике это занимало минуты и выглядело как бесконечное подключение.
            int start = Math.max(1, total - maxMessages + 1);
            Message[] messages = inbox.getMessages(start, total);

            // Заголовки всех писем запрашиваются одним пакетом, а не по одному письму.
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            profile.add(FetchProfile.Item.CONTENT_INFO);
            inbox.fetch(messages, profile);

            List<Email> emails = new ArrayList<>(messages.length);
            for (int i = messages.length - 1; i >= 0; i--) { // новые письма первыми
                if (messages[i] instanceof MimeMessage mimeMessage) {
                    try {
                        emails.add(parserService.parseMessage(mimeMessage));
                    } catch (MessagingException | IOException e) {
                        // Одно битое письмо не должно ломать загрузку всего ящика.
                        System.err.println("Пропущено письмо, которое не удалось разобрать: " + e.getMessage());
                    }
                }
            }
            return emails;
        } finally {
            closeQuietly(inbox);
            closeQuietly(store);
        }
    }

    private Store connect() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", host);
        properties.put("mail.imaps.port", port);
        properties.put("mail.imaps.ssl.enable", "true");

        properties.put("mail.imaps.connectiontimeout", "10000"); // 10 секунд на подключение
        properties.put("mail.imaps.timeout", "10000");           // 10 секунд на чтение данных
        properties.put("mail.imaps.writetimeout", "10000");

        // По умолчанию тело письма читается кусками по 16 КБ, по запросу на каждый кусок.
        // Письмо с вложением на несколько МБ превращалось в сотни запросов. Читаем кусками по 1 МБ.
        properties.put("mail.imaps.fetchsize", "1048576");

        Session session = Session.getInstance(properties);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);
        return store;
    }

    private static void closeQuietly(Folder folder) {
        if (folder != null && folder.isOpen()) {
            try {
                folder.close(false);
            } catch (MessagingException ignored) {
            }
        }
    }

    private static void closeQuietly(Store store) {
        if (store != null && store.isConnected()) {
            try {
                store.close();
            } catch (MessagingException ignored) {
            }
        }
    }
}
