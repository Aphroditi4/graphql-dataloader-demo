package com.example.brewery_api.model;

import java.util.List;

public class Brewery {
    private String id;
    private String name;
    private String location;
    private Integer established;
    private String description;
    private List<String> beerIds;

    public Brewery() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getEstablished() {
        return established;
    }

    public void setEstablished(Integer established) {
        this.established = established;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getBeerIds() {
        return beerIds;
    }

    public void setBeerIds(List<String> beerIds) {
        this.beerIds = beerIds;
    }
}