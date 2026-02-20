package com.gamingplatform.config;

import com.gamingplatform.entity.UserProfile;
import com.gamingplatform.repository.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDefaultUser(UserProfileRepository userProfileRepository) {
        return args -> {
            if (userProfileRepository.count() == 0) {
                UserProfile user = new UserProfile();
                user.setUsername("demo_user");
                userProfileRepository.save(user);
            }
        };
    }
}
