package org.janggo.pseveryday.subscriber;

import org.janggo.pseveryday.subscriber.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
