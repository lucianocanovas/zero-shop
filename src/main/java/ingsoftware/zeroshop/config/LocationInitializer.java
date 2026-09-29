package ingsoftware.zeroshop.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.entity.location.Country;
import ingsoftware.zeroshop.entity.location.State;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.repository.location.CountryRepository;
import ingsoftware.zeroshop.repository.location.StateRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@Configuration
public class LocationInitializer {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

    @PostConstruct
    public void initLocations() {
        if (countryRepository.count() == 0) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = new ClassPathResource("data/locations.json").getInputStream();

                List<CountryJSON> countriesJSON = mapper.readValue(inputStream, new TypeReference<List<CountryJSON>>() {
                });

                for (CountryJSON cJson : countriesJSON) {
                    Country country = new Country();
                    country.setName(cJson.getName());
                    country.setCode(cJson.getCode());
                    country = countryRepository.save(country);

                    if (cJson.getStates() != null) {
                        for (StateJSON sJson : cJson.getStates()) {
                            State state = new State();
                            state.setName(sJson.getName());
                            state.setCode(sJson.getCode());
                            state.setCountry(country);
                            state = stateRepository.save(state);

                            if (sJson.getCities() != null) {
                                for (CityJSON cityJson : sJson.getCities()) {
                                    City city = new City();
                                    city.setName(cityJson.getName());
                                    city.setCode(cityJson.getCode());
                                    city.setState(state);
                                    cityRepository.save(city);
                                }
                            }
                        }
                    }
                }

                System.out.println(" Base de datos inicializada exitosamente con " + countryRepository.count()
                        + " países, " + stateRepository.count() + " provincias/estados y " + cityRepository.count()
                        + " ciudades desde locations.json.");

            } catch (Exception e) {
                System.err.println(" Error al inicializar locations.json: " + e.getMessage());
            }
        }
    }

    // --- DTOs internos para leer el JSON ---

    @Data
    public static class CountryJSON {
        private String name;
        private String code;
        private List<StateJSON> states;
    }

    @Data
    public static class StateJSON {
        private String name;
        private String code;
        private List<CityJSON> cities;
    }

    @Data
    public static class CityJSON {
        private String name;
        private String code;
    }
}
