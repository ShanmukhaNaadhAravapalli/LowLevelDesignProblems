package Logger;

import java.util.*;

enum LogLevel {
    INFO(1), ERROR(2), DEBUG(3);
    private final int level;
    LogLevel(int value) { this.level = value; }
    public int getLevel() { return level; }
}

interface LogObserver {
    void log(String message);
}

class FileLogger implements LogObserver {
    public void log(String message) {
        System.out.println("Logging into file: " + message);
    }
}

class ConsoleLogger implements LogObserver {
    public void log(String message) {
        System.out.println("Logging into Console: " + message);
    }
}

class DatabaseLogger implements LogObserver {
    public void log(String message) {
        System.out.println("Logging into Database: " + message);
    }
}

class LogSinkSubject {
    private final Map<LogLevel, List<LogObserver>> observers = new EnumMap<>(LogLevel.class);
    public void addObserver(LogLevel level , LogObserver observer){
        observers.computeIfAbsent(level, l -> new ArrayList<>()).add(observer);
    }
    public void notifyObservers(LogLevel level, String message){
        List<LogObserver> levelObservers = observers.get(level);
        if (levelObservers != null) {
            levelObservers.forEach(observer -> observer.log(message));
        }
    }
}

abstract class LoggerHandler {
    protected LoggerHandler nextLoggerHandler;
    protected LogLevel logLevel;

    public void setNextLoggerHandler(LoggerHandler nextLoggerHandler) {
        this.nextLoggerHandler = nextLoggerHandler;
    }

    public void log(LogLevel level, String message, LogSinkSubject logSinkSubject) {
        if (this.logLevel == level) {
            publishLog(message, logSinkSubject);
        }
        if (nextLoggerHandler != null) {
            nextLoggerHandler.log(level, message, logSinkSubject);
        }
    }

    protected abstract void publishLog(String message, LogSinkSubject logSinkSubject);
}

class InfoLogger extends LoggerHandler {
    public InfoLogger(LogLevel level) { this.logLevel = level; }
    public void publishLog(String message, LogSinkSubject subject) {
        subject.notifyObservers(LogLevel.INFO, "Info: " + message);
    }
}

class ErrorLogger extends LoggerHandler {
    public ErrorLogger(LogLevel level) { this.logLevel = level; }
    public void publishLog(String message, LogSinkSubject subject) {
        subject.notifyObservers(LogLevel.ERROR, "Error: " + message);
    }
}

class DebugLogger extends LoggerHandler {
    public DebugLogger(LogLevel level) { this.logLevel = level; }
    public void publishLog(String message, LogSinkSubject subject) {
        subject.notifyObservers(LogLevel.DEBUG, "Debug: " + message);
    }
}

class LogManager {
    public static LoggerHandler buildLoggerChain() {
        LoggerHandler infoLogger = new InfoLogger(LogLevel.INFO);
        LoggerHandler errorLogger = new ErrorLogger(LogLevel.ERROR);
        LoggerHandler debugLogger = new DebugLogger(LogLevel.DEBUG);
        infoLogger.setNextLoggerHandler(errorLogger);
        errorLogger.setNextLoggerHandler(debugLogger);
        return infoLogger;
    }

    public static LogSinkSubject buildLogSinkSubject() {
        LogSinkSubject subject = new LogSinkSubject();
        subject.addObserver(LogLevel.INFO, new ConsoleLogger());
        subject.addObserver(LogLevel.ERROR, new FileLogger());
        subject.addObserver(LogLevel.DEBUG, new DatabaseLogger());
        return subject;
    }
}

class Logger {
    private final static Logger loggerInstance = new Logger();
    private final static LoggerHandler loggerHandler = LogManager.buildLoggerChain();
    private final static LogSinkSubject logSinkSubject = LogManager.buildLogSinkSubject();

    private Logger() {}

    public static Logger getLoggerInstance() {
        return loggerInstance;
    }

    private void logMessage(String message, LogLevel level) {
        loggerHandler.log(level, message, logSinkSubject);
    }

    public void info(String message) {
        logMessage(message, LogLevel.INFO);
    }

    public void error(String message) {
        logMessage(message, LogLevel.ERROR);
    }

    public void debug(String message) {
        logMessage(message, LogLevel.DEBUG);
    }
}

public class LoggerApplication {
    public static void main(String[] args) {
        Logger logger = Logger.getLoggerInstance();

        logger.info("This is info");
        logger.error("This is error");
        logger.debug("This is debug");
    }
}
