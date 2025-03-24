package com.example.brewery_api.fetcher;

import com.example.brewery_api.model.Beer;
import com.example.brewery_api.model.Brewery;
import com.example.brewery_api.model.Review;
import com.example.brewery_api.service.BeerService;
import com.example.brewery_api.service.BreweryService;
import com.example.brewery_api.service.ReviewService;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class BeerDataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(BeerDataFetcher.class);
    private final BeerService beerService;
    private final BreweryService breweryService;
    private final ReviewService reviewService;

    public BeerDataFetcher(BeerService beerService, BreweryService breweryService, ReviewService reviewService) {
        this.beerService = beerService;
        this.breweryService = breweryService;
        this.reviewService = reviewService;
    }

    public DataFetcher<Beer> getBeerById() {
        return environment -> {
            String beerId = environment.getArgument("id");
            logger.info("Fetching beer with id: {}", beerId);
            return beerService.getBeerById(beerId);
        };
    }

    public DataFetcher<List<Beer>> getAllBeers() {
        return environment -> {
            logger.info("Fetching all beers");
            return beerService.getAllBeers();
        };
    }

    public DataFetcher<Brewery> getBeerBreweryWithoutDataLoader() {
        return environment -> {
            Beer beer = environment.getSource();
            logger.info("Fetching brewery for beer {}: {} (without DataLoader - N+1 issue)",
                    beer.getId(), beer.getName());
            return breweryService.getBreweryById(beer.getBreweryId());
        };
    }

    public DataFetcher<CompletableFuture<Brewery>> getBeerBreweryWithDataLoader() {
        return environment -> {
            Beer beer = environment.getSource();
            DataLoader<String, Brewery> dataLoader = environment.getDataLoader("breweryLoader");
            logger.info("Fetching brewery for beer {}: {} (with DataLoader)",
                    beer.getId(), beer.getName());
            return dataLoader.load(beer.getBreweryId());
        };
    }

    public DataFetcher<List<Review>> getBeerReviewsWithoutDataLoader() {
        return environment -> {
            Beer beer = environment.getSource();
            logger.info("Fetching reviews for beer {}: {} (without DataLoader - N+1 issue)",
                    beer.getId(), beer.getName());
            return reviewService.getReviewsByBeerId(beer.getId());
        };
    }

    public DataFetcher<CompletableFuture<List<Review>>> getBeerReviewsWithDataLoader() {
        return environment -> {
            Beer beer = environment.getSource();
            DataLoader<String, List<Review>> dataLoader = environment.getDataLoader("beerReviewsLoader");
            logger.info("Fetching reviews for beer {}: {} (with DataLoader)",
                    beer.getId(), beer.getName());
            return dataLoader.load(beer.getId());
        };
    }
}