/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package com.github.phillipkruger.user.graphql;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.phillipkruger.user.backend.ScoreDB;
import com.github.phillipkruger.user.model.Score;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;

/**
 * Example of DataAccessLayer used to cache based on exact keys
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
@ApplicationScoped
public class DataAccessLayer {
    
    @Inject 
    ScoreDB scoreDB;

    private final Logger log = Logger.getLogger(DataAccessLayer.class.getName());
    
    public DataAccessLayer() {
        log.severe("Constructor");
    }

    //this cache uses relevant id as a key directly, we can easily invalidate it 
    @CacheResult(cacheName = "scoresForPerson")
    public List<Score> getScores(@CacheKey String personId) {
        log.severe("getScores for profile.person.personId " + personId);
        return scoreDB.getScores(personId);
    }

    @CacheInvalidate(cacheName = "scoresForPerson")
    public void addScore(@CacheKey String personId, String name, String value) {
        //TODO business logic to add new Score for person
    }

}
