package CricBuzz;
import java.util.List;
enum PlayerRole {
    BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER
}

enum BallType {
    NORMAL, WIDE , NO_BALL, DEAD_BALL;
}

enum MatchResult {
    SCHEDULED,
    LIVE,
    FINISHED,
    DRAWN,
    CANCELED
}

enum RunType {
    NORMAL,
    FOUR,
    SIX,
    LEG_BYE,
    BYE,
    NO_BALL,
    OVERTHROW
}

enum WicketType {
    BOWLED,
    CAUGHT,
    RUN_OUT,
    LBW,
    STUMPED,
    HIT_WICKET,
    RETIRED_HURT,
    // OBSTRUCTING
}

enum BattingStatus {
    NOT_OUT,
    OUT,
    RETIRED_OUT,
}
class Player {
    final String id;
    final String name;
    final PlayerRole role;
    final String country;

    Player(String id, String name, PlayerRole role, String country) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerRole getRole() {
        return role;
    }

    public String getCountry() {
        return country;
    }
}

class InningBattingStatus {
    private int runs;
    private int ballsfaced;
    private int fours;
    private int sixes;
    private BattingStatus status;
    private Wicket wicket;

}

class Wicket {
    private WicketType wicketType;
    private Player playerOut;
    private Player caughtBy;
    private Player runoutBy;
    private Player stumpedBy;
}
class Team {
    final String name;
    final List<Player> players;

    Team(String name, List<Player> players) {
        this.name = name;
        this.players = players;
    }
}

public class CricBuzzDemo {
}
