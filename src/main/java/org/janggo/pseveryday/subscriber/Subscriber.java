package org.janggo.pseveryday.subscriber;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "subscriber")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    public Subscriber(String email) {
        this.email = email;
    }
}
