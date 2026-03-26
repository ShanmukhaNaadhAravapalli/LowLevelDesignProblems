package IssueManagementSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/* =========================
   ===== ENUMS ============
   ========================= */

enum IssueType {
    BUG, FEATURE, STORY, SUBTASK
}

enum IssueStatus {
    OPEN, IN_PROGRESS, TESTING, DONE, CLOSED
}

enum IssuePriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

/* =========================
   ===== USER =============
   ========================= */

class User {
    private final long id;
    private final String name;
    private final String email;
    private final List<Long> assignedIssueIds;
    private final LocalDateTime createdAt;

    public User(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.assignedIssueIds = Collections.synchronizedList(new ArrayList<>());
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Long> getAssignedIssueIds() {
        return new ArrayList<>(assignedIssueIds);
    }

    public void addIssue(long issueId) {
        assignedIssueIds.add(issueId);
    }

    public void removeIssue(long issueId) {
        assignedIssueIds.remove(issueId);
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name=%s, email=%s]", id, name, email);
    }
}

/* =========================
   ===== ISSUE ============
   ========================= */

abstract class Issue {
    private final long id;
    private final String title;
    private String description;
    private final IssueType type;
    private IssueStatus status;
    private IssuePriority priority;
    private User assignee;
    private final User reporter;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate dueDate;
    private final List<String> comments;
    private Long sprintId;
    private final Map<String, Object> metadata;

