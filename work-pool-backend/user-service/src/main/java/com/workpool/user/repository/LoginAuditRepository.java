package com.workpool.user.repository;

import com.workpool.user.model.LoginAudit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoginAuditRepository extends MongoRepository<LoginAudit, String> {
}
