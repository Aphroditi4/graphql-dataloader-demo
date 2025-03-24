package com.example.brewery_api.service;

import com.example.brewery_api.model.Beer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BeerService {
    private final Map<String, Beer> beers = new HashMap<>();

    public void addBeer(Beer beer) {
        beers.put(beer.getId(), beer);
    }

    public Beer getBeerById(String id) {
        return beers.get(id);
    }

    public List<Beer> getAllBeers() {
        return new ArrayList<>(beers.values());
    }

    public List<Beer> getBeersByIds(Collection<String> ids) {
        return ids.stream()
                .map(this::getBeerById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Beer> getBeersByBreweryId(String breweryId) {
        return beers.values().stream()
                .filter(beer -> beer.getBreweryId().equals(breweryId))
                .collect(Collectors.toList());
    }

    public int getBeerCount() {
        return beers.size();
    }
}