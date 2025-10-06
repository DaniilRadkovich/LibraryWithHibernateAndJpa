package ru.radkovich.springcourse.services;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.radkovich.springcourse.models.Book;
import ru.radkovich.springcourse.models.Person;
import ru.radkovich.springcourse.repositories.PeopleRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public List<Person> findAll(boolean sortByName) {
        if (sortByName)
            return peopleRepository.findAll(Sort.by("name"));
        else
            return peopleRepository.findAll();
    }

    public List<Person> findWithPagination(Integer page, Integer peoplePerPage, boolean sortByName) {
        if (sortByName)
            return peopleRepository.findAll(PageRequest.of(page, peoplePerPage, Sort.by("name"))).getContent();
        else
            return peopleRepository.findAll(PageRequest.of(page, peoplePerPage)).getContent();
    }

    public Person findOne(int id) {
        Optional<Person> foundPerson = peopleRepository.findById(id);
        return foundPerson.orElse(null);
    }

    @Transactional
    public void save(Person person) {
        peopleRepository.save(person);
    }

    @Transactional
    public void update(int id, Person updatedPerson) {
        updatedPerson.setId(id);
        peopleRepository.save(updatedPerson);
    }

    @Transactional
    public void delete(int id) {
        peopleRepository.deleteById(id);
    }

    public Optional<Person> getPersonByFullName(String fullName) {
        return peopleRepository.findByNameContaining(fullName);
    }

    public List<Book> getBooksByPersonId(int id) {
        Optional<Person> person = peopleRepository.findById(id);

        if (person.isPresent()) {
            Hibernate.initialize(person.get().getBooks());

            person.get().getBooks().forEach(book -> {
                if (book.getTakenAt() != null) {
                    long diffInMilsecs = Math.abs(book.getTakenAt().getTime() - new Date().getTime());
                    if (diffInMilsecs > 864000000)
                        book.setExpired(true);
                }
            });
            return person.get().getBooks();
        } else {
            return Collections.emptyList();
        }

    }
}
