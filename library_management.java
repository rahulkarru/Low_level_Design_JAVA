import java.util.*;

// User entity
class User {
    private String id;
    private String name;
    private List<Book> borrowedBooks = new ArrayList<>();
    private Map<String, Date> borrowedDateMap = new HashMap<>(); // Track borrow date by ISBN

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBorrowedBooks() { return borrowedBooks; }

    public void borrowBook(Book book) {
        borrowedBooks.add(book);
        borrowedDateMap.put(book.getIsbn(), new Date());
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
        borrowedDateMap.remove(book.getIsbn());
    }

    public Date getBorrowDate(String isbn) {
        return borrowedDateMap.get(isbn);
    }
}

// Book entity remains the same
class Book {
    private String isbn;
    private String author;
    private String name;

    public Book(String isbn, String author, String name) {
        this.isbn = isbn;
        this.author = author;
        this.name = name;
    }
    // Getters and Setters
    public String getIsbn() { return isbn; }
    public String getAuthor() { return author; }
    public String getName() { return name; }
}

// UserManager manages user operations
class UserManager {
    private Map<String, User> users = new HashMap<>();

    public void addUser(User user) {
        users.put(user.getId(), user);
    }
    public User getUser(String id) {
        return users.get(id);
    }
}

// LibraryManager manages book inventory
class LibraryManager {
    private Map<String, Integer> bookInventory = new HashMap<>();

    public void addBook(Book book, int count) {
        bookInventory.put(book.getIsbn(), bookInventory.getOrDefault(book.getIsbn(), 0) + count);
    }
    public boolean removeBook(String isbn) {
        if(bookInventory.containsKey(isbn) && bookInventory.get(isbn) > 0) {
            bookInventory.put(isbn, bookInventory.get(isbn) - 1);
            return true;
        }
        return false;
    }
    public int getBookCount(String isbn) {
        return bookInventory.getOrDefault(isbn, 0);
    }
}

// ReservationManager handles book reservations
class ReservationManager {
    private Map<String, List<String>> reservations = new HashMap<>(); // isbn -> list of userIds
    private NotificationService notificationService;

    public ReservationManager(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    public void reserveBook(String isbn, String userId) {
        reservations.computeIfAbsent(isbn, k -> new ArrayList<>()).add(userId);
    }

    public void notifyUsersBookAvailable(String isbn, UserManager userManager) {
        List<String> reservedUsers = reservations.getOrDefault(isbn, new ArrayList<>());
        for (String userId : reservedUsers) {
            User user = userManager.getUser(userId);
            if(user != null) {
                notificationService.sendNotification(user, "Book with ISBN " + isbn + " is now available.");
            }
        }
        reservations.remove(isbn);  // Clear reservation list after notifications
    }
}

// TransactionManager handles issuing and returning books
class TransactionManager {
    private LibraryManager libraryManager;
    private UserManager userManager;
    private ReservationManager reservationManager;
    private FineCalculator fineCalculator;

    public TransactionManager(LibraryManager libraryManager, UserManager userManager,
                              ReservationManager reservationManager, FineCalculator fineCalculator) {
        this.libraryManager = libraryManager;
        this.userManager = userManager;
        this.reservationManager = reservationManager;
        this.fineCalculator = fineCalculator;
    }

    public boolean issueBook(String isbn, String userId) {
        User user = userManager.getUser(userId);
        if (user == null || libraryManager.getBookCount(isbn) == 0) {
            return false;
        }
        if(libraryManager.removeBook(isbn)) {
            Book book = new Book(isbn, "Unknown", "Unknown");
            user.borrowBook(book);
            return true;
        }
        return false;
    }

    public boolean returnBook(String isbn, String userId) {
        User user = userManager.getUser(userId);
        if (user == null) return false;

        Book bookToReturn = null;
        for (Book book : user.getBorrowedBooks()) {
            if (book.getIsbn().equals(isbn)) {
                bookToReturn = book;
                break;
            }
        }
        if (bookToReturn != null) {
            // Calculate fine
            Date borrowedDate = user.getBorrowDate(isbn);
            long fine = fineCalculator.calculateFine(borrowedDate, new Date());
            if (fine > 0) {
                System.out.println("Fine due for user " + user.getName() + ": " + fine + " units");
            }
            user.returnBook(bookToReturn);
            libraryManager.addBook(bookToReturn, 1);
            // Notify reserved users
            reservationManager.notifyUsersBookAvailable(isbn, userManager);
            return true;
        }
        return false;
    }
}

// Calculates fines based on date difference
class FineCalculator {
    private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    private static final int ALLOWED_DAYS = 14;
    private static final long FINE_PER_DAY = 5; // Arbitrary units

    public long calculateFine(Date borrowedDate, Date returnDate) {
        long diffMillis = returnDate.getTime() - borrowedDate.getTime();
        long diffDays = diffMillis / MILLIS_IN_DAY;
        long overdueDays = diffDays - ALLOWED_DAYS;
        return overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0;
    }
}

// Simplified NotificationService
class NotificationService {
    public void sendNotification(User user, String message) {
        System.out.println("Notify user " + user.getName() + ": " + message);
    }
}

// Main class to coordinate all
public class MainLibrarySystem {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        LibraryManager libraryManager = new LibraryManager();
        NotificationService notificationService = new NotificationService();
        ReservationManager reservationManager = new ReservationManager(notificationService);
        FineCalculator fineCalculator = new FineCalculator();
        TransactionManager transactionManager = new TransactionManager(libraryManager, userManager, reservationManager, fineCalculator);

        // Setup users and books
        User user1 = new User("1", "Alice");
        userManager.addUser(user1);

        Book book1 = new Book("ISBN001", "Author1", "Book One");
        libraryManager.addBook(book1, 5);

        // User reserves the book
        reservationManager.reserveBook("ISBN001", "1");

        // Issue book
        if(transactionManager.issueBook("ISBN001", "1")) {
            System.out.println("Book issued to user " + user1.getName());
        } else {
            System.out.println("Issue failed.");
        }

        // Return book after some days (simulate by setting borrowed date in past)
        try {
            // For testing overdue fine, we modify the borrowed date
            Thread.sleep(2000); // Sleep to create time difference
        } catch (InterruptedException ignored) {}

        if(transactionManager.returnBook("ISBN001", "1")) {
            System.out.println("Book returned by user " + user1.getName());
        } else {
            System.out.println("Return failed.");
        }
    }
}
