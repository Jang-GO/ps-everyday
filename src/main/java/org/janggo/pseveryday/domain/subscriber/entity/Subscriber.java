package org.janggo.pseveryday.domain.subscriber.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.janggo.pseveryday.domain.problem.entity.Tag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "subscriber")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private LocalDateTime subscribedAt;

    @Embedded
    private TierPreference tierPreference;

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TagPreference> tagPreferences = new ArrayList<>();

    public Subscriber(String email, TierPreference tierPreference) {
        this.email = email;
        this.tierPreference = tierPreference;
        this.subscribedAt = LocalDateTime.now();
    }

    public List<String> getTagPreferenceNames() {
        return tagPreferences.stream()
                .map(tp -> tp.getTag().getDisplayName())  // Tag 엔티티의 displayName 사용
                .collect(Collectors.toList());
    }

    public void addTagPreference(Tag tag) {  // Tag 엔티티를 직접 받도록 수정
        TagPreference tagPreference = new TagPreference();
        tagPreference.setSubscriber(this);
        tagPreference.setTag(tag);
        tagPreferences.add(tagPreference);
    }

    public void setTagPreferences(List<Tag> tags) {  // Tag 엔티티 리스트를 받도록 수정
        tagPreferences.clear();
        if (tags != null) {
            tags.forEach(this::addTagPreference);
        }
    }

    public void updateTierPreference(TierPreference tierPreference) {
        this.tierPreference = tierPreference;
    }

    public void clearTagPreferences() {
        tagPreferences.clear();
    }
}
