package org.center.service;

import org.center.model.Book;
import org.center.repository.BookRepository;

import java.util.List;
import java.util.Optional;

public class BookService {

    private final BookRepository bookRepository;

    public BookService() {
        this(new BookRepository());
    }

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> findBelowSafetyStock() {
        return bookRepository.findBelowSafetyStock();
    }

    public Book save(Book book) {
        if (book.getBookId() == null) {
            return bookRepository.save(book);
        }
        bookRepository.update(book);
        return book;
    }
}
