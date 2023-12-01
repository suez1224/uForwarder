package com.uber.data.kafka.consumerproxy.worker.limiter;

import com.netflix.concurrency.limits.Limit;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limit.VegasLimit;
import com.netflix.concurrency.limits.limit.WindowedLimit;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import java.util.Optional;

/** Adaptive inflight limiting implemented with Vegas limit */
public class VegasAdaptiveInflightLimiter extends AdaptiveInflightLimiter {
  private static final int CONCURRENCY_LIMITER_INITIAL_LIMIT = 100;

  private SimpleLimiter limiter;
  private AdaptiveMetrics metrics;

  private VegasAdaptiveInflightLimiter(Limit limit) {
    this.limiter = SimpleLimiter.newBuilder().limit(limit).build();
    this.metrics = new AdaptiveMetrics();
  }

  @Override
  Optional<Limiter.Listener> tryAcquireImpl() {
    return limiter.acquire(null);
  }

  @Override
  public InflightLimiter.Metrics getMetrics() {
    return metrics;
  }

  /**
   * Creates builder
   *
   * @return builder of VegasAdaptiveInflightLimiter
   */
  public static AdaptiveInflightLimiter.Builder newBuilder() {
    return () -> {
      Limit vegasLimit =
          VegasLimit.newBuilder().initialLimit(CONCURRENCY_LIMITER_INITIAL_LIMIT).build();
      WindowedLimit limit = WindowedLimit.newBuilder().build(vegasLimit);
      return new VegasAdaptiveInflightLimiter(limit);
    };
  }

  private class AdaptiveMetrics extends AdaptiveInflightLimiter.Metrics {
    @Override
    public long getInflight() {
      return limiter.getInflight();
    }

    @Override
    public long getLimit() {
      return limiter.getLimit();
    }
  }
}
