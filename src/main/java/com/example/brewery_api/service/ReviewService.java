package com.example.brewery_api.service;

import com.example.brewery_api.model.Review;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final Map<String, Review> reviews = new HashMap<>();

    public void addReview(Review review) {
        reviews.put(review.getId(), review);
    }

    public Review getReviewById(String id) {
        return reviews.get(id);
    }

    public List<Review> getAllReviews() {
        return new ArrayList<>(reviews.values());
    }

    public List<Review> getReviewsByIds(Collection<String> ids) {
        return ids.stream()
                .map(this::getReviewById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByBeerId(String beerId) {
        return reviews.values().stream()
                .filter(review -> review.getBeerId().equals(beerId))
                .collect(Collectors.toList());
    }

    public Map<String, List<Review>> getReviewsByBeerIds(Set<String> beerIds) {
        Map<String, List<Review>> reviewsByBeerId = new HashMap<>();
        beerIds.forEach(beerId ->
                reviewsByBeerId.put(beerId, getReviewsByBeerId(beerId))
        );
        return reviewsByBeerId;
    }

    public int getReviewCount() {
        return reviews.size();
    }
}