package de.jaimerojas.clickup.model;

public class ClickUpTaskState {
    private String id;
    private String status;
    private String type;

    /**
     * Default constructor for serialization
     */
    public ClickUpTaskState() {
    }

    // serialization setters and getters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
