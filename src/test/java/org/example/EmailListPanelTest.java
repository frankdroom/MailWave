package org.example;

import org.example.EmailListPanel.Filter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Вкладки All / Unread / Starred / Attachments. */
class EmailListPanelTest {

    private final Email unread = email("unread", false, false, false);
    private final Email read = email("read", true, false, false);
    private final Email starred = email("starred", true, true, false);
    private final Email withFile = email("with file", false, false, true);
    private final List<Email> all = List.of(unread, read, starred, withFile);

    @Test
    void allKeepsEverythingInOrder() {
        assertEquals(all, EmailListPanel.filter(all, Filter.ALL));
    }

    @Test
    void unreadKeepsOnlyUnreadEmails() {
        assertEquals(List.of(unread, withFile), EmailListPanel.filter(all, Filter.UNREAD));
    }

    @Test
    void starredKeepsOnlyStarredEmails() {
        assertEquals(List.of(starred), EmailListPanel.filter(all, Filter.STARRED));
    }

    @Test
    void attachmentsKeepsOnlyEmailsWithFiles() {
        assertEquals(List.of(withFile), EmailListPanel.filter(all, Filter.ATTACHMENTS));
    }

    @Test
    void filteringAnEmptyListGivesAnEmptyList() {
        for (Filter filter : Filter.values()) {
            assertTrue(EmailListPanel.filter(List.of(), filter).isEmpty());
        }
    }

    @Test
    void filteringDoesNotChangeTheSourceList() {
        List<Email> source = new java.util.ArrayList<>(all);

        EmailListPanel.filter(source, Filter.STARRED);

        assertEquals(all, source);
    }

    @Test
    void panelShowsAllUpdatedEmails() {
        EmailListPanel panel = new EmailListPanel();

        panel.updateList(all);

        assertEquals(4, panel.getList().getModel().getSize());
    }

    @Test
    void panelAppliesTheSelectedFilter() {
        EmailListPanel panel = new EmailListPanel();
        panel.updateList(all);

        panel.setFilter(Filter.UNREAD);
        assertEquals(2, panel.getList().getModel().getSize());
        // размер совпал бы и у инвертированного фильтра, поэтому проверяем сами письма
        assertEquals(unread, panel.getList().getModel().getElementAt(0));
        assertEquals(withFile, panel.getList().getModel().getElementAt(1));

        panel.setFilter(Filter.ALL);
        assertEquals(4, panel.getList().getModel().getSize());
    }

    @Test
    void nullListClearsThePanel() {
        EmailListPanel panel = new EmailListPanel();
        panel.updateList(all);

        panel.updateList(null);

        assertEquals(0, panel.getList().getModel().getSize());
    }

    @Test
    void clearEmptiesThePanel() {
        EmailListPanel panel = new EmailListPanel();
        panel.updateList(all);

        panel.clear();

        assertEquals(0, panel.getList().getModel().getSize());
    }

    private static Email email(String subject, boolean read, boolean starred, boolean withAttachment) {
        List<FileByteData> files = withAttachment ? List.of(new FileByteData("a.txt", new byte[]{1})) : List.of();
        return new Email(subject, "sender@example.com", new Date(), "body", files, read, starred);
    }
}
