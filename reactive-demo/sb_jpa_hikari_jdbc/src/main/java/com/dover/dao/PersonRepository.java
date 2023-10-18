package com.example.dao;

import com.example.domain.Person;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {
}
