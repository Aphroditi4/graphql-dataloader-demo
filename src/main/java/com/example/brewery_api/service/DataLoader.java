package com.example.brewery_api.service;

import com.example.brewery_api.model.Beer;
import com.example.brewery_api.model.Brewery;
import com.example.brewery_api.model.Review;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final BreweryService breweryService;
    private final BeerService beerService;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @Value("classpath:data/breweries.json")
    private Resource breweriesResource;

    @Value("classpath:data/beers.json")
    private Resource beersResource;

    @Value("classpath:data/reviews.json")
    private Resource reviewsResource;

    public DataLoader(BreweryService breweryService, BeerService beerService,
                      ReviewService reviewService, ObjectMapper objectMapper) {
        this.breweryService = breweryService;
        this.beerService = beerService;
        this.reviewService = reviewService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        loadBreweries();
        loadBeers();
        loadReviews();

        logger.info("Loaded {} breweries, {} beers, and {} reviews into memory",
                breweryService.getBreweryCount(),
                beerService.getBeerCount(),
                reviewService.getReviewCount());
    }

    private void loadBreweries() throws IOException {
        try (InputStream is = breweriesResource.getInputStream()) {
            List<Brewery> breweries = objectMapper.readValue(is, new TypeReference<List<Brewery>>() {});
            breweries.forEach(breweryService::addBrewery);
            logger.info("Loaded {} breweries from file", breweries.size());
        }
    }

    private void loadBeers() throws IOException {
        try (InputStream is = beersResource.getInputStream()) {
            List<Beer> beers = objectMapper.readValue(is, new TypeReference<List<Beer>>() {});
            beers.forEach(beerService::addBeer);
            logger.info("Loaded {} beers from file", beers.size());
        }
    }

    private void loadReviews() throws IOException {
        try (InputStream is = reviewsResource.getInputStream()) {
            List<Review> reviews = objectMapper.readValue(is, new TypeReference<List<Review>>() {});
            reviews.forEach(reviewService::addReview);
            logger.info("Loaded {} reviews from file", reviews.size());
        }
    }
}