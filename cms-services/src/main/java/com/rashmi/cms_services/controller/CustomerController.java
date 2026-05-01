package com.rashmi.cms_services.controller;

import com.rashmi.cms_services.dto.CustomerRequestDTO;
import com.rashmi.cms_services.dto.CustomerResponseDTO;
import com.rashmi.cms_services.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    // CREATE
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(
            @Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    // GET ONE
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    // GET ALL (paged)
    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    // BULK UPLOAD
    @PostMapping("/bulk-upload")
    public ResponseEntity<String> bulkUpload(@RequestParam("file") MultipartFile file) {
        try {
            int count = customerService.bulkUpload(file);
            return ResponseEntity.ok("Successfully uploaded " + count + " customers.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
}