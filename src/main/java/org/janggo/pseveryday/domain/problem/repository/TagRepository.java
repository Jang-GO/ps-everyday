package org.janggo.pseveryday.domain.problem.repository;

import org.janggo.pseveryday.domain.problem.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByDisplayName(String displayName);
    Optional<Tag> findByKey(String key);

    boolean existsByDisplayName(String name);
}
