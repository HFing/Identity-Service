package com.hfing.identity_service.dto.request;


import com.hfing.identity_service.validator.DobContraints;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserUpdateRequest {
     String password ;
     String firstName ;
     String lastName ;

     @DobContraints(min = 18, message = "INVALID_DOB")
     LocalDate dob ;
     List<String> roles ;




}
