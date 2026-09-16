package com.questify.auth.repository;

import com.questify.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    java.util.List<User> findByInstitutionId(Long institutionId);

    java.util.List<User> findByInstitutionIdAndRole(Long institutionId, com.questify.auth.entity.Role role);
}
