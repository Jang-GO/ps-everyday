package org.janggo.pseveryday.domain.subscriber.repository;

import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmail(String email);

    Optional<Subscriber> findByEmail(String email);
}
