package org.janggo.pseveryday.util;

import lombok.RequiredArgsConstructor;
import org.janggo.pseveryday.domain.problem.entity.Tag;
import org.janggo.pseveryday.domain.problem.repository.TagRepository;
import org.janggo.pseveryday.domain.subscriber.entity.Subscriber;
import org.janggo.pseveryday.domain.subscriber.entity.TagPreference;
import org.janggo.pseveryday.domain.subscriber.entity.TierPreference;
import org.janggo.pseveryday.domain.subscriber.repository.SubscriberRepository;
import org.janggo.pseveryday.domain.subscriber.repository.TagPreferenceRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class TestSubscriberGenerator {
    private final SubscriberRepository subscriberRepository;
    private final TagRepository tagRepository;
    private final TagPreferenceRepository tagPreferenceRepository;

    public void generateTestSubscribers(int count) {
        List<Tag> allTags = tagRepository.findAll();
        Random random = new Random();

        List<Subscriber> subscribers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Subscriber subscriber = new Subscriber("test_user_" + i + "@example.com", new TierPreference(1,30));

            // 50%의 확률로 티어 선호도 설정
            if (random.nextBoolean()) {
                int minTier = random.nextInt(20) + 1; // 1~20 사이 랜덤 티어
                int maxTier = Math.min(30, minTier + random.nextInt(10) + 1);
                subscriber.updateTierPreference(new TierPreference(minTier, maxTier));
            }

            int tagCount = random.nextInt(6); // 0~5개 태그
            if (tagCount > 0) {
                Collections.shuffle(allTags);
                List<Tag> selectedTags = allTags.subList(0, Math.min(tagCount, allTags.size()));
                subscriber.setTagPreferences(selectedTags);
            }

            subscribers.add(subscriber);

        }
        // 벌크 저장
        subscriberRepository.saveAll(subscribers);
    }
}
