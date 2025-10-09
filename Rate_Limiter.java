import java.util.ArrayDeque;
import java.util.Deque;


interface RateLimitingStrategy {
    boolean allowRequest();
}


class SlidingWindowRateLimiter implements RateLimitingStrategy {
    private final int limit;
    private final long windowSizeInMillis;
    private final Deque<Long> timestamps;

    public SlidingWindowRateLimiter(int limit, long windowSizeInSeconds) {
        this.limit = limit;
        this.windowSizeInMillis = windowSizeInSeconds * 1000;
        this.timestamps = new ArrayDeque<>();
    }

    @Override
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowSizeInMillis) {
            timestamps.pollFirst();
        }
        if (timestamps.size() < limit) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}


class TokenBucketRateLimiter implements RateLimitingStrategy {
    private final int capacity;
    private int tokens;
    private final long refillIntervalInMillis;
    private long lastRefillTimestamp;

    public TokenBucketRateLimiter(int capacity, long refillIntervalInSeconds) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.refillIntervalInMillis = refillIntervalInSeconds * 1000;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean allowRequest() {
        refillTokens();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refillTokens() {
        long now = System.currentTimeMillis();
        long intervalsPassed = (now - lastRefillTimestamp) / refillIntervalInMillis;
        if (intervalsPassed > 0) {
            tokens = Math.min(capacity, tokens + (int) intervalsPassed);
            lastRefillTimestamp = now;
        }
    }
}


class RateLimiterContext {
    private RateLimitingStrategy strategy;

    public RateLimiterContext(RateLimitingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(RateLimitingStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean allowRequest() {
        return strategy.allowRequest();
    }
}


public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Using Sliding Window Rate Limiter:");
        RateLimiterContext limiter = new RateLimiterContext(new SlidingWindowRateLimiter(3, 5));
        for (int i = 1; i <= 6; i++) {
            System.out.printf("Request %d: %b%n", i, limiter.allowRequest());
            Thread.sleep(1000);
        }

        System.out.println("\nSwitching to Token Bucket Rate Limiter:");
        limiter.setStrategy(new TokenBucketRateLimiter(3, 2)); 
        for (int i = 1; i <= 10; i++) {
            System.out.printf("Request %d: %b%n", i, limiter.allowRequest());
            Thread.sleep(500);
        }
    }
}
