package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import java.util.List;

@Entity
@Table(name = "pull_requests")
public class PR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String prUrl;
    private String author;
    private String status;
    private String mergedBy;
    private String fromBranch;
    private String toBranch;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> ciDetails;
    private String conflicts;
    private String outOfDate;
    private int approvals;
    private int changeRequests;
    private boolean addedToWiki;
    private String repoName;
    private String targetReleaseVersion;
    private String notes;
    private java.time.LocalDateTime lastChecked;
    private java.time.LocalDateTime reminder;
    private String wikiUrl;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPrUrl() { return prUrl; }
    public void setPrUrl(String prUrl) { this.prUrl = prUrl; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMergedBy() { return mergedBy; }
    public void setMergedBy(String mergedBy) { this.mergedBy = mergedBy; }
    public String getFromBranch() { return fromBranch; }
    public void setFromBranch(String fromBranch) { this.fromBranch = fromBranch; }
    public String getToBranch() { return toBranch; }
    public void setToBranch(String toBranch) { this.toBranch = toBranch; }
    public List<String> getCiDetails() { return ciDetails; }
    public void setCiDetails(List<String> ciDetails) { this.ciDetails = ciDetails; }
    public String getConflicts() { return conflicts; }
    public void setConflicts(String conflicts) { this.conflicts = conflicts; }
    public String getOutOfDate() { return outOfDate; }
    public void setOutOfDate(String outOfDate) { this.outOfDate = outOfDate; }
    public int getApprovals() { return approvals; }
    public void setApprovals(int approvals) { this.approvals = approvals; }
    public int getChangeRequests() { return changeRequests; }
    public void setChangeRequests(int changeRequests) { this.changeRequests = changeRequests; }
    public boolean isAddedToWiki() { return addedToWiki; }
    public void setAddedToWiki(boolean addedToWiki) { this.addedToWiki = addedToWiki; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public String getTargetReleaseVersion() { return targetReleaseVersion; }
    public void setTargetReleaseVersion(String targetReleaseVersion) { this.targetReleaseVersion = targetReleaseVersion; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public java.time.LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(java.time.LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
    public java.time.LocalDateTime getReminder() { return reminder; }
    public void setReminder(java.time.LocalDateTime reminder) { this.reminder = reminder; }
    public String getWikiUrl() { return wikiUrl; }
    public void setWikiUrl(String wikiUrl) { this.wikiUrl = wikiUrl; }
}
