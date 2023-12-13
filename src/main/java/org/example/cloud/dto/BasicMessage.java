package org.example.cloud.dto;

public class BasicMessage {

    private MediaContentType type;
    private String url;

    private String fullname;
    private String text;
    private Long destination;

    public BasicMessage(MediaContentType type, Long destination) {
        this.type = type;
        this.destination = destination;
    }

    public BasicMessage() {}

    public MediaContentType getType() {
        return type;
    }

    public void setType(MediaContentType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDestination() {
        return destination;
    }

    public void setDestination(Long destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        if (fullname != null && !fullname.isEmpty()) {
            return fullname + ": " + text;
        } else {
            return text;
        }
    }
}
