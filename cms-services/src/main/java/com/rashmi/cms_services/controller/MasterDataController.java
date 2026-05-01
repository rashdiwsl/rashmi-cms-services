package com.rashmi.cms_services.controller;

import com.rashmi.cms_services.entity.City;
import com.rashmi.cms_services.entity.Country;
import com.rashmi.cms_services.repository.CityRepository;
import com.rashmi.cms_services.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    @GetMapping("/api/cities")
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @GetMapping("/api/countries")
    public List<Country> getCountries() {
        return countryRepository.findAll();
    }
}