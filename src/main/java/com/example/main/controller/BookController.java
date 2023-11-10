package com.example.main.controller;

import com.example.main.model.Book;
import com.example.main.repository.BookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {
    BookRepository bookRepository;
    KafkaTemplate<String, String> kafkaTemplate;

    public BookController(BookRepository bookRepository,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.bookRepository = bookRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<Book> save(@RequestBody Book book) {
        System.out.println("Adding Book:: " + book);
        Book savedBook = bookRepository.save(book);
        System.out.println("Saved book " + savedBook);

        this.kafkaTemplate.send("bookeventhub", "Book created="+book.getTitle())
                .whenComplete((sendResult, throwable) -> {
            if (throwable == null)
                System.out.println("Sent successfully -> " + sendResult);
            else
                throwable.printStackTrace();
        });

        return ResponseEntity.ok(savedBook);
    }

    @GetMapping
    public Iterable<Book> findAll() {
        return bookRepository.findAll();
    }
}
