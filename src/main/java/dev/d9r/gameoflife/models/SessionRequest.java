package dev.d9r.gameoflife.models;

import java.io.Serializable;

public class SessionRequest implements Serializable {
    private String sessionId;

    public SessionRequest() {}

    public SessionRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
