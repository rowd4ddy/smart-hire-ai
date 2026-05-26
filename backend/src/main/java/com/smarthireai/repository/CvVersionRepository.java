package com.smarthireai.repository;

import com.smarthireai.entity.CvVersion;
import com.smarthireai.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvVersionRepository extends JpaRepository<CvVersion, Long> {

    List<CvVersion> findByCandidateOrderByVersionNumberDesc(User candidate);

    Optional<CvVersion> findTopByCandidateOrderByVersionNumberDesc(User candidate);
}
