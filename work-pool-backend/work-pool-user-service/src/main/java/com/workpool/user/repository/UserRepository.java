package com.workpool.user.repository;

import com.workpool.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthProviderId(String provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("{ 'location.state': ?0, 'active': true }")
    List<User> findActiveUsersByState(String state);

    @Query("{ 'location.city': ?0, 'active': true }")
    List<User> findActiveUsersByCity(String city);

    @Query("{ 'skills': { $in: ?0 }, 'location.state': ?1, 'active': true }")
    List<User> findBySkillsInAndState(List<String> skills, String state);
}
