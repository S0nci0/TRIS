package it.tris.common;

public class ProtocolMessage {
    private String type;
    private String content;

    public ProtocolMessage() {
    }

    public ProtocolMessage(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
