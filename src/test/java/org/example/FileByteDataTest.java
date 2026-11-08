package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileByteDataTest {

    @Test
    void exposesNameAndContent() {
        FileByteData file = new FileByteData("report.pdf", new byte[]{1, 2, 3});

        assertEquals("report.pdf", file.getFileName());
        assertArrayEquals(new byte[]{1, 2, 3}, file.getData());
    }

    @Test
    void bothNameGettersReturnTheSameValue() {
        FileByteData file = new FileByteData("a.txt", new byte[0]);

        assertEquals(file.getFileName(), file.getFilename());
    }

    @Test
    void toStringIsTheFileName() {
        assertEquals("a.txt", new FileByteData("a.txt", new byte[0]).toString());
    }
}
