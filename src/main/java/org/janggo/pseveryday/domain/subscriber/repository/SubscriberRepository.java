package org.janggo.pseveryday.domain.subscriber.repository;

import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    @EntityGraph(attributePaths = {
            "tagPreferences",
            "tagPreferences.tag"
    })
    List<Subscriber> findAll();

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    void deleteByEmail(String email);

    Optional<Subscriber> findByEmail(String email);
}
