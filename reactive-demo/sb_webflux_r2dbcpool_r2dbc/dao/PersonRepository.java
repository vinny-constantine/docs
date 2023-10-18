package com.example.reactive.dao;

import com.example.reactive.domain.Person;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PersonRepository extends ReactiveCrudRepository<Person, Long> {
}
