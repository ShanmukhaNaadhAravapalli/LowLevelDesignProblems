package bookMyShow;

import java.util.*;

enum SeatType {
    SILVER(150.0),
    GOLD(200.0),
    PLATINUM(295.0);
    private final double basePrice;
    SeatType(double basePrice){
        this.basePrice = basePrice;
    }
    public double getBasePrice(){
        return this.basePrice;
    }
}

enum ShowSeatStatus {
    AVAILABLE,
    BOOKED,
    BLOCKED
}

enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
class User {
    private int userID;
    private String username;
    private String useremail;
    private String number;

    public User(int userID, String useremail, String username, String number) {
        this.userID = userID;
        this.useremail = useremail;
        this.username = username;
        this.number = number;
    }

    public int getUserID() {
        return userID;
    }

    public String getUseremail() {
        return useremail;
    }

    public String getUsername() {
        return username;
    }

    public String getNumber() {
        return number;
    }
}

class City {
    public int id;
    public String name;

    public City(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class Seat {
    private final int seatId;
    private final  int row;
    private final SeatType seatType;
    public Seat(int seatId, int row, SeatType seatType) {
        this.seatId = seatId;
        this.row = row;
        this.seatType = seatType;
    }

    public int getSeatId() {
        return seatId;
    }

    public int getRow() {
        return row;
    }

    public SeatType getSeatType() {
        return seatType;
    }
}

class ShowSeat {
    private Seat seat;
    private double price;
    private Show show;
    private ShowSeatStatus seatStatus;
    private Long blockedUntil;

    public ShowSeat(Seat seat, double price, Show show, ShowSeatStatus seatStatus, long blockedUntil) {
        this.seat = seat;
        this.price = price;
        this.show = show;
        this.seatStatus = ShowSeatStatus.AVAILABLE;
        this.blockedUntil = null;
    }

    public Seat getSeat() {
        return seat;
    }

    public double getPrice() {
        return price;
    }

    public Show getShow() {
        return show;
    }

    public ShowSeatStatus getSeatStatus() {
        return seatStatus;
    }

    public long getBlockedUntil() {
        return blockedUntil;
    }

    public boolean bookSeat(){
        if(this.seatStatus == ShowSeatStatus.BLOCKED ){
            this.seatStatus = ShowSeatStatus.BOOKED;
            this.blockedUntil = null;
            return true;
        }
        return false;
    }

    public boolean blockSeat(){
        if(this.seatStatus == ShowSeatStatus.AVAILABLE){
            this.seatStatus = ShowSeatStatus.BLOCKED;
            this.blockedUntil = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean releaseSeat() {
        if (this.seatStatus == ShowSeatStatus.BLOCKED) {
            this.seatStatus = ShowSeatStatus.AVAILABLE;
            this.blockedUntil = null;
            return true;
        }
        return false;
    }
}

class Movie {
    private final int movieId;
    private final String movieName;
    private final int duration;
    private final String language;
    private final String genre;

    public Movie(int movieId, String movieName, int duration, String language, String genre) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.duration = duration;
        this.language = language;
        this.genre = genre;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public int getDuration() {
        return duration;
    }

    public String getLanguage() {
        return language;
    }

    public String getGenre() {
        return genre;
    }
}

class Theatre {
    private int threatreId;
    private City city;
    private List<Show> shows;
    private List<Screen> screens;

    public Theatre(int threatreId, City city, List<Show> shows, List<Screen> screens) {
        this.threatreId = threatreId;
        this.city = city;
        this.shows = shows;
        this.screens = screens;
    }

    public int getThreatreId() {
        return threatreId;
    }

    public City getCity() {
        return city;
    }

    public List<Show> getShows() {
        return shows;
    }

    public List<Screen> getScreens() {
        return screens;
    }
}

class Screen{
    private int screenId;
    private Theatre theatre;
    private List<Seat> Seats;

    public Screen(int screenId, Theatre theatre, List<Seat> seats) {
        this.screenId = screenId;
        this.theatre = theatre;
        Seats = seats;
    }

    public int getScreenId() {
        return screenId;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public List<Seat> getSeats() {
        return List.copyOf(Seats);
    }
}

class Show {
    private int showId;
    private Movie movie;
    private int startTime;
    private Screen screen;
    private List<ShowSeat> showseats;

    public Show(int showId, Movie movie, int startTime, List<ShowSeat> showseats) {
        this.showId = showId;
        this.movie = movie;
        this.startTime = startTime;
        this.showseats = showseats;
    }

    public void addShowSeat( ShowSeat showSeat){
        showseats.add(showSeat);
    }

    public int getShowId() {
        return showId;
    }

    public Movie getMovie() {
        return movie;
    }

    public int getStartTime() {
        return startTime;
    }

    public List<ShowSeat> getShowseats() {
        return List.copyOf(showseats);
    }
}

class Booking {
    private int bookingId;
    private Show show;
    private Theatre theatre;
    private Movie movie;
    private List<ShowSeat> bookedShowSeats;
    private User user;
    private Date bookingTime;
    private BookingStatus bookingStatus;

    public double getTotalAmount() {
        return totalAmount;
    }

    private double totalAmount;


    public Booking(int bookingId, Show show, Theatre theatre, Movie movie, List<ShowSeat> showSeats, User user, Date bookingTime) {
        this.bookingId = bookingId;
        this.show = show;
        this.theatre = theatre;
        this.movie = movie;
        this.bookedShowSeats = showSeats;
        this.user = user;
        this.bookingTime = bookingTime;
        this.bookingStatus = BookingStatus.PENDING;
    }

    public int getBookingId() {
        return bookingId;
    }

    public Show getShow() {
        return show;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public Movie getMovie() {
        return movie;
    }

    public List<ShowSeat> getShowSeats() {
        return List.copyOf(bookedShowSeats);
    }

    public User getUser() {
        return user;
    }

    public Date getBookingTime() {
        return bookingTime;
    }

    public void confirmBooking() {
        this.bookingStatus = BookingStatus.CONFIRMED;
        for (ShowSeat seat : bookedShowSeats) {
            seat.bookSeat();
        }
    }

    public void cancelBooking() {
        this.bookingStatus = BookingStatus.CANCELLED;
        for (ShowSeat seat : bookedShowSeats) {
            seat.releaseSeat();
        }
    }
}

class Payment {
    private String paymentId;
    private Booking booking;
    private PaymentStatus paymentStatus;
    private Date paymentTime;
    private String paymentMethod;
    private double amount;

    public Payment(String paymentId, Booking booking, PaymentStatus paymentStatus, String paymentMethod, double amount) {
        this.paymentId = paymentId;
        this.booking = booking;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public Booking getBooking() {
        return booking;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Date getPaymenttDate() {
        return paymentTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void completePayment() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paymentTime = new Date();
    }

    public void failPayment() {
        this.paymentStatus = PaymentStatus.FAILED;
    }
}
class PaymentService {

    public Payment processPayment(Booking booking, String paymentMethod){
        String paymentId = "PAY" + System.currentTimeMillis();
        Payment payment = new Payment(paymentId , booking, PaymentStatus.PENDING,  paymentMethod, booking.getTotalAmount());
        try{
            Thread.sleep(1000);
            if(Math.random() < 0.9)
                payment.completePayment();
            else
                payment.failPayment();
        }
        catch(InterruptedException e){
            payment.failPayment();
        }
        return payment;
    }
}
class MovieController {
    Map<City, List<Movie>> cityvsMovies;
    List<Movie> allMovies;

    public MovieController(Map<City, List<Movie>> cityvsMovies, List<Movie> allMovies) {
        this.cityvsMovies = new HashMap<>();
        this.allMovies = new ArrayList<>();
    }

    public void addMovie(Movie movie){
        allMovies.add(movie);
    }

    public void addCity(City city){
        cityvsMovies.put(city, new ArrayList<>());
    }

    public void addCityForMovie(City city, Movie movie){
        cityvsMovies.computeIfAbsent(city, k -> new ArrayList<>()).add(movie);
    }

    public List<Movie> getMovieByCity(City city){
        return cityvsMovies.get(city)!= null ? cityvsMovies.get(city) : new ArrayList<>();
    }

    public List<City> getAllCities(){
        return List.copyOf(cityvsMovies.keySet());
    }

    public Movie getMovieByName(String movieName) {

        for(Movie movie : allMovies) {
            if((movie.getMovieName()).equals(movieName)) {
                return movie;
            }
        }
        return null;
    }

    // ALL CRUD Operations removing and updating movie from a particular city

}



class TheatreController {
    Map<City, List<Theatre>> cityvsTheatre;
    List<Theatre> allTheatre;

    public Map<City, List<Theatre>> getCityvsTheatre() {
        return cityvsTheatre;
    }

    public List<Theatre> gettTheatres() {
        return List.copyOf(allTheatre);
    }

    public List<Theatre> getTheatresByCity(City city){
        return cityvsTheatre.get(city);
    }

    public void addTheatreToCity(Theatre theatre, City city){
        allTheatre.add(theatre);
        cityvsTheatre.computeIfAbsent(city, k-> new ArrayList<>()).add(theatre);
    }

    public Map<Theatre, List<Show>> getAllShows(Movie movie, City city){ // Important
        Map<Theatre, List<Show>> theatrevsShows = new HashMap<>();
        for(Theatre theatre: cityvsTheatre.get(city)){
            List<Show> shows = theatre.getShows();
            for(Show show: shows){
                if(show.getMovie().getMovieId() == movie.getMovieId()){
                    theatrevsShows.computeIfAbsent(theatre, k -> new ArrayList<>()).add(show);
                }
            }
        }
        return theatrevsShows;
    }
}

class BookingService{
    private MovieController movieController;
    private TheatreController theatreController;
    public BookingService( MovieController movieController, TheatreController theatreController) {
        this.movieController = movieController;
        this.theatreController = theatreController;
    }
}



public class BookMyShow {
}
