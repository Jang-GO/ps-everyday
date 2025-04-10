package org.janggo.pseveryday;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.janggo.pseveryday.problem.entity.Tag;
import org.janggo.pseveryday.problem.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@DataJpaTest
public class TagRepositoryQueryTest {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    EntityManager em;

    private List<Long> tagIds;

    @BeforeEach
    void setUp() {
        Tag tag1 = tagRepository.save(new Tag("test_tag1"));
        Tag tag2 = tagRepository.save(new Tag("test_tag2"));
        Tag tag3 = tagRepository.save(new Tag("test_tag3"));

        // 영속성 컨텍스트에서 가져와 select 쿼리르 비교하지 못하기 때문에 추가
        em.flush();
        em.clear();
        tagIds = Arrays.asList(tag1.getId(), tag2.getId(), tag3.getId());
    }

    @Test
    void findById_여러번_호출_쿼리비교() {
        System.out.println("=== findById 여러 번 호출 시작 ===");
        List<Tag> tags = tagIds.stream()
                .map(tagRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        tags.forEach(tag -> log.info("찾은 태그: {}", tag.getDisplayName()));
    }

    @Test
    void findAllById_한번에_호출_쿼리비교() {
        System.out.println("=== findAllById 한번 호출 시작 ===");
        List<Tag> tags = tagRepository.findAllById(tagIds);
        tags.forEach(tag -> log.info("찾은 태그: {}" , tag.getDisplayName()));
    }
}
