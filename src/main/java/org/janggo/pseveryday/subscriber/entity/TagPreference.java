package org.janggo.pseveryday.subscriber.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    private String tagName;
}
