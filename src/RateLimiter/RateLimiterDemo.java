package RateLimiter;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

interface RateLimiter {
    /**
     * @param key userId/IP/apiKey etc.
     * @return true if request allowed, false if rate-limited.
     */
    boolean allowRequest(String key);
}

class TokenBucketRateLimiter implements RateLimiter{
    private final int capacity;
    private final int refillRatePerSecond;
    private ConcurrentHashMap<String , TokenBucket> buckets;

    public TokenBucketRateLimiter(int capacity, int refillRatePerSecond, ConcurrentHashMap<String, TokenBucket> buckets) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.buckets = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String key){
        TokenBucket bucket = buckets.computeIfAbsent(key , k -> new TokenBucket(capacity,refillRatePerSecond));
        return bucket.allowRequest();
    }

}
class TokenBucket {
    private final int capacity;
    private final int refillRatePerSecond;

    private double tokens;
    private Instant lastRefillTimestamp;

    public TokenBucket(int capacity, int refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.tokens = capacity;
        this.lastRefillTimestamp = Instant.now();
    }

    public synchronized boolean allowRequest() {
        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    private void refill() {
        double secondsPassed = (Instant.now().toEpochMilli() - lastRefillTimestamp.toEpochMilli()) / 1_000.0;
        double tokensToAdd = secondsPassed * refillRatePerSecond;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTimestamp = Instant.now();
    }
}

class LeakyBucketRateLimiter implements RateLimiter {
    private final int capacity ;
    private final double leakRatePerSecond;
    private Map<String, LeakyBucket > buckets = new HashMap<>();
    public LeakyBucketRateLimiter(int capacity, double leakRatePerSecond) {
        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
    }

    @Override
    public boolean allowRequest(String key) {
        LeakyBucket bucket = buckets.computeIfAbsent(key, k -> new LeakyBucket(capacity, leakRatePerSecond));
        return bucket.allowRequest();
    }
}
class LeakyBucket {
    private final int capacity;
    private final double leakRatePerSecond;
    private double water;
    private Instant lastLeakTimeStamp;

    public LeakyBucket(int capacity, double leakRatePerSecond) {
        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
        this.water = 0.0;
        this.lastLeakTimeStamp = Instant.now();
    }
    public synchronized boolean allowRequest(){
        leak();
        if(water + 1.0 <= capacity){
            water += 1.0;
            return true;
        }
        return false;
    }

    private void leak() {
        double elapsed =(Instant.now().toEpochMilli() - lastLeakTimeStamp.toEpochMilli())/1_000.0;
        double leaked = elapsed * leakRatePerSecond;
        water = Math.max(0.0, water - leaked);
        lastLeakTimeStamp = Instant.now();

    }
}

class FixedWindowRateLimiter implements RateLimiter {
    public final long maxRequestsPerWindow;
    public final long windowSizeInSeconds;

    private static class Window {
        long windowStart;
        long count;

        public Window(Long now) {
            this.windowStart = now;
            this.count = 0;
        }
    }
    private final Map<String, Window>  windows= new ConcurrentHashMap<>();
    public FixedWindowRateLimiter(long maxRequestsPerWindow, long windowSizeInSeconds) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSizeInSeconds = windowSizeInSeconds;
    }

    @Override
    public boolean allowRequest(String key) {
        long now = Instant.now().getEpochSecond();
        Window window = windows.computeIfAbsent(key, k-> new Window(now));
        synchronized (window) {
            if (now - window.windowStart >= windowSizeInSeconds) {
                window.windowStart = now;
                window.count = 0;
            }
            if (window.count + 1 <= maxRequestsPerWindow) {
                window.count++;
                return true;
            }
            return false;
        }
    }
}

class SlidingWindowLogRateLimiter implements RateLimiter {
    private final int maxRequests;
    private final long windowSizeMs;
    private final Map<String, Queue<Long>> requestLogs = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public boolean allowRequest(String key) {
        Queue<Long> requestLog = requestLogs.computeIfAbsent(key, k-> new LinkedList<>());
        long now = Instant.now().toEpochMilli();
        synchronized(requestLog){
            while(!requestLog.isEmpty() && now - requestLog.peek() >= windowSizeMs)
                requestLog.poll();
            if(requestLog.size() < maxRequests){
                requestLog.offer(now);
                return true;
            }
            return false;
        }
    }
}
public class RateLimiterDemo {
}
