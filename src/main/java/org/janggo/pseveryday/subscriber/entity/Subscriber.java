package org.janggo.pseveryday.subscriber.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
                .map(TagPreference::getTagName)
                .collect(Collectors.toList());
    }

    public void addTagPreference(String tagName) {
        TagPreference tagPreference = new TagPreference();
        tagPreference.setSubscriber(this);
        tagPreference.setTagName(tagName);
        tagPreferences.add(tagPreference);
    }

    public void setTagPreferences(List<String> tagNames) {
        tagPreferences.clear();

        if (tagNames != null) {
            tagNames.forEach(this::addTagPreference);
        }
    }
}
