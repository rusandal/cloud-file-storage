package ru.minikhanov.cloud_storage.models;

public class MessageResponse {
    private final static Integer id = 0;
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }


}
