package com.gamingplatform.repository;

import com.gamingplatform.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    long countByUser_Id(Long userId);
}
