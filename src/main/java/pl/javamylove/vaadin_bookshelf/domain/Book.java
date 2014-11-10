package pl.javamylove.vaadin_bookshelf.domain;

import javax.validation.constraints.Size;

public class Book {
	private String id;
	@Size(min = 5, max = 200, message = "Minimum 5 znaków")
	private String title;
	private String subTitle;
	private String description;
	@Size(min = 5, max = 100, message = "Minimum 5 znaków")
	private String author;
	private String isbn;
	private String year;
	private String page;
	private String publisher;
	private String image;
	private String download;

	public Book() {
	}

	public Book(String id, String title, String subTitle, String description,
			String author, String isbn, String year, String page,
			String publisher, String image, String download) {
		super();
		this.id = id;
		this.title = title;
		this.subTitle = subTitle;
		this.description = description;
		this.author = author;
		this.isbn = isbn;
		this.year = year;
		this.page = page;
		this.publisher = publisher;
		this.image = image;
		this.download = download;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	@Override
	public String toString() {
		return "Book [id=" + id + ", title=" + title + ", subTitle=" + subTitle
				+ ", description=" + description + ", author=" + author
				+ ", isbn=" + isbn + ", year=" + year + ", page=" + page
				+ ", publisher=" + publisher + ", image=" + image
				+ ", download=" + download + "]";
	}

}
