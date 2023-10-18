package com.example.reactive.controller;

import com.example.reactive.dao.PersonRepository;
import com.example.reactive.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/people")
public class PersonController {

    @Autowired
    PersonRepository personRepository;

    @GetMapping("/{id}")
    private Mono<Person> getPersonById(@PathVariable Long id) {
        return personRepository.findById(id);
    }

    @GetMapping
    private Flux<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @PostMapping
    public Mono<Person> save(@RequestBody Person person) {
        return personRepository.save(person);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> findById(@PathVariable Long id) {
        return personRepository.deleteById(id);
    }
}