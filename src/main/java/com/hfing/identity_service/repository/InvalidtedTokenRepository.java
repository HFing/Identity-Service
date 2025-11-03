package com.hfing.identity_service.repository;

import com.hfing.identity_service.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidtedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
