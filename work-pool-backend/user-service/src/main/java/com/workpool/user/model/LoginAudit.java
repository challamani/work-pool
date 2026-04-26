package com.workpool.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "login_audits")
public class LoginAudit {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String email;

    private String authProvider;
    private boolean success;
    private String reason;

    private String clientIp;
    private String forwardedFor;
    private String userAgent;
    private String acceptLanguage;
    private String origin;
    private String referer;
    private String requestId;
    private String geoCountry;
    private String geoCity;

    @CreatedDate
    private Instant createdAt;
}
