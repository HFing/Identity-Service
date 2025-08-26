package com.hfing.identity_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserCreationRequest {

    @Size(min = 4, max = 50, message = "USERNAME_INVALID")
     String username ;

    @Size(min = 8, max = 20, message = "INVAID_PASSWORD")
     String password ;
     String firstName ;
     String lastName ;
     LocalDate dob ;


}
