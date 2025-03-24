package com.example.brewery_api.fetcher;

import com.example.brewery_api.model.Beer;
import com.example.brewery_api.model.Brewery;
import com.example.brewery_api.service.BeerService;
import com.example.brewery_api.service.BreweryService;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class BreweryDataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(BreweryDataFetcher.class);
    private final BreweryService breweryService;
    private final BeerService beerService;

    public BreweryDataFetcher(BreweryService breweryService, BeerService beerService) {
        this.breweryService = breweryService;
        this.beerService = beerService;
    }

    public DataFetcher<Brewery> getBreweryById() {
        return environment -> {
            String breweryId = environment.getArgument("id");
            logger.info("Fetching brewery with id: {}", breweryId);
            return breweryService.getBreweryById(breweryId);
        };
    }

    public DataFetcher<List<Brewery>> getAllBreweries() {
        return environment -> {
            logger.info("Fetching all breweries");
            return breweryService.getAllBreweries();
        };
    }

    public DataFetcher<List<Beer>> getBreweryBeersWithoutDataLoader() {
        return environment -> {
            Brewery brewery = environment.getSource();
            logger.info("Fetching beers for brewery {}: {} (without DataLoader - N+1 issue)",
                    brewery.getId(), brewery.getName());
            return beerService.getBeersByBreweryId(brewery.getId());
        };
    }

    public DataFetcher<CompletableFuture<List<Beer>>> getBreweryBeersWithDataLoader() {
        return environment -> {
            Brewery brewery = environment.getSource();
            DataLoader<String, List<Beer>> dataLoader = environment.getDataLoader("breweryBeersLoader");
            logger.info("Fetching beers for brewery {}: {} (with DataLoader)",
                    brewery.getId(), brewery.getName());
            return dataLoader.load(brewery.getId());
        };
    }
}