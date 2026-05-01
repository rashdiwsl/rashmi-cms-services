package com.rashmi.cms_services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerRequestDTO {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotNull(message = "Date of birth is mandatory")
    private LocalDate dateOfBirth;

    @NotBlank(message = "NIC number is mandatory")
    private String nicNumber;

    private List<String> mobileNumbers;

    private List<AddressDTO> addresses;

    private List<Long> familyMemberIds;
}