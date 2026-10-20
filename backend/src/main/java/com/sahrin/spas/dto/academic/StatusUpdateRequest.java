package com.sahrin.spas.dto.academic;

public class StatusUpdateRequest {

    private Boolean active;
    private String status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(Boolean active) {
        this.active = active;
    }

    public StatusUpdateRequest(String status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
