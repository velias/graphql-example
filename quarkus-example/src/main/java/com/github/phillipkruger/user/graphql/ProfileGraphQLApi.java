package com.github.phillipkruger.user.graphql;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import com.github.phillipkruger.user.backend.EventDB;
import com.github.phillipkruger.user.backend.PersonDB;
import com.github.phillipkruger.user.backend.ScoreDB;
import com.github.phillipkruger.user.model.Event;
import com.github.phillipkruger.user.model.Person;
import com.github.phillipkruger.user.model.Profile;
import com.github.phillipkruger.user.model.Score;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;

@GraphQLApi
@ApplicationScoped
public class ProfileGraphQLApi {
    
    private final Logger log = Logger.getLogger(ProfileGraphQLApi.class.getName());
    
    public ProfileGraphQLApi() {
        log.severe("Constructor");
    }
    
    @Query("profileFull")
    @Description("Get a person's profile using the person's Id (same a the REST service)")
    public Profile getProfileFull(int personId) {
        Person person = personDB.getPerson(personId);
        List<Score> scores = scoreDB.getScores(person.getIdNumber());
        Profile profile = new Profile();
        profile.setId(person.getIdNumber());
        profile.setPerson(person);
        profile.setScores(scores);
        return profile;
    }
    
    @Query("profile")
    @Description("Get a person's profile using the person's Id")
    //this cache works OK
    @CacheResult(cacheName = "profileByPersonId")
    public Profile getProfile(int personId) {
        Person person = personDB.getPerson(personId);
        
        Profile profile = new Profile();
        profile.setId(person.getIdNumber());
        profile.setPerson(person);
        
        return profile;
    }
    
    /**
     * This cache requires equals() and hashCode() in the Profile object to be based on person.getIdNumber()!
     * Cache Invalidation may be pain in this case also as you have to construct two objects and use them as input for method which invalidates using @CacheInvalidate. 
     */
    @CacheResult(cacheName = "scoresForProfile")
    public List<Score> getScores(@Source Profile profile) {
        log.severe("getScores for profile " + profile);
        Person person = profile.getPerson();
        //changed to DAL component call, which can cache based on id directly
        return dataAccessLayer.getScores(person.getIdNumber());
        //return scoreDB.getScores(person.getIdNumber());
    }
    
    @Query("person")
    public Person getPerson(@Name("personId") int personId){
        return personDB.getPerson(personId);
    }
    
    public List<Score> getScores(@Source Person person) {
        return scoreDB.getScores(person.getIdNumber());
    }
    
    public List<Score> getScores2(@Source Person person) throws ScoresNotAvailableException {
        throw new ScoresNotAvailableException("Scores for person [" + person.getIdNumber() + "] is not available");
    }
    
    // List Queries 
    
    @Query
    public List<Person> getPeople(){
        return personDB.getPeople();
    }
    
    @Query
    public List<Score> getPersonScores(int personId){
        Person person = personDB.getPerson(personId);
        return scoreDB.getScores(person.getIdNumber());
    }
    
    // Mutations
    
    @Mutation
    public Person updatePerson(Person person){
        return personDB.updatePerson(person);    
    }
    
    @Mutation
    public Person deletePerson(int id){
        return personDB.deletePerson(id);    
    }
    
    // Complex graphs (Source of a source)
    // this cache requires equals() and hashCode() in the Score object based on score.getId(). Score object filled with id is necessary for invalidation also.
    @CacheResult(cacheName = "eventsForScore")
    public List<Event> getEvents(@Source Score score) {
        return eventDB.getEvents(score.getId());
    }
    
//    public List<Double> getRandomNumbers(@Source Score score){
//        List<Double> randomNumbers = new ArrayList<>();
//        randomNumbers.add(Math.random());
//        randomNumbers.add(Math.random());
//        randomNumbers.add(Math.random());
//        return randomNumbers;
//    }
    
    // Default values
    @Query
    public List<Person> getPersonsWithSurname(
            @DefaultValue("Kruger") String surname) {
    
        return personDB.getPeopleWithSurname(surname);
    }
    
    @Inject 
    PersonDB personDB;
    
    @Inject 
    ScoreDB scoreDB;
    
    @Inject 
    EventDB eventDB;
    
    @Inject
    DataAccessLayer dataAccessLayer;

}
