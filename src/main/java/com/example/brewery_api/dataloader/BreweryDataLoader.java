package com.example.brewery_api.dataloader;

import com.example.brewery_api.model.Brewery;
import com.example.brewery_api.service.BreweryService;
import org.dataloader.BatchLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class BreweryDataLoader implements BatchLoader<String, Brewery> {
    private static final Logger logger = LoggerFactory.getLogger(BreweryDataLoader.class);
    private final BreweryService breweryService;

    public BreweryDataLoader(BreweryService breweryService) {
        this.breweryService = breweryService;
    }

    @Override
    public CompletableFuture<List<Brewery>> load(List<String> keys) {
        logger.info("Loading breweries for keys: {}", keys);
        try {
            // Convert List to Set for the service method (if needed)
            Set<String> keySet = Set.copyOf(keys);
            List<Brewery> breweries = breweryService.getBreweriesByIds(keySet);
            return CompletableFuture.completedFuture(breweries);
        } catch (Exception e) {
            logger.error("Failed to load breweries for keys {}: {}", keys, e.getMessage());
            throw new RuntimeException("Failed to load breweries", e);
        }
    }

    // Helper method to adapt to Flux for BatchLoaderRegistry
    public Flux<Brewery> loadReactive(List<String> keys) {
        return Mono.fromFuture(load(keys))
                .flatMapMany(Flux::fromIterable);
    }

    // New method to load breweries directly
    public List<Brewery> loadBreweriesByIds(List<String> ids) {
        logger.info("Direct loading breweries for IDs: {}", ids);
        try {
            Set<String> idSet = Set.copyOf(ids);
            return breweryService.getBreweriesByIds(idSet);
        } catch (Exception e) {
            logger.error("Failed to load breweries for IDs {}: {}", ids, e.getMessage());
            throw new RuntimeException("Failed to load breweries", e);
        }
    }
}