package stackOverflow;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

enum VoteType {
    UPVOTE(1), DOWNVOTE(-1);
    private final int value;
    VoteType(int value){
        this.value  = value;
    }
    public int getValue(){
        return this.value;
    }
}

class Tag {
    private final String name;
    public Tag(String name) { this.name = name.toLowerCase(); }
    public String getName() { return name; }
}

class User {
    private final int id;
    private final String name;
    private AtomicInteger reputation;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
        this.reputation = new AtomicInteger(0);
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getReputation() {
        return reputation.get();
    }

    public void updateReputation(int change){
        this.reputation.addAndGet(change);
    }
}

abstract class Content {
    private final int id;
    private final User author;
    private final String body;
    private final LocalDateTime creationTime;

    public Content(int id, User author, String body) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.creationTime = LocalDateTime.now(); // which is better get from co nstructior or intitlizaehere
    }

    public int getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }
}

abstract class Post extends  Content{
    private AtomicInteger reputation ;
    private final List<Comment> comments;
    private final Map<String , VoteType > votes;
    public Post(int id, String body, User author){
        super(id, author, body);
        this.reputation = new AtomicInteger(0);
        this.comments = new CopyOnWriteArrayList<>();
        this.votes = new ConcurrentHashMap<>();
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void updateReputation(int delta){
        reputation.addAndGet(delta);
    }
}

class Question extends Post {
    private final String title;
    private final Set<Tag> tags;
    private final List<Answer> answers ;
    private Answer accptedAnswer;

    public Question(int id, String body, User author, String title, Set<Tag> tags) {
        super(id, body, author);
        this.title = title;
        this.tags = tags;
        this.answers = new ArrayList<>();
    }

    public void addAnswer(Answer answer){
        answers.add(answer);
    }
    public synchronized boolean acceptAnswer(Answer answer){
        if(accptedAnswer!= null && !this.getAuthor().getId().equals(answer.getAuthor().getId()) ){
            this.accptedAnswer = answer;
            answer.setAccepted(true);
            return true;
        }
        return false;
    }


    public Set<Tag> getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }
}
class Answer extends Post{
    private final Question question;
    private boolean isAccepted;

