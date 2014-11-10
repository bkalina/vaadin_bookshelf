package pl.javamylove.vaadin_bookshelf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pl.javamylove.vaadin_bookshelf.domain.Book;

public class BookManager {

	private List<Book> db = new ArrayList<Book>();

	public void addBook(Book book) {
		Book b = new Book(UUID.randomUUID().toString(), book.getTitle(), book
				.getSubTitle(), book.getDescription(), book
				.getAuthor(), book.getIsbn(), book.getYear(), book
				.getPage(), book.getPublisher(), book.getImage(),
				book.getDownload());
		db.add(b);
	}

	public void changeBook(Book oldBook, Book newBook) {
		Book toChange = null;
		for (Book b : db) {
			if (b.getTitle().equals(oldBook.getTitle())
					&& b.getAuthor().equals(oldBook.getAuthor())) {
				toChange = b;
			}
		}
		toChange.setTitle(newBook.getTitle());
		toChange.setSubTitle(newBook.getSubTitle());
		toChange.setDescription(newBook.getDescription());
		toChange.setAuthor(newBook.getAuthor());
		toChange.setIsbn(newBook.getIsbn());
		toChange.setYear(newBook.getYear());
		toChange.setPage(newBook.getPage());
		toChange.setPublisher(newBook.getPublisher());
	}

	public void deleteBook(Book bookToDelete) {
		Book toRemove = null;
		for (Book b : db) {
			if (b.getId().equals(bookToDelete.getId())) {
				toRemove = b;
				break;
			}
		}
		System.out.println("Delete: " + db.remove(toRemove));
	}

	public List<Book> findAll() {
		return db;
	}
}
