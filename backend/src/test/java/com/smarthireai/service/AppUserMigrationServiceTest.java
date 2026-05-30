package com.smarthireai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.Role;
import com.smarthireai.entity.User;
import com.smarthireai.repository.AppUserRepository;
import com.smarthireai.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppUserMigrationServiceTest {

    @Autowired
    private AppUserMigrationService appUserMigrationService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void migratesExistingAppUsersIntoUsersWithoutChangingPasswordHash() {
        appUserRepository.save(new AppUser(
                "Recruiter User",
                "recruiter@example.com",
                "encoded-password",
                Role.RECRUITER
        ));

        int migrated = appUserMigrationService.migrateAppUsers();

        User migratedUser = userRepository.findByEmail("recruiter@example.com").orElseThrow();
        assertThat(migrated).isEqualTo(1);
        assertThat(migratedUser.getFullName()).isEqualTo("Recruiter User");
        assertThat(migratedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(migratedUser.getRole()).isEqualTo(User.UserRole.RECRUITER);
    }

    @Test
    void skipsAppUsersWhenUserWithSameEmailAlreadyExists() {
        appUserRepository.save(new AppUser(
                "Old Recruiter",
                "recruiter@example.com",
                "old-password",
                Role.RECRUITER
        ));
        userRepository.save(new User(
                "recruiter@example.com",
                "current-password",
                "Current Recruiter",
                User.UserRole.RECRUITER
        ));

        int migrated = appUserMigrationService.migrateAppUsers();

        User existingUser = userRepository.findByEmail("recruiter@example.com").orElseThrow();
        assertThat(migrated).isZero();
        assertThat(existingUser.getFullName()).isEqualTo("Current Recruiter");
        assertThat(existingUser.getPassword()).isEqualTo("current-password");
    }
}
