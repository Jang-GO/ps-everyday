package org.janggo.pseveryday.subscriber.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.janggo.pseveryday.problem.entity.Tag;

// 사용자 태그 선호도 테이블
@Entity
@Table(name = "subscriber_tag_preference")
@Getter
@Setter
public class TagPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;  // Tag 엔티티와 연관관계
}
