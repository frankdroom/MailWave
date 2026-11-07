package org.example;

public class FileByteData {
    private final String fileName;
    private final byte[] data;

    public FileByteData(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = data;
    }


    public String getFileName() {
        return fileName;
    }

    public String getFilename() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return fileName;
    }
}