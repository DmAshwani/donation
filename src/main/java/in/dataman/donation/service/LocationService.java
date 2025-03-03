package in.dataman.donation.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import in.dataman.donation.transentity.City;
import in.dataman.donation.transentity.Country;
import in.dataman.donation.transentity.State;
import in.dataman.donation.transrepository.CityRepository;
import in.dataman.donation.transrepository.CountryRepository;
import in.dataman.donation.transrepository.StateRepository;

@Service
public class LocationService {
    @Autowired
    private CountryRepository countryRepo;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepo;

    public List<Country> getAllCountries() {
        return countryRepo.findAll();
    }

    public List<State> getStatesByCountry() {
        return stateRepository.findAll();
    }

    public Page<City> getCitiesByState(String stateCode, int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page, size);
        return cityRepo.findByStateCode(stateCode, pageable);
    }
    
    public Page<City> getCitiesByStateS(String stateCode, String namePrefix, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (namePrefix != null && !namePrefix.isEmpty()) {
            return cityRepo.findCitiesByStateAndCityName(Integer.parseInt(stateCode), namePrefix, pageable);
        } else {
            return cityRepo.findByStateCode(stateCode, pageable);
        }
    }

    
}

