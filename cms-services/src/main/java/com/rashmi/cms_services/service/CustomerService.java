package com.rashmi.cms_services.service;

import com.rashmi.cms_services.dto.AddressDTO;
import com.rashmi.cms_services.dto.CustomerRequestDTO;
import com.rashmi.cms_services.dto.CustomerResponseDTO;
import com.rashmi.cms_services.entity.*;
import com.rashmi.cms_services.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMobileRepository mobileRepository;
    private final CustomerAddressRepository addressRepository;
    private final CityRepository cityRepository;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        if (customerRepository.existsByNicNumber(dto.getNicNumber())) {
            throw new RuntimeException("NIC number already exists: " + dto.getNicNumber());
        }
        Customer customer = new Customer();
        mapDtoToEntity(dto, customer);
        return toResponseDTO(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        if (customerRepository.existsByNicNumberAndIdNot(dto.getNicNumber(), id)) {
            throw new RuntimeException("NIC number already exists: " + dto.getNicNumber());
        }

        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNicNumber(dto.getNicNumber());

        customer.getMobiles().clear();
        if (dto.getMobileNumbers() != null) {
            dto.getMobileNumbers().forEach(num -> {
                CustomerMobile m = new CustomerMobile();
                m.setCustomer(customer);
                m.setMobileNumber(num);
                customer.getMobiles().add(m);
            });
        }

        customer.getAddresses().clear();
        if (dto.getAddresses() != null) {
            dto.getAddresses().stream()
                    .filter(addrDto -> addrDto.getAddressLine1() != null && !addrDto.getAddressLine1().isBlank())
                    .forEach(addrDto -> {
                        CustomerAddress a = new CustomerAddress();
                        a.setCustomer(customer);
                        a.setAddressLine1(addrDto.getAddressLine1());
                        a.setAddressLine2(addrDto.getAddressLine2());
                        if (addrDto.getCityId() != null) {
                            City city = cityRepository.findById(addrDto.getCityId())
                                    .orElseThrow(() -> new RuntimeException("City not found"));
                            a.setCity(city);
                            a.setCountry(city.getCountry());
                        }
                        customer.getAddresses().add(a);
                    });
        }

        customer.getFamilyMembers().clear();
        if (dto.getFamilyMemberIds() != null) {
            dto.getFamilyMemberIds().forEach(memberId -> {
                CustomerFamily f = new CustomerFamily();
                f.setCustomer(customer);
                f.setFamilyMember(customerRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Family member not found: " + memberId)));
                customer.getFamilyMembers().add(f);
            });
        }

        return toResponseDTO(customerRepository.save(customer));
    }

    public CustomerResponseDTO getCustomer(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return toResponseDTO(customer);
    }

    public Page<CustomerResponseDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponseDTO);
    }

    // ✅ Returns Map with counts
    @Transactional
    public Map<String, Object> bulkUpload(MultipartFile file) throws Exception {
        int created = 0, updated = 0, failed = 0, totalRows = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellValue(row, 0);
                    String dob = getCellValue(row, 1);
                    String nic = getCellValue(row, 2);

                    if (name.isBlank() || dob.isBlank() || nic.isBlank()) {
                        failed++;
                        continue;
                    }

                    totalRows++;
                    Optional<Customer> existing = customerRepository.findByNicNumber(nic);

                    if (existing.isPresent()) {
                        Customer c = existing.get();
                        c.setName(name);
                        c.setDateOfBirth(LocalDate.parse(dob));
                        customerRepository.save(c);
                        updated++;
                    } else {
                        Customer c = new Customer();
                        c.setName(name);
                        c.setDateOfBirth(LocalDate.parse(dob));
                        c.setNicNumber(nic);
                        customerRepository.save(c);
                        created++;
                    }

                    if ((created + updated) % 500 == 0) {
                        customerRepository.flush();
                    }

                } catch (Exception e) {
                    failed++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", totalRows);
        result.put("created", created);
        result.put("updated", updated);
        result.put("failed", failed);
        return result;
    }

    private void mapDtoToEntity(CustomerRequestDTO dto, Customer customer) {
        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNicNumber(dto.getNicNumber());

        if (dto.getMobileNumbers() != null) {
            List<CustomerMobile> mobiles = dto.getMobileNumbers().stream()
                    .filter(num -> num != null && !num.isBlank())
                    .map(num -> {
                        CustomerMobile m = new CustomerMobile();
                        m.setCustomer(customer);
                        m.setMobileNumber(num);
                        return m;
                    }).collect(Collectors.toList());
            customer.setMobiles(mobiles);
        }

        if (dto.getAddresses() != null) {
            Set<CustomerAddress> addresses = dto.getAddresses().stream()
                    .filter(addrDto -> addrDto.getAddressLine1() != null && !addrDto.getAddressLine1().isBlank())
                    .map(addrDto -> {
                        CustomerAddress a = new CustomerAddress();
                        a.setCustomer(customer);
                        a.setAddressLine1(addrDto.getAddressLine1());
                        a.setAddressLine2(addrDto.getAddressLine2());
                        if (addrDto.getCityId() != null) {
                            City city = cityRepository.findById(addrDto.getCityId())
                                    .orElseThrow(() -> new RuntimeException("City not found"));
                            a.setCity(city);
                            a.setCountry(city.getCountry());
                        }
                        return a;
                    }).collect(Collectors.toCollection(HashSet::new));
            customer.setAddresses(addresses);
        }

        if (dto.getFamilyMemberIds() != null) {
            Set<CustomerFamily> family = dto.getFamilyMemberIds().stream().map(memberId -> {
                CustomerFamily f = new CustomerFamily();
                f.setCustomer(customer);
                f.setFamilyMember(customerRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Family member not found: " + memberId)));
                return f;
            }).collect(Collectors.toCollection(HashSet::new));
            customer.setFamilyMembers(family);
        }
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private CustomerResponseDTO toResponseDTO(Customer c) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDateOfBirth(c.getDateOfBirth());
        dto.setNicNumber(c.getNicNumber());

        if (c.getMobiles() != null)
            dto.setMobileNumbers(c.getMobiles().stream()
                    .map(CustomerMobile::getMobileNumber)
                    .collect(Collectors.toList()));

        if (c.getAddresses() != null)
            dto.setAddresses(c.getAddresses().stream().map(a -> {
                AddressDTO adto = new AddressDTO();
                adto.setAddressLine1(a.getAddressLine1());
                adto.setAddressLine2(a.getAddressLine2());
                if (a.getCity() != null) {
                    adto.setCityId(a.getCity().getId());
                    adto.setCityName(a.getCity().getName());
                    if (a.getCity().getCountry() != null) {
                        adto.setCountryId(a.getCity().getCountry().getId());
                        adto.setCountryName(a.getCity().getCountry().getName());
                    }
                }
                return adto;
            }).collect(Collectors.toList()));

        if (c.getFamilyMembers() != null)
            dto.setFamilyMembers(c.getFamilyMembers().stream().map(f -> {
                CustomerResponseDTO.FamilyMemberDTO fm = new CustomerResponseDTO.FamilyMemberDTO();
                fm.setId(f.getFamilyMember().getId());
                fm.setName(f.getFamilyMember().getName());
                fm.setNicNumber(f.getFamilyMember().getNicNumber());
                return fm;
            }).collect(Collectors.toList()));

        return dto;
    }
}