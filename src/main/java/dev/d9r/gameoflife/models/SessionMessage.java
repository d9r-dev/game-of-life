package dev.d9r.gameoflife.models;

import java.io.Serializable;

public class SessionMessage implements Serializable {
    private Boolean error;
    private String errorMessage;
    private String sessionId;

    public SessionMessage() {}

    public SessionMessage(String sessionId, Boolean error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
