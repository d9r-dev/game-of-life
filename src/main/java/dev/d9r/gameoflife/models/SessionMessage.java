package dev.d9r.gameoflife.models;

public class SessionMessage {
    private String sessionId;

    public SessionMessage() {}

    public SessionMessage(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
