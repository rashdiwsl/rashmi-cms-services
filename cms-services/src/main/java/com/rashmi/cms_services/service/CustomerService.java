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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMobileRepository mobileRepository;
    private final CustomerAddressRepository addressRepository;
    private final CityRepository cityRepository;

    // ── CREATE ──────────────────────────────────────────
    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        if (customerRepository.existsByNicNumber(dto.getNicNumber())) {
            throw new RuntimeException("NIC number already exists: " + dto.getNicNumber());
        }
        Customer customer = new Customer();
        mapDtoToEntity(dto, customer);
        return toResponseDTO(customerRepository.save(customer));
    }

    // ── UPDATE ──────────────────────────────────────────
    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        mobileRepository.deleteByCustomerId(id);
        addressRepository.deleteByCustomerId(id);
        customer.getFamilyMembers().clear();
        mapDtoToEntity(dto, customer);
        return toResponseDTO(customerRepository.save(customer));
    }

    // ── GET ONE ─────────────────────────────────────────
    public CustomerResponseDTO getCustomer(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return toResponseDTO(customer);
    }

    // ── GET ALL (paged) ─────────────────────────────────
    public Page<CustomerResponseDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponseDTO);
    }

    // ── BULK UPLOAD ─────────────────────────────────────
    @Transactional
    public int bulkUpload(MultipartFile file) throws Exception {
        int count = 0;
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<Customer> batch = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nic = getCellValue(row, 2);
                if (customerRepository.existsByNicNumber(nic)) continue;

                Customer c = new Customer();
                c.setName(getCellValue(row, 0));
                c.setDateOfBirth(LocalDate.parse(getCellValue(row, 1)));
                c.setNicNumber(nic);
                batch.add(c);

                // Save in batches of 500 to avoid memory issues
                if (batch.size() == 500) {
                    customerRepository.saveAll(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                customerRepository.saveAll(batch);
                count += batch.size();
            }
        }
        return count;
    }

    // ── HELPERS ─────────────────────────────────────────
    private void mapDtoToEntity(CustomerRequestDTO dto, Customer customer) {
        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNicNumber(dto.getNicNumber());

        // Mobiles
        if (dto.getMobileNumbers() != null) {
            List<CustomerMobile> mobiles = dto.getMobileNumbers().stream().map(num -> {
                CustomerMobile m = new CustomerMobile();
                m.setCustomer(customer);
                m.setMobileNumber(num);
                return m;
            }).collect(Collectors.toList());
            customer.setMobiles(mobiles);
        }

        // Addresses
        if (dto.getAddresses() != null) {
            List<CustomerAddress> addresses = dto.getAddresses().stream().map(addrDto -> {
                CustomerAddress a = new CustomerAddress();
                a.setCustomer(customer);
                a.setAddressLine1(addrDto.getAddressLine1());
                a.setAddressLine2(addrDto.getAddressLine2());
                a.setCity(cityRepository.findById(addrDto.getCityId())
                        .orElseThrow(() -> new RuntimeException("City not found")));
                a.setCountry(a.getCity().getCountry());
                return a;
            }).collect(Collectors.toList());
            customer.setAddresses(addresses);
        }

        // Family members
        if (dto.getFamilyMemberIds() != null) {
            List<CustomerFamily> family = dto.getFamilyMemberIds().stream().map(memberId -> {
                CustomerFamily f = new CustomerFamily();
                f.setCustomer(customer);
                f.setFamilyMember(customerRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Family member not found: " + memberId)));
                return f;
            }).collect(Collectors.toList());
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
                    .map(CustomerMobile::getMobileNumber).collect(Collectors.toList()));

        if (c.getAddresses() != null)
            dto.setAddresses(c.getAddresses().stream().map(a -> {
                AddressDTO adto = new AddressDTO();
                adto.setAddressLine1(a.getAddressLine1());
                adto.setAddressLine2(a.getAddressLine2());
                adto.setCityId(a.getCity().getId());
                adto.setCountryId(a.getCountry().getId());
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