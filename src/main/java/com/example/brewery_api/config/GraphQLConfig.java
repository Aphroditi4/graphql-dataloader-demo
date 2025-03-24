package com.example.brewery_api.config;

import com.example.brewery_api.dataloader.BeerReviewsDataLoader;
import com.example.brewery_api.dataloader.BreweryDataLoader;
import com.example.brewery_api.fetcher.BeerDataFetcher;
import com.example.brewery_api.fetcher.BreweryDataFetcher;
import com.example.brewery_api.fetcher.ReviewDataFetcher;
import com.example.brewery_api.model.Beer;
import com.example.brewery_api.model.Brewery;
import com.example.brewery_api.model.Review;
import com.example.brewery_api.service.BeerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Configuration
public class GraphQLConfig {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLConfig.class);

    private final BeerDataFetcher beerDataFetcher;
    private final BreweryDataFetcher breweryDataFetcher;
    private final ReviewDataFetcher reviewDataFetcher;
    private final BreweryDataLoader breweryDataLoader;
    private final BeerReviewsDataLoader beerReviewsDataLoader;
    private final BeerService beerService;
    private final BatchLoaderRegistry batchLoaderRegistry;

    @Value("${app.use.dataloaders:true}")
    private boolean useDataLoaders;

    public GraphQLConfig(BeerDataFetcher beerDataFetcher, BreweryDataFetcher breweryDataFetcher,
                         ReviewDataFetcher reviewDataFetcher, BreweryDataLoader breweryDataLoader,
                         BeerReviewsDataLoader beerReviewsDataLoader, BeerService beerService,
                         BatchLoaderRegistry batchLoaderRegistry) {
        this.beerDataFetcher = beerDataFetcher;
        this.breweryDataFetcher = breweryDataFetcher;
        this.reviewDataFetcher = reviewDataFetcher;
        this.breweryDataLoader = breweryDataLoader;
        this.beerReviewsDataLoader = beerReviewsDataLoader;
        this.beerService = beerService;
        this.batchLoaderRegistry = batchLoaderRegistry;
    }

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> {
            wiringBuilder.type("Query", typeWiring -> typeWiring
                    .dataFetcher("test", environment -> "GraphQL API is working!")
                    .dataFetcher("hello", environment -> "Hello from GraphQL!")
                    .dataFetcher("brewery", breweryDataFetcher.getBreweryById())
                    .dataFetcher("breweries", breweryDataFetcher.getAllBreweries())
                    .dataFetcher("beer", beerDataFetcher.getBeerById())
                    .dataFetcher("beers", beerDataFetcher.getAllBeers())
                    .dataFetcher("review", reviewDataFetcher.getReviewById())
                    .dataFetcher("reviews", reviewDataFetcher.getAllReviews())
            );

            wiringBuilder.type("Brewery", typeWiring -> {
                if (useDataLoaders) {
                    logger.info("Using DataLoader for Brewery.beers field");
                    return typeWiring.dataFetcher("beers", breweryDataFetcher.getBreweryBeersWithDataLoader());
                } else {
                    logger.info("Using regular fetcher for Brewery.beers field (N+1 issue)");
                    return typeWiring.dataFetcher("beers", breweryDataFetcher.getBreweryBeersWithoutDataLoader());
                }
            });

            wiringBuilder.type("Beer", typeWiring -> {
                if (useDataLoaders) {
                    logger.info("Using DataLoader for Beer.brewery and Beer.reviews fields");
                    return typeWiring
                            .dataFetcher("brewery", beerDataFetcher.getBeerBreweryWithDataLoader())
                            .dataFetcher("reviews", beerDataFetcher.getBeerReviewsWithDataLoader());
                } else {
                    logger.info("Using regular fetchers for Beer fields (N+1 issue)");
                    return typeWiring
                            .dataFetcher("brewery", beerDataFetcher.getBeerBreweryWithoutDataLoader())
                            .dataFetcher("reviews", beerDataFetcher.getBeerReviewsWithoutDataLoader());
                }
            });

            wiringBuilder.type("Review", typeWiring -> {
                if (useDataLoaders) {
                    logger.info("Using DataLoader for Review.beer field");
                    return typeWiring.dataFetcher("beer", reviewDataFetcher.getReviewBeerWithDataLoader());
                } else {
                    logger.info("Using regular fetcher for Review.beer field (N+1 issue)");
                    return typeWiring.dataFetcher("beer", reviewDataFetcher.getReviewBeerWithoutDataLoader());
                }
            });
        };
    }

    @PostConstruct
    public void configureBatchLoaderRegistry() {
        //  (object type)
        batchLoaderRegistry.forTypePair(String.class, Brewery.class)
                .registerBatchLoader((beerIds, environment) -> {
                    List<String> idList = new ArrayList<>(beerIds);
                    return Flux.fromIterable(breweryDataLoader.loadBreweriesByIds(idList));
                });

        // (list type)
        batchLoaderRegistry.forTypePair(String.class, List.class)
                .withName("beerReviewsLoader")
                .registerMappedBatchLoader((ids, environment) -> {
                    Map<String, List> result = new HashMap<>();
                    Map<String, List<Review>> typedResult = beerReviewsDataLoader.loadBatchOfReviews(new ArrayList<>(ids));

                    for (Map.Entry<String, List<Review>> entry : typedResult.entrySet()) {
                        result.put(entry.getKey(), entry.getValue());
                    }

                    return Mono.just(result);
                });

        //  (list type)
        batchLoaderRegistry.forTypePair(String.class, List.class)
                .withName("breweryBeersLoader")
                .registerMappedBatchLoader((beerIds, environment) -> {
                    CompletableFuture<Map<String, List>> future = CompletableFuture.supplyAsync(() -> {
                        logger.info("Loading beers for {} breweries with DataLoader", beerIds.size());
                        Map<String, List> result = new HashMap<>();
                        for (String breweryId : beerIds) {
                            try {
                                List beers = beerService.getBeersByBreweryId(breweryId);
                                result.put(breweryId, beers != null ? beers : Collections.emptyList());
                            } catch (Exception e) {
                                logger.error("Failed to load beers for breweryId {}: {}", breweryId, e.getMessage());
                                result.put(breweryId, Collections.emptyList());
                            }
                        }
                        return result;
                    });
                    return Mono.fromFuture(future);
                });

        // (object type)
        batchLoaderRegistry.forTypePair(String.class, Beer.class)
                .withName("beerLoader")
                .registerBatchLoader((beerIds, environment) -> {
                    List<String> idList = new ArrayList<>(beerIds);
                    CompletableFuture<List<Beer>> future = CompletableFuture.supplyAsync(() ->
                            beerService.getBeersByIds(idList));
                    return Mono.fromFuture(future).flatMapMany(Flux::fromIterable);
                });
    }
}