    public Issue(long id, String title, String description, IssueType type,
                 User reporter, LocalDate dueDate, IssuePriority priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.reporter = reporter;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : IssuePriority.MEDIUM;
        this.status = IssueStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.comments = Collections.synchronizedList(new ArrayList<>());
        this.metadata = new ConcurrentHashMap<>();
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IssueType getType() { return type; }
    public IssueStatus getStatus() { return status; }
    public IssuePriority getPriority() { return priority; }
    public User getAssignee() { return assignee; }
    public User getReporter() { return reporter; }
    public LocalDate getDueDate() { return dueDate; }
    public Long getSprintId() { return sprintId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<String> getComments() { return new ArrayList<>(comments); }

    // Setters
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignTo(User user) {
        this.assignee = user;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(IssueStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
        this.updatedAt = LocalDateTime.now();
    }

    public void addComment(String comment) {
        comments.add(String.format("[%s] %s", LocalDateTime.now(), comment));
        this.updatedAt = LocalDateTime.now();
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public boolean isOverdue() {
        return dueDate != null &&
                LocalDate.now().isAfter(dueDate) &&
                status != IssueStatus.DONE &&
                status != IssueStatus.CLOSED;
    }

    public abstract void print();

    protected String getBasicInfo() {
        return String.format("%s: %s | Status: %s | Priority: %s | Assignee: %s",
                type,
                title,
                status,
                priority,
                assignee != null ? assignee.getName() : "Unassigned");
    }
}

/* =========================
   ===== ISSUE TYPES ======
   ========================= */

class Story extends Issue {
    private final List<Long> subTaskIds;

    public Story(long id, String title, String description, User reporter,
                 LocalDate dueDate, IssuePriority priority) {
        super(id, title, description, IssueType.STORY, reporter, dueDate, priority);
        this.subTaskIds = Collections.synchronizedList(new ArrayList<>());
    }

    public void addSubTask(long subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public void removeSubTask(long subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    public List<Long> getSubTaskIds() {
        return new ArrayList<>(subTaskIds);
    }

    @Override
    public void print() {
        System.out.println(getBasicInfo());
        if (!subTaskIds.isEmpty()) {
            System.out.println("  SubTasks: " + subTaskIds.size());
        }
    }
}

class Bug extends Issue {
    private String severity;
    private String stepsToReproduce;

    public Bug(long id, String title, String description, User reporter,
               LocalDate dueDate, IssuePriority priority) {
        super(id, title, description, IssueType.BUG, reporter, dueDate, priority);
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setStepsToReproduce(String steps) {
        this.stepsToReproduce = steps;
    }

    @Override
    public void print() {
        System.out.println(getBasicInfo() +
                (severity != null ? " | Severity: " + severity : ""));
    }
}

class Feature extends Issue {
    private String businessValue;

    public Feature(long id, String title, String description, User reporter,
                   LocalDate dueDate, IssuePriority priority) {
        super(id, title, description, IssueType.FEATURE, reporter, dueDate, priority);
    }

    public void setBusinessValue(String businessValue) {
        this.businessValue = businessValue;
    }

    @Override
    public void print() {
        System.out.println(getBasicInfo());
    }
}

class SubTask extends Issue {
    private final long parentStoryId;

    public SubTask(long id, String title, String description, User reporter,
                   LocalDate dueDate, IssuePriority priority, long parentStoryId) {
        super(id, title, description, IssueType.SUBTASK, reporter, dueDate, priority);
        this.parentStoryId = parentStoryId;
        addMetadata("parentStoryId", parentStoryId);
    }

    public long getParentStoryId() {
        return parentStoryId;
    }

    @Override
    public void print() {
        System.out.println("  └─ " + getBasicInfo());
    }
}

/* =========================
   ===== SPRINT ===========
   ========================= */

class Sprint {
    private final long id;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<Long> issueIds;
    private String goal;
    private boolean active;

    public Sprint(long id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.issueIds = Collections.synchronizedList(new ArrayList<>());
        this.active = true;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getGoal() { return goal; }
    public boolean isActive() { return active; }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addIssue(long issueId) {
        issueIds.add(issueId);
    }

    public void removeIssue(long issueId) {
        issueIds.remove(issueId);
    }

    public List<Long> getIssueIds() {
        return new ArrayList<>(issueIds);
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(endDate);
    }

    @Override
    public String toString() {
        return String.format("Sprint[id=%d, name=%s, issues=%d, active=%b]",
                id, name, issueIds.size(), active);
    }
}

/* =========================
   ===== REPOSITORIES =====
   ========================= */

class IssueRepository {
    private final Map<Long, Issue> issues = new ConcurrentHashMap<>();

    public void save(Issue issue) {
        issues.put(issue.getId(), issue);
    }

    public Optional<Issue> findById(long issueId) {
        return Optional.ofNullable(issues.get(issueId));
    }

    public Collection<Issue> findAll() {
        return new ArrayList<>(issues.values());
    }

    public List<Issue> findByAssignee(long userId) {
        return issues.values().stream()
                .filter(issue -> issue.getAssignee() != null &&
                        issue.getAssignee().getId() == userId)
                .collect(Collectors.toList());
    }

    public List<Issue> findByStatus(IssueStatus status) {
        return issues.values().stream()
                .filter(issue -> issue.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Issue> findBySprint(long sprintId) {
        return issues.values().stream()
                .filter(issue -> issue.getSprintId() != null &&
                        issue.getSprintId() == sprintId)
                .collect(Collectors.toList());
    }

    public boolean delete(long issueId) {
        return issues.remove(issueId) != null;
    }

    public int count() {
        return issues.size();
    }
}

class UserRepository {
    private final Map<Long, User> users = new ConcurrentHashMap<>();

    public void save(User user) {
        users.put(user.getId(), user);
    }

    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public boolean delete(long userId) {
        return users.remove(userId) != null;
    }
}

class SprintRepository {
    private final Map<Long, Sprint> sprints = new ConcurrentHashMap<>();

    public void save(Sprint sprint) {
        sprints.put(sprint.getId(), sprint);
    }

    public Optional<Sprint> findById(long sprintId) {
        return Optional.ofNullable(sprints.get(sprintId));
    }

    public Collection<Sprint> findAll() {
        return new ArrayList<>(sprints.values());
    }

    public List<Sprint> findActiveSprints() {
        return sprints.values().stream()
                .filter(Sprint::isActive)
                .collect(Collectors.toList());
    }

    public boolean delete(long sprintId) {
        return sprints.remove(sprintId) != null;
    }
}

/* =========================
   ===== SERVICES =========
   ========================= */

class UserService {
    private final UserRepository userRepository;
    private final AtomicLong userIdGenerator = new AtomicLong(1);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name, String email) {
        long userId = userIdGenerator.getAndIncrement();
        User user = new User(userId, name, email);
        userRepository.save(user);
        return user;
    }

    public Optional<User> getUser(long userId) {
        return userRepository.findById(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userRepository.findAll());
    }

    public void deleteUser(long userId) {
        userRepository.delete(userId);
    }
}

class IssueService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final AtomicLong issueIdGenerator = new AtomicLong(1);

    public IssueService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public Issue createIssue(IssueType type, String title, String description,
                             long reporterId, LocalDate dueDate,
                             IssuePriority priority, Map<String, Object> metadata) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        long issueId = issueIdGenerator.getAndIncrement();
        Issue issue;

        switch (type) {
            case STORY:
                issue = new Story(issueId, title, description, reporter, dueDate, priority);
                break;
            case BUG:
                issue = new Bug(issueId, title, description, reporter, dueDate, priority);
                break;
            case FEATURE:
                issue = new Feature(issueId, title, description, reporter, dueDate, priority);
                break;
            case SUBTASK:
                if (metadata == null || !metadata.containsKey("parentStoryId")) {
                    throw new RuntimeException("SubTask requires parentStoryId in metadata");
                }
                long parentStoryId = ((Number) metadata.get("parentStoryId")).longValue();

                // Validate parent story exists
                Issue parentStory = issueRepository.findById(parentStoryId)
                        .orElseThrow(() -> new RuntimeException("Parent story not found"));

                if (!(parentStory instanceof Story)) {
                    throw new RuntimeException("Parent must be a Story");
                }

                issue = new SubTask(issueId, title, description, reporter,
                        dueDate, priority, parentStoryId);
                ((Story) parentStory).addSubTask(issueId);
                break;
            default:
                throw new RuntimeException("Unknown issue type");
        }

        if (metadata != null) {
            metadata.forEach(issue::addMetadata);
        }

        issueRepository.save(issue);
        return issue;
    }

    public void assignIssue(long issueId, long userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove from previous assignee
        if (issue.getAssignee() != null) {
            issue.getAssignee().removeIssue(issueId);
        }

        issue.assignTo(user);
        user.addIssue(issueId);
    }

    public void changeStatus(long issueId, IssueStatus status) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        issue.changeStatus(status);
    }

    public void addComment(long issueId, String comment) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));
        issue.addComment(comment);
    }

    public Optional<Issue> getIssue(long issueId) {
        return issueRepository.findById(issueId);
    }

    public List<Issue> getAllIssues() {
        return new ArrayList<>(issueRepository.findAll());
    }

    public List<Issue> getIssuesByUser(long userId) {
        return issueRepository.findByAssignee(userId);
    }

    public Map<IssueType, List<Issue>> getUserIssuesByType(long userId) {
        return issueRepository.findByAssignee(userId).stream()
                .collect(Collectors.groupingBy(Issue::getType));
    }

    public List<Issue> getOverdueIssues() {
        return issueRepository.findAll().stream()
                .filter(Issue::isOverdue)
                .collect(Collectors.toList());
    }

    public void deleteIssue(long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        // If it's a subtask, remove from parent story
        if (issue instanceof SubTask) {
            SubTask subTask = (SubTask) issue;
            issueRepository.findById(subTask.getParentStoryId())
                    .ifPresent(parent -> {
                        if (parent instanceof Story) {
                            ((Story) parent).removeSubTask(issueId);
                        }
                    });
        }

        // Remove from assignee
        if (issue.getAssignee() != null) {
            issue.getAssignee().removeIssue(issueId);
        }

        issueRepository.delete(issueId);
    }
}

class SprintService {
    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final AtomicLong sprintIdGenerator = new AtomicLong(1);

    public SprintService(SprintRepository sprintRepository, IssueRepository issueRepository) {
        this.sprintRepository = sprintRepository;
        this.issueRepository = issueRepository;
    }

    public Sprint createSprint(String name, LocalDate startDate, LocalDate endDate, String goal) {
        long sprintId = sprintIdGenerator.getAndIncrement();
        Sprint sprint = new Sprint(sprintId, name, startDate, endDate);
        if (goal != null) {
            sprint.setGoal(goal);
        }
        sprintRepository.save(sprint);
        return sprint;
    }

    public void addIssueToSprint(long sprintId, long issueId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        // Remove from previous sprint if exists
        if (issue.getSprintId() != null) {
            sprintRepository.findById(issue.getSprintId())
                    .ifPresent(oldSprint -> oldSprint.removeIssue(issueId));
        }

        sprint.addIssue(issueId);
        issue.setSprintId(sprintId);
    }

    public void removeIssueFromSprint(long sprintId, long issueId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        sprint.removeIssue(issueId);
        issue.setSprintId(null);
    }

    public void closeSprint(long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));
        sprint.setActive(false);
    }

    public Optional<Sprint> getSprint(long sprintId) {
        return sprintRepository.findById(sprintId);
    }

    public List<Sprint> getAllSprints() {
        return new ArrayList<>(sprintRepository.findAll());
    }

    public List<Sprint> getActiveSprints() {
        return sprintRepository.findActiveSprints();
    }

    public void printSprint(long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Sprint: " + sprint.getName());
        System.out.println("Duration: " + sprint.getStartDate() + " to " + sprint.getEndDate());
        if (sprint.getGoal() != null) {
            System.out.println("Goal: " + sprint.getGoal());
        }
        System.out.println("Status: " + (sprint.isActive() ? "Active" : "Closed"));
        System.out.println("=".repeat(60));

        List<Issue> issues = issueRepository.findBySprint(sprintId);

        if (issues.isEmpty()) {
            System.out.println("No issues in this sprint");
        } else {
            Map<IssueType, List<Issue>> groupedIssues = issues.stream()
                    .collect(Collectors.groupingBy(Issue::getType));

            groupedIssues.forEach((type, typeIssues) -> {
                System.out.println("\n" + type + "s (" + typeIssues.size() + "):");
                typeIssues.forEach(issue -> {
                    System.out.print("  ");
                    issue.print();

                    // Print subtasks if it's a story
                    if (issue instanceof Story) {
                        Story story = (Story) issue;
                        for (long subTaskId : story.getSubTaskIds()) {
                            issueRepository.findById(subTaskId)
                                    .ifPresent(subTask -> {
                                        System.out.print("    ");
                                        subTask.print();
                                    });
                        }
                    }
                });
            });
        }
        System.out.println();
    }
}

/* =========================
   ===== MAIN SERVICE =====
   ========================= */

interface TaskPlannerService {
    Sprint createSprint(String name, LocalDate startDate, LocalDate endDate, String goal);
    Issue createTask(IssueType type, String title, String description,
                     long reporterId, LocalDate dueDate, IssuePriority priority,
                     Map<String, Object> metadata);
    void addSprintTask(long sprintId, long taskId);
    void removeSprintTask(long sprintId, long taskId);
    void changeTaskStatus(long taskId, IssueStatus status);
    void changeTaskAssignee(long taskId, long userId);
    User createUser(String name, String email);
    Map<IssueType, List<Issue>> getUserTasksByType(long userId);
}

class TaskPlannerServiceImpl implements TaskPlannerService {
    private final UserService userService;
    private final IssueService issueService;
    private final SprintService sprintService;

