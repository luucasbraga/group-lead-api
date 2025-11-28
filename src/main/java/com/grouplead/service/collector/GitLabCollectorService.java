package com.grouplead.service.collector;

import com.grouplead.domain.entity.Commit;
import com.grouplead.domain.entity.Developer;
import com.grouplead.domain.entity.MergeRequest;
import com.grouplead.integration.gitlab.GitLabClient;
import com.grouplead.integration.gitlab.GitLabMapper;
import com.grouplead.integration.gitlab.dto.GitLabCommit;
import com.grouplead.integration.gitlab.dto.GitLabMergeRequest;
import com.grouplead.repository.CommitRepository;
import com.grouplead.repository.DeveloperRepository;
import com.grouplead.repository.MergeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitLabCollectorService {

    private final GitLabClient gitLabClient;
    private final GitLabMapper gitLabMapper;
    private final CommitRepository commitRepository;
    private final MergeRequestRepository mergeRequestRepository;
    private final DeveloperRepository developerRepository;

    @Transactional
    public CollectionResult collectCommits(LocalDateTime since) {
        log.info("Starting GitLab commit collection since {}", since);

        int totalSaved = 0;
        List<String> projectIds = gitLabClient.getProjectIds();

        for (String projectId : projectIds) {
            try {
                List<GitLabCommit> commits = gitLabClient.getCommits(projectId, since);

                for (GitLabCommit gitLabCommit : commits) {
                    if (!commitRepository.existsBySha(gitLabCommit.getId())) {
                        // Get commit details with stats
                        GitLabCommit details = gitLabClient.getCommitDetails(projectId, gitLabCommit.getId());
                        if (details == null) {
                            details = gitLabCommit;
                        }

                        Commit commit = gitLabMapper.toCommit(details, projectId);

                        // Link to developer
                        String email = details.getAuthorEmail();
                        developerRepository.findByEmail(email)
                                .ifPresent(commit::setDeveloper);

                        commitRepository.save(commit);
                        totalSaved++;
                    }
                }
            } catch (Exception e) {
                log.error("Error collecting commits for project {}", projectId, e);
            }
        }

        log.info("Collected {} commits from GitLab", totalSaved);
        return new CollectionResult("commits", totalSaved);
    }

    @Transactional
    public CollectionResult collectMergeRequests(LocalDateTime since) {
        log.info("Starting GitLab merge request collection since {}", since);

        int totalSaved = 0;
        List<String> projectIds = gitLabClient.getProjectIds();

        for (String projectId : projectIds) {
            try {
                // Collect merged MRs
                List<GitLabMergeRequest> mergedMRs = gitLabClient.getMergedMergeRequests(projectId, since);
                totalSaved += processMergeRequests(mergedMRs);

                // Collect open MRs
                List<GitLabMergeRequest> openMRs = gitLabClient.getMergeRequests(projectId, "opened");
                totalSaved += processMergeRequests(openMRs);
            } catch (Exception e) {
                log.error("Error collecting merge requests for project {}", projectId, e);
            }
        }

        log.info("Collected {} merge requests from GitLab", totalSaved);
        return new CollectionResult("merge_requests", totalSaved);
    }

    private int processMergeRequests(List<GitLabMergeRequest> mergeRequests) {
        int saved = 0;

        for (GitLabMergeRequest gitLabMR : mergeRequests) {
            try {
                String externalId = String.valueOf(gitLabMR.getIid());
                String projectId = String.valueOf(gitLabMR.getProjectId());

                var existingMR = mergeRequestRepository.findByExternalIdAndProjectId(externalId, projectId);

                if (existingMR.isPresent()) {
                    // Update existing MR
                    MergeRequest existing = existingMR.get();
                    MergeRequest updated = gitLabMapper.toMergeRequest(gitLabMR);

                    existing.setStatus(updated.getStatus());
                    existing.setMergedAt(updated.getMergedAt());
                    existing.setClosedAt(updated.getClosedAt());
                    existing.setCommentsCount(updated.getCommentsCount());

                    mergeRequestRepository.save(existing);
                } else {
                    MergeRequest mr = gitLabMapper.toMergeRequest(gitLabMR);

                    // Link to developer
                    if (gitLabMR.getAuthor() != null && gitLabMR.getAuthor().getEmail() != null) {
                        developerRepository.findByEmail(gitLabMR.getAuthor().getEmail())
                                .ifPresent(mr::setDeveloper);
                    }

                    mergeRequestRepository.save(mr);
                }
                saved++;
            } catch (Exception e) {
                log.error("Error processing merge request {}", gitLabMR.getIid(), e);
            }
        }

        return saved;
    }

    public record CollectionResult(String type, int count) {}
}
