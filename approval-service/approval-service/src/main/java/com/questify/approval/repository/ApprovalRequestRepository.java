package com.questify.approval.repository;

import com.questify.approval.domain.ApprovalRequest;
import com.questify.approval.domain.ApprovalStage;
import com.questify.approval.domain.ApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);

    List<ApprovalRequest> findByStatusAndCurrentStageOrderByCreatedAtDesc(ApprovalStatus status,
                                                                         ApprovalStage stage);

    List<ApprovalRequest> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    Optional<ApprovalRequest> findByPaperIdAndStatus(Long paperId, ApprovalStatus status);

    @Query("select r from ApprovalRequest r left join fetch r.steps where r.id = :id")
    Optional<ApprovalRequest> findByIdWithSteps(@Param("id") Long id);
}
