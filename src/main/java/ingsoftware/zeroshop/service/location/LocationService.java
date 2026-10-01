package ingsoftware.zeroshop.service.location;

import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.entity.location.Country;
import ingsoftware.zeroshop.entity.location.State;
import ingsoftware.zeroshop.repository.location.AddressRepository;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.repository.location.CountryRepository;
import ingsoftware.zeroshop.repository.location.StateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LocationService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final AddressRepository addressRepository;

    public LocationService(CountryRepository countryRepository,
                           StateRepository stateRepository,
                           CityRepository cityRepository,
                           AddressRepository addressRepository) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryRepository.findAllByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public List<State> getStatesByCountryId(UUID countryId) {
        if (countryId == null) {
            return List.of();
        }
        return stateRepository.findByCountryIdAndDeletedFalse(countryId);
    }

    @Transactional(readOnly = true)
    public List<City> getCitiesByStateId(UUID stateId) {
        if (stateId == null) {
            return List.of();
        }
        return cityRepository.findByStateIdAndDeletedFalse(stateId);
    }

    @Transactional(readOnly = true)
    public Optional<City> findCityById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return cityRepository.findByIdAndDeletedFalse(id);
    }

    @Transactional(readOnly = true)
    public Optional<Address> findAddressById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return addressRepository.findByIdAndDeletedFalse(id);
    }

    @Transactional
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<Country> findAllCountries() {
        return countryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<City> getAllCities() {
        return cityRepository.findAllByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Optional<City> findFirstCity() {
        return cityRepository.findAllByDeletedFalse().stream().findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<City> findCityByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return cityRepository.findByNameIgnoreCaseAndDeletedFalse(name.trim());
    }
}
