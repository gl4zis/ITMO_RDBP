package ru.itmo.is.util;

import org.springframework.stereotype.Component;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.bid.*;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.PasswordManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestDataBuilder {

    // ========== User Builders ==========

    public UserBuilder user() {
        return new UserBuilder();
    }

    public ResidentBuilder resident() {
        return new ResidentBuilder();
    }

    // ========== Dormitory Builders ==========

    public UniversityBuilder university() {
        return new UniversityBuilder();
    }

    public DormitoryBuilder dormitory() {
        return new DormitoryBuilder();
    }

    public RoomBuilder room() {
        return new RoomBuilder();
    }

    // ========== Bid Builders ==========

    public OccupationBidBuilder occupationBid() {
        return new OccupationBidBuilder();
    }

    public EvictionBidBuilder evictionBid() {
        return new EvictionBidBuilder();
    }

    public DepartureBidBuilder departureBid() {
        return new DepartureBidBuilder();
    }

    public RoomChangeBidBuilder roomChangeBid() {
        return new RoomChangeBidBuilder();
    }

    // ========== Event Builder ==========

    public EventBuilder event() {
        return new EventBuilder();
    }

    // ========== User Builder ==========

    public static class UserBuilder {
        private String login = "testuser";
        private String password = "password123";
        private String name = "Test";
        private String surname = "User";
        private User.Role role = User.Role.NON_RESIDENT;

        public UserBuilder withLogin(String login) {
            this.login = login;
            return this;
        }

        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public UserBuilder withRole(User.Role role) {
            this.role = role;
            return this;
        }

        public User build() {
            User user = new User();
            user.setLogin(login);
            user.setPassword(PasswordManager.hash(password));
            user.setName(name);
            user.setSurname(surname);
            user.setRole(role);
            return user;
        }
    }

    // ========== Resident Builder ==========

    public static class ResidentBuilder {
        private String login = "resident1";
        private String password = "password123";
        private String name = "Resident";
        private String surname = "Test";
        private University university;
        private Room room;

        public ResidentBuilder withLogin(String login) {
            this.login = login;
            return this;
        }

        public ResidentBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public ResidentBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ResidentBuilder withSurname(String surname) {
            this.surname = surname;
            return this;
        }

        public ResidentBuilder withUniversity(University university) {
            this.university = university;
            return this;
        }

        public ResidentBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public Resident build() {
            User user = new User();
            user.setLogin(login);
            user.setPassword(PasswordManager.hash(password));
            user.setName(name);
            user.setSurname(surname);
            user.setRole(User.Role.RESIDENT);

            Resident resident = Resident.of(user);
            resident.setUniversity(university);
            resident.setRoom(room);
            return resident;
        }
    }

    // ========== University Builder ==========

    public static class UniversityBuilder {
        private String name = "Test University";
        private String address = "Test Address";
        private List<Dormitory> dormitories = new ArrayList<>();

        public UniversityBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UniversityBuilder withAddress(String address) {
            this.address = address;
            return this;
        }

        public UniversityBuilder withDormitories(List<Dormitory> dormitories) {
            this.dormitories = dormitories != null ? new ArrayList<>(dormitories) : new ArrayList<>();
            return this;
        }

        public UniversityBuilder addDormitory(Dormitory dormitory) {
            this.dormitories.add(dormitory);
            return this;
        }

        public University build() {
            University university = new University();
            university.setName(name);
            university.setAddress(address);
            university.setDormitories(dormitories);
            return university;
        }
    }

    // ========== Dormitory Builder ==========

    public static class DormitoryBuilder {
        private String address = "Test Dormitory Address";
        private List<University> universities = new ArrayList<>();
        private List<Room> rooms = new ArrayList<>();

        public DormitoryBuilder withAddress(String address) {
            this.address = address;
            return this;
        }

        public DormitoryBuilder withUniversities(List<University> universities) {
            this.universities = universities != null ? new ArrayList<>(universities) : new ArrayList<>();
            return this;
        }

        public DormitoryBuilder addUniversity(University university) {
            this.universities.add(university);
            return this;
        }

        public DormitoryBuilder withRooms(List<Room> rooms) {
            this.rooms = rooms != null ? new ArrayList<>(rooms) : new ArrayList<>();
            return this;
        }

        public DormitoryBuilder addRoom(Room room) {
            this.rooms.add(room);
            return this;
        }

        public Dormitory build() {
            Dormitory dormitory = new Dormitory();
            dormitory.setAddress(address);
            dormitory.setUniversities(universities);
            dormitory.setRooms(rooms);
            return dormitory;
        }
    }

    // ========== Room Builder ==========

    public static class RoomBuilder {
        private Dormitory dormitory;
        private int number = 101;
        private Room.Type type = Room.Type.BLOCK;
        private int capacity = 2;
        private int floor = 1;
        private int cost = 5000;
        private List<Resident> residents = new ArrayList<>();

        public RoomBuilder withDormitory(Dormitory dormitory) {
            this.dormitory = dormitory;
            return this;
        }

        public RoomBuilder withNumber(int number) {
            this.number = number;
            return this;
        }

        public RoomBuilder withType(Room.Type type) {
            this.type = type;
            return this;
        }

        public RoomBuilder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public RoomBuilder withFloor(int floor) {
            this.floor = floor;
            return this;
        }

        public RoomBuilder withCost(int cost) {
            this.cost = cost;
            return this;
        }

        public RoomBuilder withResidents(List<Resident> residents) {
            this.residents = residents != null ? new ArrayList<>(residents) : new ArrayList<>();
            return this;
        }

        public RoomBuilder addResident(Resident resident) {
            this.residents.add(resident);
            return this;
        }

        public Room build() {
            Room room = new Room();
            room.setDormitory(dormitory);
            room.setNumber(number);
            room.setType(type);
            room.setCapacity(capacity);
            room.setFloor(floor);
            room.setCost(cost);
            room.setResidents(residents);
            return room;
        }
    }

    // ========== Bid Builders ==========

    public static class OccupationBidBuilder {
        private User sender;
        private University university;
        private Dormitory dormitory;
        private String text = "Test occupation bid";
        private Bid.Status status = Bid.Status.IN_PROCESS;
        private final List<BidFile> files = new ArrayList<>();

        public OccupationBidBuilder withSender(User sender) {
            this.sender = sender;
            return this;
        }

        public OccupationBidBuilder withUniversity(University university) {
            this.university = university;
            return this;
        }

        public OccupationBidBuilder withDormitory(Dormitory dormitory) {
            this.dormitory = dormitory;
            return this;
        }

        public OccupationBidBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public OccupationBidBuilder withStatus(Bid.Status status) {
            this.status = status;
            return this;
        }

        public OccupationBid build() {
            OccupationBid bid = new OccupationBid();
            bid.setSender(sender);
            bid.setUniversity(university);
            bid.setDormitory(dormitory);
            bid.setText(text);
            bid.setStatus(status);
            bid.setFiles(files);
            return bid;
        }
    }

    public static class EvictionBidBuilder {
        private User sender;
        private String text = "Test eviction bid";
        private Bid.Status status = Bid.Status.IN_PROCESS;
        private final List<BidFile> files = new ArrayList<>();

        public EvictionBidBuilder withSender(User sender) {
            this.sender = sender;
            return this;
        }

        public EvictionBidBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public EvictionBidBuilder withStatus(Bid.Status status) {
            this.status = status;
            return this;
        }

        public Bid build() {
            Bid bid = new Bid();
            bid.setType(Bid.Type.EVICTION);
            bid.setSender(sender);
            bid.setText(text);
            bid.setStatus(status);
            bid.setFiles(files);
            return bid;
        }
    }

    public static class DepartureBidBuilder {
        private User sender;
        private LocalDate dayFrom = LocalDate.now().plusDays(1);
        private LocalDate dayTo = LocalDate.now().plusDays(10);
        private String text = "Test departure bid";
        private Bid.Status status = Bid.Status.IN_PROCESS;
        private List<BidFile> files = new ArrayList<>();

        public DepartureBidBuilder withSender(User sender) {
            this.sender = sender;
            return this;
        }

        public DepartureBidBuilder withDayFrom(LocalDate dayFrom) {
            this.dayFrom = dayFrom;
            return this;
        }

        public DepartureBidBuilder withDayTo(LocalDate dayTo) {
            this.dayTo = dayTo;
            return this;
        }

        public DepartureBidBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public DepartureBidBuilder withStatus(Bid.Status status) {
            this.status = status;
            return this;
        }

        public DepartureBid build() {
            DepartureBid bid = new DepartureBid();
            bid.setSender(sender);
            bid.setDayFrom(dayFrom);
            bid.setDayTo(dayTo);
            bid.setText(text);
            bid.setStatus(status);
            bid.setFiles(files);
            return bid;
        }
    }

    public static class RoomChangeBidBuilder {
        private User sender;
        private Room roomTo;
        private Room.Type roomPreferType = Room.Type.BLOCK;
        private String text = "Test room change bid";
        private Bid.Status status = Bid.Status.IN_PROCESS;
        private List<BidFile> files = new ArrayList<>();

        public RoomChangeBidBuilder withSender(User sender) {
            this.sender = sender;
            return this;
        }

        public RoomChangeBidBuilder withRoomTo(Room roomTo) {
            this.roomTo = roomTo;
            return this;
        }

        public RoomChangeBidBuilder withRoomPreferType(Room.Type roomPreferType) {
            this.roomPreferType = roomPreferType;
            return this;
        }

        public RoomChangeBidBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public RoomChangeBidBuilder withStatus(Bid.Status status) {
            this.status = status;
            return this;
        }

        public RoomChangeBid build() {
            RoomChangeBid bid = new RoomChangeBid();
            bid.setSender(sender);
            bid.setRoomTo(roomTo);
            bid.setRoomPreferType(roomPreferType);
            bid.setText(text);
            bid.setStatus(status);
            bid.setFiles(files);
            return bid;
        }
    }

    // ========== Event Builder ==========

    public static class EventBuilder {
        private Event.Type type = Event.Type.IN;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Room room;
        private User user;
        private Integer paymentSum;

        public EventBuilder withType(Event.Type type) {
            this.type = type;
            return this;
        }

        public EventBuilder withTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EventBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public EventBuilder withUser(User user) {
            this.user = user;
            return this;
        }

        public EventBuilder withPaymentSum(Integer paymentSum) {
            this.paymentSum = paymentSum;
            return this;
        }

        public Event build() {
            Event event = new Event();
            event.setType(type);
            event.setTimestamp(timestamp);
            event.setRoom(room);
            event.setUsr(user);
            event.setPaymentSum(paymentSum);
            return event;
        }
    }
}

