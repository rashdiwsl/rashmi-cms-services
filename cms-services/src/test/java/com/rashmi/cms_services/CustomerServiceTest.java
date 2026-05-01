package com.rashmi.cms_services;

import com.rashmi.cms_services.dto.CustomerRequestDTO;
import com.rashmi.cms_services.dto.CustomerResponseDTO;
import com.rashmi.cms_services.entity.Customer;
import com.rashmi.cms_services.repository.CustomerRepository;
import com.rashmi.cms_services.repository.CustomerAddressRepository;
import com.rashmi.cms_services.repository.CustomerMobileRepository;
import com.rashmi.cms_services.repository.CityRepository;
import com.rashmi.cms_services.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMobileRepository mobileRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequestDTO requestDTO;
    private Customer customer;

    @BeforeEach
    void setUp() {
        requestDTO = new CustomerRequestDTO();
        requestDTO.setName("Rashmi Rathnayake");
        requestDTO.setDateOfBirth(LocalDate.of(1995, 5, 15));
        requestDTO.setNicNumber("199512345678");

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Rashmi Rathnayake");
        customer.setDateOfBirth(LocalDate.of(1995, 5, 15));
        customer.setNicNumber("199512345678");
    }

    @Test
    void createCustomer_Success() {
        when(customerRepository.existsByNicNumber("199512345678")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponseDTO response = customerService.createCustomer(requestDTO);

        assertNotNull(response);
        assertEquals("Rashmi Rathnayake", response.getName());
        assertEquals("199512345678", response.getNicNumber());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateNIC_ThrowsException() {
        when(customerRepository.existsByNicNumber("199512345678")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(requestDTO));

        assertTrue(ex.getMessage().contains("NIC number already exists"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomer_NotFound_ThrowsException() {
        when(customerRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> customerService.getCustomer(99L));

        assertTrue(ex.getMessage().contains("Customer not found"));
    }

    @Test
    void getCustomer_Success() {
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDTO response = customerService.getCustomer(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rashmi Rathnayake", response.getName());
    }
}