    public TaskPlannerServiceImpl(UserService userService,
                                  IssueService issueService,
                                  SprintService sprintService) {
        this.userService = userService;
        this.issueService = issueService;
        this.sprintService = sprintService;
    }

    @Override
    public Sprint createSprint(String name, LocalDate startDate,
                               LocalDate endDate, String goal) {
        return sprintService.createSprint(name, startDate, endDate, goal);
    }

    @Override
    public Issue createTask(IssueType type, String title, String description,
                            long reporterId, LocalDate dueDate,
                            IssuePriority priority, Map<String, Object> metadata) {
        return issueService.createIssue(type, title, description,
                reporterId, dueDate, priority, metadata);
    }

    @Override
    public void addSprintTask(long sprintId, long taskId) {
        sprintService.addIssueToSprint(sprintId, taskId);
    }

    @Override
    public void removeSprintTask(long sprintId, long taskId) {
        sprintService.removeIssueFromSprint(sprintId, taskId);
    }

    @Override
    public void changeTaskStatus(long taskId, IssueStatus status) {
        issueService.changeStatus(taskId, status);
    }

    @Override
    public void changeTaskAssignee(long taskId, long userId) {
        issueService.assignIssue(taskId, userId);
    }

    @Override
    public User createUser(String name, String email) {
        return userService.createUser(name, email);
    }

    @Override
    public Map<IssueType, List<Issue>> getUserTasksByType(long userId) {
        return issueService.getUserIssuesByType(userId);
    }
}

/* =========================
   ===== DEMO =============
   ========================= */

public class IsssueManagementSystem{
    public static void main(String[] args) {
    }
}