package org.janggo.pseveryday.domain.subscriber.repository;

import org.janggo.pseveryday.domain.subscriber.entity.TagPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagPreferenceRepository extends JpaRepository<TagPreference, Long> {
}
