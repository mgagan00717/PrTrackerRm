package com.example.demo.repository;

import com.example.demo.model.PR;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PRRepository extends JpaRepository<PR, Long> {
    List<PR> findByStatus(String status);
    List<PR> findByAuthorContainingIgnoreCase(String author);
    List<PR> findByRepoName(String repoName);
}

