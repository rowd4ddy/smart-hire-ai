package com.smarthireai.service;

import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.User;
import com.smarthireai.repository.AppUserRepository;
import com.smarthireai.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserMigrationService implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final UserRepository userRepository;

    public AppUserMigrationService(AppUserRepository appUserRepository, UserRepository userRepository) {
        this.appUserRepository = appUserRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateAppUsers();
    }

    @Transactional
    public int migrateAppUsers() {
        int migrated = 0;

        for (AppUser appUser : appUserRepository.findAll()) {
            String email = appUser.getEmail() == null ? null : appUser.getEmail().trim().toLowerCase();

            if (email == null || email.isBlank() || userRepository.existsByEmail(email)) {
                continue;
            }

            User user = new User(
                    email,
                    appUser.getPassword(),
                    appUser.getFullName(),
                    User.UserRole.valueOf(appUser.getRole().name())
            );
            userRepository.save(user);
            migrated++;
        }

        return migrated;
    }
}
