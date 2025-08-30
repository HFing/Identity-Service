package com.hfing.identity_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hfing.identity_service.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleRequest {
     String name;
     String description;
     Set<String> permissions;
}
