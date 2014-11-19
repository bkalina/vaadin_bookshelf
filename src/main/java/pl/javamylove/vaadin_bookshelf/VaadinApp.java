package pl.javamylove.vaadin_bookshelf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import pl.javamylove.vaadin_bookshelf.domain.Book;
import pl.javamylove.vaadin_bookshelf.service.BookManager;

import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Title("My bookshelf")
public class VaadinApp extends UI {

	private static final long serialVersionUID = 1L;

	private BookManager bookManager = new BookManager();
	private Book book = new Book();
	private BeanItem<Book> bookItem = new BeanItem<Book>(book);
	private final String USER_AGENT = "Mozilla/5.0";

	private BeanItemContainer<Book> books = new BeanItemContainer<Book>(
			Book.class);
	private BeanItemContainer<Book> itBooks = new BeanItemContainer<Book>(
			Book.class);

	enum Action {
		ADD, EDIT
	}

	// Dodawanie i edycja
	private class AddBookForm extends Window {
		private static final long serialVersionUID = 1L;
		private Action action;

		public AddBookForm(final Action actionType) {
			this.action = actionType;
			setModal(true);
			center();
			final Button saveBtn;
			final Button cancelBtn = new Button(" Anuluj ");

			switch (actionType) {
			case ADD:
				setCaption("Dodaj książkę");
				saveBtn = new Button(" Dodaj książkę ");
				break;

			case EDIT:
				setCaption("Edytuj pozycję");
				saveBtn = new Button(" Zapisz ");
				break;

			default:
				saveBtn = new Button();
				break;
			}
			final FormLayout form = new FormLayout();
			final BeanFieldGroup<Book> binder = new BeanFieldGroup<Book>(
					Book.class);
			binder.setItemDataSource(bookItem);
			form.addComponent(binder.buildAndBind("Tytuł", "title"));
			form.addComponent(binder.buildAndBind("Podtytuł", "subTitle"));
			form.addComponent(binder.buildAndBind("Opis", "description"));
			form.addComponent(binder.buildAndBind("Autor", "author"));
			form.addComponent(binder.buildAndBind("ISBN", "isbn"));
			form.addComponent(binder.buildAndBind("Rok", "year"));
			form.addComponent(binder.buildAndBind("Liczba stron", "page"));
			form.addComponent(binder.buildAndBind("Wydawca", "publisher"));
			binder.setBuffered(true);

			VerticalLayout fvl = new VerticalLayout();
			fvl.setMargin(true);
			fvl.addComponent(form);

			HorizontalLayout hl = new HorizontalLayout();
			hl.addComponent(saveBtn);
			hl.addComponent(cancelBtn);
			fvl.addComponent(hl);

			setContent(fvl);

			saveBtn.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						Book oldBook = new Book(book.getId(), book.getTitle(),
								book.getSubTitle(), book.getDescription(), book
										.getAuthor(), book.getIsbn(), book
										.getYear(), book.getPage(), book
										.getPublisher(), book.getImage(), book
										.getDownload());
						binder.commit();
						switch (action) {
						case ADD:
							bookManager.addBook(book);
							break;
						case EDIT:
							bookManager.changeBook(oldBook, book);
							break;
						default:
							break;
						}
						books.removeAllItems();
						books.addAll(bookManager.findAll());
						close();
					} catch (Exception e) {
						@SuppressWarnings("deprecation")
						Notification notif = new Notification("Błąd",
								"Uzupełnij formularz!",
								Notification.TYPE_ERROR_MESSAGE);
						notif.setDelayMsec(5000);
						notif.setPosition(Position.BOTTOM_CENTER);
						notif.show(Page.getCurrent());
					}
				}
			});

			cancelBtn.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					binder.discard();
					close();
				}
			});
		}
	}

	// Dodawanie z IT-ebooks
	private class AddBookFormExt extends Window {
		private static final long serialVersionUID = 1L;

		public AddBookFormExt() {
			setModal(true);
			center();
			setCaption("Dodaj książkę z IT-ebooks");
			setWidth(80.0f, Unit.PERCENTAGE);
			final Button saveBtn = new Button(" Dodaj książkę ");
			final Button cancelBtn = new Button(" Anuluj ");
			final Button searchlBtn = new Button(" Szukaj ");
			final TextField searchQuery = new TextField();

			final FormLayout form = new FormLayout();
			final BeanFieldGroup<Book> binder = new BeanFieldGroup<Book>(
					Book.class);
			binder.setItemDataSource(bookItem);
			form.addComponent(binder.buildAndBind("Tytuł", "title"));
			form.addComponent(binder.buildAndBind("Podtytuł", "subTitle"));
			form.addComponent(binder.buildAndBind("Opis", "description"));
			form.addComponent(binder.buildAndBind("Autor", "author"));
			form.addComponent(binder.buildAndBind("ISBN", "isbn"));
			form.addComponent(binder.buildAndBind("Rok", "year"));
			form.addComponent(binder.buildAndBind("Liczba stron", "page"));
			form.addComponent(binder.buildAndBind("Wydawca", "publisher"));
			binder.setBuffered(true);

			final Table itBookTable = new Table("", itBooks);
			itBookTable.setColumnHeader("title", "Tytuł");
			itBookTable.setColumnHeader("subTitle", "Podtytuł");
			itBookTable.setColumnHeader("description", "Opis");
			itBookTable.setColumnHeader("author", "Autor");
			itBookTable.setColumnHeader("isbn", "ISBN");
			itBookTable.setColumnHeader("year", "Rok wydania");
			itBookTable.setColumnHeader("page", "Liczba stron");
			itBookTable.setColumnHeader("publisher", "Wydawca");
			itBookTable.setColumnHeader("download", "Link");
			itBookTable.setColumnHeader("image", "Okładka");
			itBookTable.setColumnHeader("id", "ID");
			itBookTable.setSelectable(true);
			itBookTable.setImmediate(true);
			itBookTable.setVisibleColumns(new Object[] { "title", "subTitle",
					"isbn" });

			itBookTable
					.addValueChangeListener(new Property.ValueChangeListener() {
						private static final long serialVersionUID = 1L;

						@Override
						public void valueChange(ValueChangeEvent event) {

							Book selectedBook = (Book) itBookTable.getValue();
							if (selectedBook == null) {
								book.setId("");
								book.setTitle("");
								book.setSubTitle("");
								book.setDescription("");
								book.setAuthor("");
							} else {
								try {
									Book temp = getBookById(selectedBook
											.getId());
									book.setTitle(temp.getTitle());
									book.setSubTitle(temp.getSubTitle());
									book.setDescription(temp.getDescription());
									book.setAuthor(temp.getAuthor());
									book.setIsbn(temp.getIsbn());
									book.setYear(temp.getYear());
									book.setPage(temp.getPage());
									book.setPublisher(temp.getPublisher());
									close();
									addWindow(new AddBookFormExt());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});

			VerticalLayout fvl = new VerticalLayout();
			fvl.setMargin(true);
			fvl.addComponent(form);

			HorizontalLayout hl = new HorizontalLayout();
			hl.addComponent(saveBtn);
			hl.addComponent(cancelBtn);
			fvl.addComponent(hl);

			HorizontalLayout hlMain = new HorizontalLayout();
			hlMain.addComponent(fvl);
			VerticalLayout fvl2 = new VerticalLayout();
			hlMain.addComponent(fvl2);
			HorizontalLayout hl2 = new HorizontalLayout();
			fvl2.addComponent(hl2);
			hl2.addComponent(searchQuery);
			hl2.addComponent(searchlBtn);
			fvl2.addComponent(itBookTable);
			setContent(hlMain);

			saveBtn.addClickListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						binder.commit();
						bookManager.addBook(book);
						books.removeAllItems();
						books.addAll(bookManager.findAll());
						close();
					} catch (Exception e) {
						@SuppressWarnings("deprecation")
						Notification notif = new Notification("Błąd",
								"Uzupełnij formularz!",
								Notification.TYPE_ERROR_MESSAGE);
						notif.setDelayMsec(5000);
						notif.setPosition(Position.BOTTOM_CENTER);
						notif.show(Page.getCurrent());
					}
				}
			});

			cancelBtn.addClickListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					binder.discard();
					close();
				}
			});

			searchlBtn.addClickListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						itBooks.addAll(searchBook(searchQuery.getValue()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private Book getBookById(String id) throws Exception {

		String url = "http://it-ebooks-api.info/v1/book/" + id;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return jsonToBook(response.toString());
	}

	private Book jsonToBook(String response) {
		Book jsonBook = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, true);
		try {
			jsonBook = mapper.readValue(response.toString(), Book.class);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonBook;
	}

	private List<Book> searchBook(String query) throws Exception {

		String url = "http://it-ebooks-api.info/v1/search/" + query;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		if (response.toString().length() < 26) {
			@SuppressWarnings("deprecation")
			Notification notif = new Notification("Informacja",
					"Brak wyników!", Notification.TYPE_HUMANIZED_MESSAGE);
			notif.setDelayMsec(5000);
			notif.setPosition(Position.BOTTOM_CENTER);
			notif.show(Page.getCurrent());
			return null;
		} else {
			return jsonToBookList(response.toString());
		}
	}

	private List<Book> jsonToBookList(String response) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, true);
		List<Book> jsonBooks = null;
		try {
			JsonNode root = mapper.readTree(response.toString());
			JsonNode books = root.get("Books");
			jsonBooks = mapper.readValue(books,
					new TypeReference<List<Book>>() {
					});
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonBooks;
	}

	@Override
	protected void init(VaadinRequest request) {

		Button addBookFormBtn = new Button("Dodaj");
		Button addBookFormExtBtn = new Button("Dodaj z IT-ebooks");
		Button editBookFormBtn = new Button("Edytuj");
		Button deleteBookFormBtn = new Button("Usuń");

		VerticalLayout vl = new VerticalLayout();
		setContent(vl);

		addBookFormBtn.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				book.setTitle("");
				book.setSubTitle("");
				book.setDescription("");
				book.setAuthor("");
				book.setIsbn("");
				book.setYear("");
				book.setPage("");
				book.setPublisher("");
				addWindow(new AddBookForm(Action.ADD));
			}
		});

		addBookFormExtBtn.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				book.setTitle("");
				book.setSubTitle("");
				book.setDescription("");
				book.setAuthor("");
				book.setIsbn("");
				book.setYear("");
				book.setPage("");
				book.setPublisher("");
				addWindow(new AddBookFormExt());
			}
		});

		editBookFormBtn.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				addWindow(new AddBookForm(Action.EDIT));
			}
		});

		deleteBookFormBtn.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (book != null) {
					bookManager.deleteBook(book);
					books.removeAllItems();
					books.addAll(bookManager.findAll());
				}
			}
		});

		HorizontalLayout hl = new HorizontalLayout();
		hl.addComponent(addBookFormBtn);
		hl.addComponent(addBookFormExtBtn);
		hl.addComponent(editBookFormBtn);
		hl.addComponent(deleteBookFormBtn);

		final Table bookTable = new Table("", books);
		bookTable.setWidth(80.0f, Unit.PERCENTAGE);
		bookTable.setColumnHeader("title", "Tytuł");
		bookTable.setColumnHeader("subTitle", "Podtytuł");
		bookTable.setColumnHeader("description", "Opis");
		bookTable.setColumnHeader("author", "Autor");
		bookTable.setColumnHeader("isbn", "ISBN");
		bookTable.setColumnHeader("year", "Rok wydania");
		bookTable.setColumnHeader("page", "Liczba stron");
		bookTable.setColumnHeader("publisher", "Wydawca");
		bookTable.setColumnHeader("download", "Link");
		bookTable.setColumnHeader("image", "Okładka");
		bookTable.setColumnHeader("id", "ID");
		bookTable.setSelectable(true);
		bookTable.setImmediate(true);
		bookTable.setVisibleColumns(new Object[] { "title", "subTitle",
				"author", "description", "isbn", "publisher", "year", "page" });
		bookTable.addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				Book selectedBook = (Book) bookTable.getValue();
				if (selectedBook == null) {
					book.setId("");
					book.setTitle("");
					book.setSubTitle("");
					book.setDescription("");
					book.setAuthor("");
					book.setIsbn("");
					book.setYear("");
					book.setPage("");
					book.setPublisher("");
				} else {
					book.setId(selectedBook.getId());
					book.setTitle(selectedBook.getTitle());
					book.setSubTitle(selectedBook.getSubTitle());
					book.setDescription(selectedBook.getDescription());
					book.setAuthor(selectedBook.getAuthor());
					book.setIsbn(selectedBook.getIsbn());
					book.setYear(selectedBook.getYear());
					book.setPage(selectedBook.getPage());
					book.setPublisher(selectedBook.getPublisher());
				}
			}
		});

		vl.addComponent(hl);
		vl.addComponent(bookTable);
	}

}
