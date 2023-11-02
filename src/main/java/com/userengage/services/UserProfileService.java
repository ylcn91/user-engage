package com.userengage.services;

import com.userengage.models.User;
import com.userengage.models.UserProfile;
import com.userengage.repositories.UserProfileRepository;
import com.userengage.repositories.UserRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import jakarta.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class UserProfileService {

    @Inject
    UserProfileRepository userProfileRepository;

    @Inject
    UserRepository userRepository;

    @Transactional
    public UserProfile createUserProfile(Long userId, String firstName, String lastName, String email, String phoneNumber, String address) {
        User user = userRepository.findById(userId);
        if (user == null) {
            log.error("User not found with id: {}", userId);
            throw new IllegalArgumentException("User not found");
        }

        UserProfile userProfile = new UserProfile(firstName, lastName, email);
        userProfile.setPhoneNumber(phoneNumber);
        userProfile.setAddress(address);
        userProfile.setUser(user);

        userProfileRepository.persist(userProfile);
        log.info("UserProfile created for user: {}", userId);

        return userProfile;
    }

    @Transactional
    public UserProfile updateUserProfile(Long userProfileId, String firstName, String lastName, String email, String phoneNumber, String address) {
        UserProfile userProfile = userProfileRepository.findById(userProfileId);
        if (userProfile == null) {
            log.error("UserProfile not found with id: {}", userProfileId);
            throw new IllegalArgumentException("UserProfile not found");
        }

        userProfile.updateProfile(firstName, lastName, email, phoneNumber, address);
        userProfileRepository.persist(userProfile);
        log.info("UserProfile updated: {}", userProfileId);

        return userProfile;
    }

    public UserProfile getUserProfile(Long userId) {
        return userProfileRepository.find("user_id", userId).firstResult();
    }
}
