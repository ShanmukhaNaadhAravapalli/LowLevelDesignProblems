package MeetingScheduler.src;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.*;

enum MeetingStatus {
    SCHEDULED,
    CANCELLED,
}

enum ResponseStatus {
    PENDING,
    ACCEPTED,
    DECLINED    ,

}

class TimeInterval{
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    public TimeInterval(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isOverlapped(TimeInterval Other){
        if(Other == null)
            return true;// throw exception
        return this.startTime.isBefore(Other.getEndTime()) && this.endTime.isAfter(Other.getStartTime())  ;
    }
}

class User {
    private String userId;
    private String name;
    private Calendar calendar;

    public User(String userId, String name, Calendar calendar) {
        this.userId = userId;
        this.name = name;
        this.calendar = calendar;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Calendar getCalendar() {
        return calendar;
    }
}

class Participant {
    private final User user;
    private ResponseStatus resposne;

    public Participant(User user) {
        this.user = user;
        this.resposne = ResponseStatus.PENDING;
    }

    public User getUser() {
        return user;
    }

    public ResponseStatus getResposne() {
        return resposne;
    }

    public void setResponseStatus(ResponseStatus status){
        this.resposne = status;
    }
}

class Meeting {
    private final String meetingId;
    private final User organizer;
    private List<Participant> participants = new ArrayList<>();
    private final TimeInterval timeInterval;
    private final MeetingRoom meetingRoom;
    private MeetingStatus status ;


    public Meeting(String meetingId, User organizer, TimeInterval timeInterval, MeetingRoom meetingRoom, List<User> invitees) {
        this.meetingId = meetingId;
        this.organizer = organizer;
        this.timeInterval = timeInterval;
        this.meetingRoom = meetingRoom;
        invitees.forEach(u -> participants.add(new Participant(u)));
        this.status = MeetingStatus.SCHEDULED;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public User getOrganizer() {
        return organizer;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public MeetingRoom getMeetingRoom() {
        return meetingRoom;
    }

    public MeetingStatus getStatus() {
        return status;
    }

    public void cancel(){
        this.status = MeetingStatus.CANCELLED;
    }
}

class MeetingRoom {
    private String id;
    private String name;
    private int capacity;
    private  Calendar roomCalendar;

    public MeetingRoom(String id, String name, int capacity, Calendar roomCalendar) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.roomCalendar = roomCalendar;
    }

    public boolean isAvailableFor(TimeInterval interval, int requiredCapacity){
        if(requiredCapacity  > this.capacity)
            return false;
        return this.roomCalendar.isAvailable(interval);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Calendar getRoomCalender() {
        return roomCalendar;
    }

    public void book(Meeting meeting){
        roomCalendar.addMeeting(meeting);
    }
}
class NotificationService {
    public void notify(String msg) {
        System.out.println("[Notification] " + msg);
    }
}

class Calendar {
    private List<Meeting> meetings = new ArrayList<>();
    public synchronized void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }

    public synchronized void removeMeeting(Meeting meeting) {
        meetings.remove(meeting);
    }

    public synchronized boolean isAvailable(TimeInterval interval) {
        return meetings.stream()
                .filter(m -> m.getStatus() == MeetingStatus.SCHEDULED)
                .noneMatch(m -> m.getTimeInterval().isOverlapped(interval));
    }
}

class MeetingService {
    private final NotificationService notifier;
    private List<MeetingRoom> meetingRooms;

    public MeetingService(NotificationService notifier) {
        this.notifier = notifier;
    }

    public void addMeetingRoom(MeetingRoom room){
        meetingRooms.add(room);
    }

    public void removeMeeting(MeetingRoom room){
        meetingRooms.remove(room);
    }

    public Optional<MeetingRoom> checkForAvailableRooms(TimeInterval timeInterval, int capacity){
        for(MeetingRoom room : meetingRooms){
            if(room.isAvailableFor(timeInterval, capacity))
                return Optional.of(room);
        }
        return Optional.ofNullable(null);
    }

    public synchronized Optional<Meeting>  schedule( User organizer, List<User> invitees, TimeInterval timeInterval, int capacity){
        if (!organizer.getCalendar().isAvailable(timeInterval)) {
            System.out.println("Organizer is busy.");
            return Optional.empty();
        }
        Optional<MeetingRoom> avaiableMeetingRoom = checkForAvailableRooms(timeInterval, invitees.size() + 1);
        if(avaiableMeetingRoom.isEmpty()) {
            System.out.println("===>>> No available meeting room");
            return Optional.ofNullable(null);
        }
        System.out.println("\n===>>> Available meeting room found");
        Meeting meeting = new Meeting(UUID.randomUUID().toString(), organizer, timeInterval, avaiableMeetingRoom.get(), invitees );
        organizer.getCalendar().addMeeting(meeting);
        for(User user : invitees){
            user.getCalendar().addMeeting(meeting);
        }
        avaiableMeetingRoom.get().book(meeting);
        notifier.notify("Meeting scheduled in room " + avaiableMeetingRoom.get().getName());
        return Optional.of(meeting);
    }

    public synchronized void cancelMeeting(Meeting meeting){
        System.out.println("\n===>>> Cancelling meeting: " + meeting.getMeetingId());
        meeting.getMeetingRoom().getRoomCalender().removeMeeting(meeting);
        meeting.getOrganizer().getCalendar().removeMeeting(meeting);
        for(Participant p : meeting.getParticipants()){
            p.getUser().getCalendar().removeMeeting(meeting);
        }
    }

}
public class MeetingSchedulerDemo {
}
