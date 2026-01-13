package com.example.app.models;

public class ImportImageRequest {
    private String filename;
    private long file_size;
    private String mime_type;
    private String file_data;
    private String token;

    public ImportImageRequest(String filename, long file_size, String mime_type, String file_data, String token) {
        this.filename = filename;
        this.file_size = file_size;
        this.mime_type = mime_type;
        this.file_data = file_data;
        this.token = token;
    }

    public String getFilename() { return filename; }
    public long getFileSize() { return file_size; }
    public String getMimeType() { return mime_type; }
    public String getFileData() { return file_data; }
    public String getToken() { return token; }
}
