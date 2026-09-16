package com.questify.approval.repository;

import com.questify.approval.domain.ApprovalStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByRequestIdOrderByTimestampAsc(Long requestId);
}
