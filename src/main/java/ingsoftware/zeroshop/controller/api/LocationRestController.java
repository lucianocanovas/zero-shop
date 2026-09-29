package ingsoftware.zeroshop.controller.api;

import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.repository.location.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationRestController {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

    @GetMapping("/states")
    public List<LocationDTO> getStates(@RequestParam("countryId") UUID countryId) {
        return stateRepository.findAll().stream()
                .filter(state -> state.getCountry() != null && state.getCountry().getId().equals(countryId)
                        && !state.getDeleted())
                .map(state -> new LocationDTO(state.getId(), state.getName(), state.getCode()))
                .collect(Collectors.toList());
    }

    @GetMapping("/cities")
    public List<LocationDTO> getCities(@RequestParam("stateId") UUID stateId) {
        return cityRepository.findAll().stream()
                .filter(city -> city.getState() != null && city.getState().getId().equals(stateId)
                        && !city.getDeleted())
                .map(city -> new LocationDTO(city.getId(), city.getName(), city.getCode()))
                .collect(Collectors.toList());
    }

    public static class LocationDTO {
        private UUID id;
        private String name;
        private String code;

        public LocationDTO(UUID id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }
}
