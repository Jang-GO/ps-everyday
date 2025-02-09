package org.janggo.pseveryday.subscriber;

import jakarta.persistence.*;

@Entity
@Table(name = "subscriber")
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;
}
