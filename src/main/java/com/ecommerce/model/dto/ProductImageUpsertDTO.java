package com.ecommerce.model.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecommerce.model.entity.SessionAware;

import lombok.Data;

@Data
public class ProductImageUpsertDTO implements SessionAware {
    private Long id;
    private String imageId;
    private String filename;
    private Map<String, Object> sessions;
    private List<String> variant;

    @Override
    public Map<String, Object> getSessionInfo() {
        if(sessions == null) return null;
        return (Map<String, Object>) sessions.get("session");
    }

    @Override
    public void setSessionInfo(Map<String, Object> sessionInfo) {
        if(sessions == null) {
            sessions = new HashMap<>();
        }
        sessions.put("session", sessionInfo);
    }

    @Override
    public void clearSessionInfo() {
        if(sessions != null) {
            sessions.remove("session");
        }
    }
}
