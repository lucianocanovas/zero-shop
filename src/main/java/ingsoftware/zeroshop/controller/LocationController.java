package ingsoftware.zeroshop.controller;

import ingsoftware.zeroshop.service.location.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/states")
    public List<LocationDTO> getStates(@RequestParam("countryId") UUID countryId) {
        return locationService.getStatesByCountryId(countryId).stream()
                .map(state -> new LocationDTO(state.getId(), state.getName(), state.getCode()))
                .collect(Collectors.toList());
    }

    @GetMapping("/cities")
    public List<LocationDTO> getCities(@RequestParam("stateId") UUID stateId) {
        return locationService.getCitiesByStateId(stateId).stream()
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
