package com.example.brewery_api.dataloader;

import com.example.brewery_api.model.Review;
import com.example.brewery_api.service.ReviewService;
import org.dataloader.MappedBatchLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
public class BeerReviewsDataLoader implements MappedBatchLoader<String, List<Review>> {
    private static final Logger logger = LoggerFactory.getLogger(BeerReviewsDataLoader.class);
    private final ReviewService reviewService;

    public BeerReviewsDataLoader(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public CompletableFuture<Map<String, List<Review>>> load(Set<String> beerIds) {
        logger.info("Loading reviews for beer IDs: {}", beerIds);
        Map<String, List<Review>> result = new HashMap<>();
        for (String beerId : beerIds) {
            try {
                List<Review> reviews = reviewService.getReviewsByBeerId(beerId);
                result.put(beerId, reviews != null ? reviews : Collections.emptyList());
            } catch (Exception e) {
                logger.error("Failed to load reviews for beerId {}: {}", beerId, e.getMessage());
                result.put(beerId, Collections.emptyList());
            }
        }
        return CompletableFuture.completedFuture(result);
    }

    // Helper method to adapt to Mono for BatchLoaderRegistry
    public Mono<Map<String, List<Review>>> loadReactive(Set<String> beerIds) {
        return Mono.fromFuture(load(beerIds));
    }

    // Overload for List<String> to match BatchLoaderRegistry
    public Mono<Map<String, List<Review>>> loadReactive(List<String> beerIds) {
        return loadReactive(Set.copyOf(beerIds));
    }

    // New method for direct loading of reviews to avoid ambiguity
    public Map<String, List<Review>> loadBatchOfReviews(List<String> beerIds) {
        logger.info("Direct loading reviews for beer IDs: {}", beerIds);
        Map<String, List<Review>> result = new HashMap<>();
        for (String beerId : beerIds) {
            try {
                List<Review> reviews = reviewService.getReviewsByBeerId(beerId);
                result.put(beerId, reviews != null ? reviews : Collections.emptyList());
            } catch (Exception e) {
                logger.error("Failed to load reviews for beerId {}: {}", beerId, e.getMessage());
                result.put(beerId, Collections.emptyList());
            }
        }
        return result;
    }
}