package org.example;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;


class EmailSenderPropertiesTest {

    private static Properties propertiesFor(String port) {
        return new EmailSender("smtp.example.com", port, "user@example.com", "secret").buildProperties();
    }

    @Test
    void port465UsesImplicitSslAndNoStartTls() {
        Properties p = propertiesFor("465");

        assertEquals("true", p.getProperty("mail.smtp.ssl.enable"));
        assertEquals("false", p.getProperty("mail.smtp.starttls.enable"));
    }

    @Test
    void port587UsesStartTlsAndNoImplicitSsl() {
        Properties p = propertiesFor("587");

        assertEquals("false", p.getProperty("mail.smtp.ssl.enable"));
        assertEquals("true", p.getProperty("mail.smtp.starttls.enable"));
    }

    @Test
    void anyOtherPortFallsBackToStartTls() {
        Properties p = propertiesFor("25");

        assertEquals("false", p.getProperty("mail.smtp.ssl.enable"));
        assertEquals("true", p.getProperty("mail.smtp.starttls.enable"));
    }

    @Test
    void usesGivenHostAndPortAndRequiresAuthentication() {
        Properties p = propertiesFor("587");

        assertEquals("smtp.example.com", p.getProperty("mail.smtp.host"));
        assertEquals("587", p.getProperty("mail.smtp.port"));
        assertEquals("true", p.getProperty("mail.smtp.auth"));
    }

    @Test
    void hasTimeoutsSoSendingNeverHangsForever() {
        Properties p = propertiesFor("465");

        assertEquals("10000", p.getProperty("mail.smtp.connectiontimeout"));
        assertEquals("10000", p.getProperty("mail.smtp.timeout"));
        assertEquals("10000", p.getProperty("mail.smtp.writetimeout"));
    }
}
