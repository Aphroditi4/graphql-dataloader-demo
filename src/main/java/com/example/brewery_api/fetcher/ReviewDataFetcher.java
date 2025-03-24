package com.example.brewery_api.fetcher;

import com.example.brewery_api.model.Beer;
import com.example.brewery_api.model.Review;
import com.example.brewery_api.service.BeerService;
import com.example.brewery_api.service.ReviewService;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ReviewDataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDataFetcher.class);
    private final ReviewService reviewService;
    private final BeerService beerService;

    public ReviewDataFetcher(ReviewService reviewService, BeerService beerService) {
        this.reviewService = reviewService;
        this.beerService = beerService;
    }

    public DataFetcher<Review> getReviewById() {
        return environment -> {
            String reviewId = environment.getArgument("id");
            logger.info("Fetching review with id: {}", reviewId);
            return reviewService.getReviewById(reviewId);
        };
    }

    public DataFetcher<List<Review>> getAllReviews() {
        return environment -> {
            logger.info("Fetching all reviews");
            return reviewService.getAllReviews();
        };
    }

    public DataFetcher<Beer> getReviewBeerWithoutDataLoader() {
        return environment -> {
            Review review = environment.getSource();
            logger.info("Fetching beer for review {}: {} (without DataLoader - N+1 issue)",
                    review.getId(), review.getAuthor());
            return beerService.getBeerById(review.getBeerId());
        };
    }

    public DataFetcher<CompletableFuture<Beer>> getReviewBeerWithDataLoader() {
        return environment -> {
            Review review = environment.getSource();
            DataLoader<String, Beer> dataLoader = environment.getDataLoader("beerLoader");
            logger.info("Fetching beer for review {}: {} (with DataLoader)",
                    review.getId(), review.getAuthor());
            return dataLoader.load(review.getBeerId());
        };
    }
}