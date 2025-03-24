package com.example.brewery_api.service;

import com.example.brewery_api.model.Brewery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BreweryService {
    private static final Logger logger = LoggerFactory.getLogger(BreweryService.class);
    private final Map<String, Brewery> breweries = new HashMap<>();
    private final ObjectMapper objectMapper;

    public BreweryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        // Load breweries from JSON file
        try (InputStream inputStream = getClass().getResourceAsStream("/data/breweries.json")) {
            if (inputStream == null) {
                logger.error("Breweries JSON file not found at /data/breweries.json");
                return;
            }
            List<Brewery> breweryList = objectMapper.readValue(inputStream, new TypeReference<List<Brewery>>() {});
            for (Brewery brewery : breweryList) {
                breweries.put(brewery.getId(), brewery);
            }
            logger.info("Loaded {} breweries from JSON", breweryList.size());
        } catch (IOException e) {
            logger.error("Failed to load breweries from JSON: {}", e.getMessage(), e);
        }
    }

    public void addBrewery(Brewery brewery) {
        breweries.put(brewery.getId(), brewery);
    }

    public Brewery getBreweryById(String id) {
        Brewery brewery = breweries.get(id);
        if (brewery == null) {
            logger.warn("Brewery with ID {} not found", id);
            return null;
        }
        return brewery;
    }

    public List<Brewery> getAllBreweries() {
        return new ArrayList<>(breweries.values());
    }

    public List<Brewery> getBreweriesByIds(Set<String> ids) {
        return breweries.values().stream()
                .filter(brewery -> ids.contains(brewery.getId()))
                .collect(Collectors.toList());
    }

    public int getBreweryCount() {
        return breweries.size();
    }
}