package com.example.demo.service;

import com.example.demo.model.PR;
import com.example.demo.repository.PRRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Service
public class PRService {

    private static final Logger log = LoggerFactory.getLogger(PRService.class);

    @Autowired
    private PRRepository prRepository;

    public PR save(PR pr) {
        log.info("Saving PR to DB -> URL: {}, Author: {}, Status: {}", pr.getPrUrl(), pr.getAuthor(), pr.getStatus());
        PR saved = prRepository.save(pr);
        log.info("PR saved successfully with ID: {}", saved.getId());
        return saved;
    }

    public List<PR> getAll() {
        List<PR> prs = prRepository.findAll();
        log.info("Fetched {} PRs from DB", prs.size());
        return prs;
    }

    public List<PR> getByStatus(String status) {
        log.info("Fetching PRs with status: {}", status);
        List<PR> prs = prRepository.findByStatus(status);
        log.info("Found {} PRs with status: {}", prs.size(), status);
        return prs;
    }

    public Optional<PR> getById(Long id) {
        log.info("Fetching PR by ID: {}", id);
        return prRepository.findById(id);
    }

    public void addToWiki(Long id, String wikiUrl) {
        log.info("Adding PR ID: {} to Wiki with URL: {}", id, wikiUrl);
        prRepository.findById(id).ifPresent(pr -> {
            pr.setAddedToWiki(true);
            pr.setWikiUrl(wikiUrl);
            prRepository.save(pr);
            log.info("PR ID: {} marked as added to Wiki with URL: {}", id, wikiUrl);
        });
    }
}
