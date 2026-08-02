package com.wk.ti.redirection.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class RedirectionAttribute implements Serializable {
    public static final String DEFAULT_REDIRECTION = "dashboard-url";
    private ConcurrentMap<String, String> sessionIdToRedirectIdMap = new ConcurrentHashMap<>();

    public String getRedirectId(String sessionId) {
        return sessionId == null ? DEFAULT_REDIRECTION : sessionIdToRedirectIdMap.get(sessionId);
    }

    public void setRedirectId(String sessionId, String redirectId) {
        if (sessionId != null && redirectId != null) {
            log.info("Action: add record with session id: {}", sessionId);
            sessionIdToRedirectIdMap.putIfAbsent(sessionId, redirectId);
        }
    }

    public void remove(String sessionId) {
        if (sessionId != null) {
            log.info("Action: remove record with session id: {}", sessionId);
            sessionIdToRedirectIdMap.remove(sessionId);
        }
    }
}

