package org.janggo.pseveryday.problem.repository;

import org.janggo.pseveryday.problem.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByDisplayName(String name);

    boolean existsByDisplayName(String name);
}
