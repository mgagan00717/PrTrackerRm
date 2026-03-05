package com.example.demo.service;

import com.example.demo.model.PR;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;

@Service
public class GithubService {

    private static final Logger log = LoggerFactory.getLogger(GithubService.class);

    @Value("${github.token}")
    private String githubToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Optional<PR> fetchPRDetails(String prUrl) {
        log.info("========== Fetching PR details for URL: {} ==========" , prUrl);
        try {
            URL url = new URL(prUrl);
            String host = url.getHost();
            String[] parts = url.getPath().split("/");
            String owner = parts[1];
            String repo = parts[2];
            String prNumber = parts[4];
            String apiBase;
            if (host.equals("github.com")) {
                apiBase = "https://api.github.com";
            } else {
                apiBase = "https://" + host + "/api/v3";
            }
            String apiUrl = String.format("%s/repos/%s/%s/pulls/%s", apiBase, owner, repo, prNumber);
            log.info("GitHub API URL: {}", apiUrl);
            HttpEntity<String> entity = new HttpEntity<>(null, createHeaders());
            ResponseEntity<Map<String, Object>> prResp = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>(){});
            log.info("GitHub API response status: {}", prResp.getStatusCode());
            if (!prResp.getStatusCode().equals(HttpStatus.OK)) {
                log.error("GitHub API returned non-OK status: {}", prResp.getStatusCode());
                return Optional.empty();
            }
            Map<String, Object> prData = prResp.getBody();
            if (prData == null) {
                log.error("GitHub API returned null body");
                return Optional.empty();
            }

            PR pr = new PR();
            pr.setPrUrl(prUrl);

            String author = ((Map<String, Object>) prData.get("user")).get("login").toString();
            String state = prData.get("state").toString();
            boolean merged = Boolean.TRUE.equals(prData.get("merged"));
            boolean draft = Boolean.TRUE.equals(prData.get("draft"));
            String mergedBy = prData.get("merged_by") != null ? ((Map<String, Object>) prData.get("merged_by")).get("login").toString() : "";
            String fromBranch = ((Map<String, Object>) prData.get("head")).get("ref").toString();
            String toBranch = ((Map<String, Object>) prData.get("base")).get("ref").toString();
            String headSha = ((Map<String, Object>) prData.get("head")).get("sha").toString();
            String mergeableState = prData.get("mergeable_state") != null ? prData.get("mergeable_state").toString() : "unknown";

            log.info("PR Author: {}", author);
            log.info("PR State: {}, Merged: {}, Draft: {}", state, merged, draft);
            log.info("Merged By: {}", mergedBy.isEmpty() ? "N/A" : mergedBy);
            log.info("Branches: {} -> {}", fromBranch, toBranch);
            log.info("Head SHA: {}", headSha);
            log.info("Mergeable State: {}", mergeableState);

            // Status mapping
            String prStatus;
            if ("open".equals(state) && draft) {
                prStatus = "Draft";
            } else if ("open".equals(state)) {
                prStatus = "Open";
            } else if ("closed".equals(state) && merged) {
                prStatus = "Merged";
            } else if ("closed".equals(state)) {
                prStatus = "Closed";
            } else {
                prStatus = "Unknown";
            }
            log.info("Computed PR Status: {}", prStatus);

            // Conflicts & Out of date
            String conflictStatus;
            String outOfDate;
            switch (mergeableState) {
                case "clean": conflictStatus = "No conflicts"; outOfDate = "No"; break;
                case "dirty": conflictStatus = "Has conflicts"; outOfDate = "No"; break;
                case "behind": conflictStatus = "No conflicts"; outOfDate = "Yes - Out of date"; break;
                case "blocked": conflictStatus = "Blocked (checks/reviews)"; outOfDate = "No"; break;
                default: conflictStatus = "Unknown (" + mergeableState + ")"; outOfDate = "Unknown"; break;
            }
            log.info("Conflicts: {}, Out of Date: {}", conflictStatus, outOfDate);

            // CI Details
            log.info("Fetching CI check runs for commit: {}", headSha);
            String checksUrl = String.format("https://api.github.com/repos/%s/%s/commits/%s/check-runs", owner, repo, headSha);
            List<String> ciDetails = new ArrayList<>();
            try {
                ResponseEntity<Map<String, Object>> checksResp = restTemplate.exchange(checksUrl, HttpMethod.GET, new HttpEntity<>(createHeaders()), new ParameterizedTypeReference<Map<String, Object>>(){});
                if (checksResp.getStatusCode().equals(HttpStatus.OK) && checksResp.getBody() != null) {
                    List<Map<String, Object>> checkRuns = (List<Map<String, Object>>) checksResp.getBody().get("check_runs");
                    if (checkRuns != null) {
                        for (Map<String, Object> run : checkRuns) {
                            String conclusion = run.get("conclusion") != null ? run.get("conclusion").toString() : "pending";
                            String name = run.get("name").toString();
                            String icon = "success".equals(conclusion) ? "✅" : "failure".equals(conclusion) ? "❌" : "⏳";
                            ciDetails.add(icon + " " + name);
                        }
                    }
                    log.info("CI Details found: {} check(s)", ciDetails.size());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch CI details: {}", e.getMessage());
                ciDetails.add("Unable to fetch CI details");
            }

            // Reviews
            log.info("Fetching reviews for PR #{}...", prNumber);
            String reviewsUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s/reviews", owner, repo, prNumber);
            int approvals = 0;
            int changeRequests = 0;
            try {
                ResponseEntity<List<Map<String, Object>>> reviewsResp = restTemplate.exchange(reviewsUrl, HttpMethod.GET, new HttpEntity<>(createHeaders()), new ParameterizedTypeReference<List<Map<String, Object>>>(){});
                if (reviewsResp.getStatusCode().equals(HttpStatus.OK) && reviewsResp.getBody() != null) {
                    Map<String, String> seenUsers = new HashMap<>();
                    for (Map<String, Object> review : reviewsResp.getBody()) {
                        String user = ((Map<String, Object>) review.get("user")).get("login").toString();
                        String reviewState = review.get("state").toString();
                        seenUsers.put(user, reviewState);
                    }
                    for (String val : seenUsers.values()) {
                        if ("APPROVED".equals(val)) approvals++;
                        else if ("CHANGES_REQUESTED".equals(val)) changeRequests++;
                    }
                    log.info("Reviews -> Approvals: {}, Change Requests: {}", approvals, changeRequests);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch reviews: {}", e.getMessage());
            }

            // Target Release Version extraction
            String targetReleaseVersion = null;
            if (toBranch != null && toBranch.toLowerCase().startsWith("release")) {
                // Example: release-24.1 or release_24.1
                String[] releaseParts = toBranch.split("[-_]");
                if (releaseParts.length > 1) {
                    targetReleaseVersion = releaseParts[1];
                }
            }
            pr.setTargetReleaseVersion(targetReleaseVersion);

            pr.setAuthor(author);
            pr.setStatus(prStatus);
            pr.setMergedBy(mergedBy);
            pr.setFromBranch(fromBranch);
            pr.setToBranch(toBranch);
            pr.setRepoName(repo);
            pr.setCiDetails(ciDetails);
            pr.setConflicts(conflictStatus);
            pr.setOutOfDate(outOfDate);
            pr.setApprovals(approvals);
            pr.setChangeRequests(changeRequests);
            pr.setLastChecked(java.time.LocalDateTime.now());

            log.info("========== PR details fetched successfully for: {} ==========", prUrl);
            return Optional.of(pr);
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("========== 403 FORBIDDEN: SSO Authorization Required for: {} ==========", prUrl);
            log.error("Error Message: {}", e.getResponseBodyAsString());
            if (e.getResponseBodyAsString().contains("SAML")) {
                log.error("╔════════════════════════════════════════════════════════════════╗");
                log.error("║  🚨 SSO AUTHORIZATION REQUIRED!                                ║");
                log.error("║                                                                ║");
                log.error("║  Your GitHub token needs SSO authorization.                   ║");
                log.error("║                                                                ║");
                log.error("║  To fix this:                                                  ║");
                log.error("║  1. Go to: https://github.com/settings/tokens                 ║");
                log.error("║  2. Find your token and click 'Configure SSO'                 ║");
                log.error("║  3. Click 'Authorize' next to your organization               ║");
                log.error("║  4. Restart this application                                   ║");
                log.error("║                                                                ║");
                log.error("║  See SETUP_GUIDE.md for detailed instructions                 ║");
                log.error("╚════════════════════════════════════════════════════════════════╝");
            }
            return Optional.empty();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("========== 401 UNAUTHORIZED: Invalid GitHub Token for: {} ==========", prUrl);
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║  🚨 INVALID GITHUB TOKEN!                                      ║");
            log.error("║                                                                ║");
            log.error("║  Your GitHub token is invalid or expired.                     ║");
            log.error("║                                                                ║");
            log.error("║  To fix this:                                                  ║");
            log.error("║  1. Check application.properties has correct token            ║");
            log.error("║  2. Verify token hasn't expired                               ║");
            log.error("║  3. Generate a new token if needed                            ║");
            log.error("║  4. Ensure token has 'repo' scope                             ║");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("========== 404 NOT FOUND: PR does not exist or no access: {} ==========", prUrl);
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║  ⚠️  PR NOT FOUND!                                             ║");
            log.error("║                                                                ║");
            log.error("║  Possible reasons:                                             ║");
            log.error("║  - PR URL is incorrect                                         ║");
            log.error("║  - PR doesn't exist                                            ║");
            log.error("║  - You don't have access to this repository                    ║");
            log.error("║  - Repository is private and token lacks access               ║");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            log.error("========== HTTP ERROR {} for: {} ==========", e.getStatusCode(), prUrl);
            log.error("Response: {}", e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("========== FAILED to fetch PR details for: {} ==========", prUrl, e);
            return Optional.empty();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }
}
