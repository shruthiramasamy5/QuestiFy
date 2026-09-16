package com.questify.auth.config;

import com.questify.auth.entity.Role;
import com.questify.auth.entity.User;
import com.questify.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Dev-only convenience: on an empty users table, creates one login per role
 * so the frontend can be exercised end-to-end without a manual SQL insert.
 * Demo password for every seeded account: Questify@123
 *
 * Disable by not activating the "dev" profile once real institution-admin
 * driven user provisioning exists.
 */
@Configuration
@Profile("dev")
public class DemoDataSeeder {

    @Bean
    CommandLineRunner seedDemoUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String demoPassword = passwordEncoder.encode("Questify@123");

            seedIfMissing(userRepository, "Ada Super Admin", "superadmin@questify.dev", demoPassword, Role.SUPER_ADMIN, null, null);
            seedIfMissing(userRepository, "Priya Institution Admin", "admin@questify.dev", demoPassword, Role.INSTITUTION_ADMIN, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Rahul Faculty", "faculty@questify.dev", demoPassword, Role.FACULTY, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Meena HOD", "hod@questify.dev", demoPassword, Role.HOD, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Karthik Reviewer", "reviewer@questify.dev", demoPassword, Role.REVIEWER, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Divya Coordinator", "coordinator@questify.dev", demoPassword, Role.COURSE_COORDINATOR, "Demo Institute of Technology", 1L);

            // Seed @questify.com demo accounts from the uploaded demo HTML
            seedIfMissing(userRepository, "Platform Super Admin", "superadmin@questify.com", demoPassword, Role.SUPER_ADMIN, null, null);
            seedIfMissing(userRepository, "Institution Admin", "institutionadmin@questify.com", demoPassword, Role.INSTITUTION_ADMIN, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Dr. Ramesh Kumar", "faculty@questify.com", demoPassword, Role.FACULTY, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Dr. Anitha Suresh", "hod@questify.com", demoPassword, Role.HOD, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Arjun Nambiar", "reviewer@questify.com", demoPassword, Role.REVIEWER, "Demo Institute of Technology", 1L);
            seedIfMissing(userRepository, "Dr. Ramesh Kumar", "coordinator@questify.com", demoPassword, Role.COURSE_COORDINATOR, "Demo Institute of Technology", 1L);
        };
    }

    private void seedIfMissing(UserRepository repo, String name, String email, String passwordHash, Role role, String instName, Long instId) {
        if (!repo.existsByEmailIgnoreCase(email)) {
            User user = new User(name, email, passwordHash, role, instName, instId);
            user.setActive(true);
            repo.save(user);
        }
    }
}
