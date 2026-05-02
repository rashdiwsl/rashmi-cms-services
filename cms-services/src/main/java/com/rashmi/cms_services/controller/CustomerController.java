package com.rashmi.cms_services.controller;

import com.rashmi.cms_services.dto.CustomerRequestDTO;
import com.rashmi.cms_services.dto.CustomerResponseDTO;
import com.rashmi.cms_services.repository.CustomerRepository;
import com.rashmi.cms_services.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Fixed - returns Map with counts
    @PostMapping("/bulk-upload")
    public ResponseEntity<Map<String, Object>> bulkUpload(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = customerService.bulkUpload(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ New - download template
    @GetMapping("/bulk-upload/template")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customer_template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customers");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Date of Birth (YYYY-MM-DD)");
            header.createCell(2).setCellValue("NIC Number");

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("John Silva");
            sample.createCell(1).setCellValue("1990-01-15");
            sample.createCell(2).setCellValue("901234567V");

            workbook.write(response.getOutputStream());
        }
    }
}