package com.smarthireai.repository;

import com.smarthireai.entity.AppUser;
import com.smarthireai.entity.Candidate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByUser(AppUser user);
}
