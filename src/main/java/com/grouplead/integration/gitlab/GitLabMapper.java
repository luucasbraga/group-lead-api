package com.grouplead.integration.gitlab;

import com.grouplead.domain.entity.Commit;
import com.grouplead.domain.entity.MergeRequest;
import com.grouplead.domain.enums.MergeRequestStatus;
import com.grouplead.integration.gitlab.dto.GitLabCommit;
import com.grouplead.integration.gitlab.dto.GitLabMergeRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class GitLabMapper {

    private static final DateTimeFormatter GITLAB_DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public Commit toCommit(GitLabCommit gitLabCommit, String projectId) {
        return Commit.builder()
                .sha(gitLabCommit.getId())
                .message(gitLabCommit.getMessage())
                .additions(gitLabCommit.getStats() != null ? gitLabCommit.getStats().getAdditions() : 0)
                .deletions(gitLabCommit.getStats() != null ? gitLabCommit.getStats().getDeletions() : 0)
                .filesChanged(0) // GitLab doesn't provide this directly
                .projectId(projectId)
                .committedAt(parseDateTime(gitLabCommit.getCommittedDate()))
                .build();
    }

    public MergeRequest toMergeRequest(GitLabMergeRequest gitLabMR) {
        return MergeRequest.builder()
                .externalId(String.valueOf(gitLabMR.getIid()))
                .projectId(String.valueOf(gitLabMR.getProjectId()))
                .title(gitLabMR.getTitle())
                .description(gitLabMR.getDescription())
                .sourceBranch(gitLabMR.getSourceBranch())
                .targetBranch(gitLabMR.getTargetBranch())
                .status(mapStatus(gitLabMR.getState()))
                .commentsCount(gitLabMR.getUserNotesCount() != null ? gitLabMR.getUserNotesCount() : 0)
                .createdAt(parseDateTime(gitLabMR.getCreatedAt()))
                .mergedAt(parseDateTime(gitLabMR.getMergedAt()))
                .closedAt(parseDateTime(gitLabMR.getClosedAt()))
                .build();
    }

    private MergeRequestStatus mapStatus(String state) {
        if (state == null) return MergeRequestStatus.OPEN;

        return switch (state.toLowerCase()) {
            case "merged" -> MergeRequestStatus.MERGED;
            case "closed" -> MergeRequestStatus.CLOSED;
            case "locked" -> MergeRequestStatus.LOCKED;
            default -> MergeRequestStatus.OPEN;
        };
    }

    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, GITLAB_DATETIME_FORMAT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateString.substring(0, 19));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
