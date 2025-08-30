package com.hfing.identity_service.repository;

import com.hfing.identity_service.entity.Permission;
import com.hfing.identity_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
