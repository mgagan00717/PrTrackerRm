package com.example.demo.controller;

import com.example.demo.model.PR;
import com.example.demo.service.PRService;
import com.example.demo.service.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PRController {

    private static final Logger log = LoggerFactory.getLogger(PRController.class);

    @Autowired
    private PRService prService;
    @Autowired
    private GithubService githubService;

    @GetMapping("/")
    public String home() {
        return "redirect:/prs";
    }

    @GetMapping("/prs")
    public String listPRs(Model model) {
        log.info("GET /prs -> Loading PR dashboard");
        List<PR> allPRs = prService.getAll();
        log.info("Total PRs loaded: {}", allPRs.size());

        List<PR> openPrs = allPRs.stream()
                .filter(pr -> "Open".equals(pr.getStatus()) || "Draft".equals(pr.getStatus()))
                .collect(Collectors.toList());
        List<PR> mergedPrs = allPRs.stream()
                .filter(pr -> "Merged".equals(pr.getStatus()))
                .collect(Collectors.toList());
        List<PR> otherPrs = allPRs.stream()
                .filter(pr -> !"Open".equals(pr.getStatus()) && !"Draft".equals(pr.getStatus()) && !"Merged".equals(pr.getStatus()))
                .collect(Collectors.toList());

        log.info("Open: {}, Merged: {}, Other: {}", openPrs.size(), mergedPrs.size(), otherPrs.size());

        model.addAttribute("prs", allPRs);
        model.addAttribute("openPrs", openPrs);
        model.addAttribute("mergedPrs", mergedPrs);
        model.addAttribute("otherPrs", otherPrs);
        return "pr_dashboard";
    }

    @PostMapping("/prs/add")
    public String addPR(@RequestParam String prUrl) {
        log.info("POST /prs/add -> Received PR URL: {}", prUrl);
        try {
            Optional<PR> prOpt = githubService.fetchPRDetails(prUrl);
            if (prOpt.isPresent()) {
                PR saved = prService.save(prOpt.get());
                log.info("PR saved successfully -> ID: {}, Status: {}, Author: {}", saved.getId(), saved.getStatus(), saved.getAuthor());
            } else {
                log.warn("Could not fetch PR details for URL: {}. Check if the URL is valid and GitHub token is configured.", prUrl);
            }
        } catch (Exception e) {
            log.error("Error adding PR: {}", prUrl, e);
        }
        return "redirect:/prs";
    }

    @PostMapping("/prs/{id}/wiki")
    public String addToWiki(@PathVariable Long id, @RequestParam String wikiUrl) {
        log.info("POST /prs/{}/wiki -> Marking PR as added to wiki, URL: {}", id, wikiUrl);
        prService.addToWiki(id, wikiUrl);
        return "redirect:/prs";
    }
}
