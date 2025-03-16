package com.ecommerce.model.entity;

import java.util.Map;

public interface SessionAware {
    Map<String, Object> getSessionInfo();
    void setSessionInfo(Map<String, Object> sessionInfo);
    void clearSessionInfo();
}


