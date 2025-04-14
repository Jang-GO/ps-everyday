package org.janggo.pseveryday.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tag_id")
    private Long id;

    String displayName;
    @Column(name = "tag_key")
    private String key;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemTag> problemTags = new ArrayList<>();

    public Tag(String displayName, String key) {
        this.displayName = displayName;
        this.key = key;
    }
}
