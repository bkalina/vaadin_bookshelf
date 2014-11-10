package pl.javamylove.vaadin_bookshelf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import pl.javamylove.vaadin_bookshelf.domain.Book;
import pl.javamylove.vaadin_bookshelf.service.BookManager;

import com.vaadin.annotations.Title;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
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

	private static Validator validator;
	private BookManager bookManager = new BookManager();

	private Book book = new Book();
	private BeanItem<Book> bookItem = new BeanItem<Book>(book);
	private final String USER_AGENT = "Mozilla/5.0";

	private BeanItemContainer<Book> books = new BeanItemContainer<Book>(
			Book.class);

	enum Action {
		ADD, EDIT
	}

	// Dodawanie i edycja
	private class MyFormWindow extends Window {
		private static final long serialVersionUID = 1L;
		private Action action;

		public MyFormWindow(final Action actionType) {
			this.action = actionType;
			setModal(true);
			center();
			final Button saveBtn;
			final Button cancelBtn = new Button(" Anuluj ");

			switch (actionType) {
			case ADD:
				setCaption("Add book");
				saveBtn = new Button(" Add book ");
				break;

			case EDIT:
				setCaption("Edit book");
				saveBtn = new Button(" Edit book ");
				break;

			default:
				saveBtn = new Button();
				break;
			}
			ValidatorFactory factory = Validation
					.buildDefaultValidatorFactory();
			validator = factory.getValidator();
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
					Book oldBook = new Book(book.getId(), book.getTitle(), book
							.getSubTitle(), book.getDescription(), book
							.getAuthor(), book.getIsbn(), book.getYear(), book
							.getPage(), book.getPublisher(), book.getImage(),
							book.getDownload());
					Set<ConstraintViolation<Book>> constraintViolations = validator
							.validate(book);
					System.out.println("###" + constraintViolations.size());
					if (constraintViolations.size() == 0) {
						try {
							binder.commit();
						} catch (CommitException e) {
							e.printStackTrace();
						}
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
					} else {
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

	private String sendGet(String id) throws Exception {

		String url = "http://it-ebooks-api.info/v1/book/" + id;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());
		// jsonToJava(response.toString());
		return response.toString();

	}

	public void jsonToJava(String input) {
		ObjectMapper mapper = new ObjectMapper();

		try {

			// read from file, convert it to user class
			Book book = mapper.readValue(input, Book.class);

			// display to console
			System.out.println(book);

		} catch (JsonGenerationException e) {

			e.printStackTrace();

		} catch (JsonMappingException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

	@Override
	protected void init(VaadinRequest request) {

		Button addBookFormBtn = new Button("Add ");
		Button deleteBookFormBtn = new Button("Delete");
		Button editBookFormBtn = new Button("Edit");
		TextField tf = new TextField();

		VerticalLayout vl = new VerticalLayout();
		tf.setWidth("300px");
		try {
			tf.setValue(sendGet("2279690981"));
		} catch (ReadOnlyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		vl.addComponent(tf);
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
				addWindow(new MyFormWindow(Action.ADD));
			}
		});

		editBookFormBtn.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				addWindow(new MyFormWindow(Action.EDIT));
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
		hl.addComponent(editBookFormBtn);
		hl.addComponent(deleteBookFormBtn);

		final Table bookTable = new Table("Books", books);
		bookTable.setColumnHeader("title", "Tytuł");
		bookTable.setColumnHeader("subtitle", "Podtytuł");
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
				} else {
					book.setId(selectedBook.getId());
					book.setTitle(selectedBook.getTitle());
					book.setSubTitle(selectedBook.getSubTitle());
					book.setDescription(selectedBook.getDescription());
					book.setAuthor(selectedBook.getAuthor());
				}
			}
		});

		vl.addComponent(hl);
		vl.addComponent(bookTable);
	}

}
