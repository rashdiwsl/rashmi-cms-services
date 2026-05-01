package com.rashmi.cms_services.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerResponseDTO {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String nicNumber;
    private List<String> mobileNumbers;
    private List<AddressDTO> addresses;
    private List<FamilyMemberDTO> familyMembers;

    @Data
    public static class FamilyMemberDTO {
        private Long id;
        private String name;
        private String nicNumber;
    }
}