    public Answer(int id, String body, User author, Question question) {
        super(id, body, author);
        this.isAccepted = false;
        this.question = question;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
class Comment extends Content{
    private final Post parentPost;
    public Comment(int id, User author, String body, Post parentPost) {
        super(id, author, body);
        this.parentPost = parentPost;
    }

    public Post getParentPost() {
        return parentPost;
    }
}
class Vote {
    private final int voterId;
    private final Post post;
    private final VoteType voteType;

    public Vote(int voterId, Post post, VoteType voteType) {
        this.voterId = voterId;
        this.post = post;
        this.voteType = voteType;
    }

    public int getVoterId() {
        return voterId;
    }

    public Post getPost() {
        return post;
    }

    public VoteType getVoteType() {
        return voteType;
    }
}
interface ReputationService {
    void addReputation(User user, int delta);
}

class DefaultReputationService implements ReputationService {
    public void addReputation(User user, int delta){
        int newReputation = user.getReputation() + delta;
        if(newReputation < 0)
            newReputation = 0; // no negative reputation.
        user.updateReputation(newReputation);
    }
}

class UserManager {
    private static UserManager instance;
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger userIdSeq = new AtomicInteger(0);

    private UserManager() {}

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    public User createUser(String username) {
        User user = new User(userIdSeq.incrementAndGet(), username);
        users.put(user.getId(), user);
        return user;
    }

    public User getUser(int id) {
        return users.get(id);
    }
}
class QuestionManager {
    private static QuestionManager instance;
    private final Map<Integer, Question> questions = new ConcurrentHashMap<>();
    private final AtomicInteger questionIdSeq = new AtomicInteger(0);

    private QuestionManager() {}

    public static QuestionManager getInstance() {
        if (instance == null) instance = new QuestionManager();
        return instance;
    }

    public Question postQuestion(User author, String title, String content, Set<Tag> tags) {
        Question q = new Question(questionIdSeq.incrementAndGet(), content, author, title, tags);
        questions.put(q.getId(), q);
        return q;
    }

    public Question getQuestion(int id) {
        return questions.get(id);
    }

    public Collection<Question> getAllQuestions() {
        return questions.values();
    }
}

class AnswerManager {
    private static AnswerManager instance;
    private final Map<Integer, Answer> answers = new ConcurrentHashMap<>() ;
    private final AtomicInteger answerSeq = new AtomicInteger(0);
    private AnswerManager() {}
    public static AnswerManager getInstance(){
        if(instance == null){
            instance = new AnswerManager();
        }
        return instance;
    }
    public Answer postAnswer(User author, Question question, String content){
        Answer ans = new Answer(answerSeq.incrementAndGet(), content,author, question);
        answers.put(ans.getId(), ans);
        question.addAnswer(ans);
        return ans;
    }

    public Answer getAnswer(int answerId){
        return answers.get(answerId);
    }

}

class CommentsManager {
    private static CommentsManager instance;
    private final Map<Integer, Comment> commentMap = new ConcurrentHashMap<>();
    private final AtomicInteger commentSeq = new AtomicInteger(0);
    private CommentsManager(){}

    public static CommentsManager getInstance(){
        if(instance == null)
            instance = new CommentsManager();
        return instance;
    }

    public void addComment( Post post, User author, String content){
        Comment c = new Comment(commentSeq.incrementAndGet(), author, content, post);
        commentMap.put(c.getId(), c);
        post.addComment(c);
    }
    public Comment getComment(int id) {
        return commentMap.get(id);
    }

}

class VotesManager {
    private static  VotesManager instance;
    private final Map<Integer, Map<Integer, Vote>> votesMap = new ConcurrentHashMap<>();
    private final AtomicInteger votesSeq = new AtomicInteger(0);
    private final ReputationService reputationService;
    private VotesManager(ReputationService reputationService){
        this.reputationService = reputationService;
    };
    public static VotesManager getInstance( ){
        if(instance == null)
            instance = new VotesManager(new DefaultReputationService());
        return instance;
    }
    public void addVotes(User voter, Post post, VoteType voteType){
        votesMap.computeIfAbsent(post.getId(), k -> new ConcurrentHashMap<>());
        Map<Integer, Vote> postVotes = votesMap.get(post.getId());
        if (postVotes.containsKey(voter.getId())) {
            throw new IllegalStateException("User already voted");
        }
        Vote v= new Vote(votesSeq.incrementAndGet(), post, voteType);
        postVotes.put(voter.getId(), v);
        post.updateReputation(voteType.getValue());
        User author = post.getAuthor();
        int repChange = 0;
        if (voteType == VoteType.UPVOTE) {
            if (post instanceof Question) repChange = 5;
            else if (post instanceof Answer) repChange = 10;
        } else {
            repChange = -2;
        }
        reputationService.addReputation(author, repChange);

    }
}



interface SearchStrategy {
    List<Question> search(String query, QuestionManager questionManager);
}

class KeywordSearchStrategy implements SearchStrategy {
    public List<Question> search(String query, QuestionManager questionManager){
        List<Question> results = new ArrayList<>();
        for(Question q : questionManager.getAllQuestions()){
            if(q.getTitle().equalsIgnoreCase(query)){
                results.add(q);
            }
        }
       return results;
    }
}
class TagSearchStrategy implements SearchStrategy {
    public List<Question> search(String tagName, QuestionManager questionManager){
        List<Question> results = new ArrayList<>();
        for(Question q : questionManager.getAllQuestions()){
            for (Tag tag : q.getTags()) {
                if (tag.getName().equalsIgnoreCase(tagName)) {
                    results.add(q);
                    break;
                }
            }
        }
        return results;
    }
}

class UsernameSearchStrategy implements SearchStrategy {
    public List<Question> search(String username, QuestionManager questionManager){
        List<Question> results = new ArrayList<>();
        for(Question q : questionManager.getAllQuestions()){
            if(q.getAuthor().getName().equalsIgnoreCase(username)){
                results.add(q);
            }
        }
        return results;
    }
}

class SearchManager {
    private SearchStrategy strategy;

    public SearchManager() {
    }
    public void setStrategy(SearchStrategy strategy){
        this.strategy = strategy;
    }

    public List<Question> search(String key, QuestionManager questionManager){
        return strategy.search(key, questionManager);
    }
}
class StackOverflow {

    private static StackOverflow instance;

    private final UserManager userManager = UserManager.getInstance();
    private final QuestionManager questionManager = QuestionManager.getInstance();
    private final AnswerManager answerManager = AnswerManager.getInstance();
    private final CommentsManager commentsManager = CommentsManager.getInstance();
    private final VotesManager votesManager = VotesManager.getInstance();

    private StackOverflow() {}

    public static StackOverflow getInstance(){
        if(instance == null) {
            instance = new StackOverflow();
        }
        return instance;
    }

    // ---------- USER ----------
    public User createUser(String name){
        return userManager.createUser(name);
    }

    public User getUser(int userId){
        return userManager.getUser(userId);
    }

    // ---------- QUESTION ----------
    public Question postQuestion(int userId, String title, String body, Set<Tag> tags){
        User user = userManager.getUser(userId);
        if(user == null) throw new IllegalArgumentException("User not found");
        return questionManager.postQuestion(user, title, body, tags);
    }

    // ---------- ANSWER ----------
    public Answer postAnswer(int userId, int questionId, String body){
        User user = userManager.getUser(userId);
        Question question = questionManager.getQuestion(questionId);

        if(user == null || question == null)
            throw new IllegalArgumentException("Invalid user or question");

        return answerManager.postAnswer(user, question, body);
    }

    // ---------- COMMENT ----------
    public void addComment(int userId, int postId, String body){
        User user = userManager.getUser(userId);
        Post post = findPostById(postId);

        commentsManager.addComment(post, user, body);
    }

    // ---------- VOTE ----------
    public void voteOnPost(int userId, int postId, VoteType voteType){
        User voter = userManager.getUser(userId);
        Post post = findPostById(postId);

        votesManager.addVotes(voter, post, voteType);
    }

    // ---------- SEARCH ----------
    public List<Question> searchQuestions(String key, SearchStrategy strategy){
        SearchManager searchManager = new SearchManager();
        searchManager.setStrategy(strategy);
        return searchManager.search(key, questionManager);
    }

    // ---------- HELPERS ----------
    private Post findPostById(int postId){
        Question q = questionManager.getQuestion(postId);
        if(q != null) return q;

        return answerManager.getAnswer(postId);
    }
}

public class StackOverflowDemo {
}
