package it.gov.pagopa.pu.common.kafka.config;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

class KafkaLogFilterTest {

  private final KafkaLogFilter filter = new KafkaLogFilter();

  @Test
  void test() throws ExecutionException, InterruptedException {
    LoggingEvent filteredLogWhenNotConsumerThread = new LoggingEvent();
    filteredLogWhenNotConsumerThread.setMessage("Not updating high watermark for partition");

    LoggingEvent alwaysAcceptedLog = new LoggingEvent();
    alwaysAcceptedLog.setMessage("ANY OTHER LOG");

    Assertions.assertEquals(FilterReply.NEUTRAL, filter.decide(filteredLogWhenNotConsumerThread));
    Assertions.assertEquals(FilterReply.NEUTRAL, filter.decide(alwaysAcceptedLog));

    try(ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1)) {
      ScheduledFuture<?> schedule = scheduledExecutorService.schedule(
        () -> {
          Assertions.assertEquals(FilterReply.DENY, filter.decide(filteredLogWhenNotConsumerThread));
          Assertions.assertEquals(FilterReply.NEUTRAL, filter.decide(alwaysAcceptedLog));
        },
        1, TimeUnit.MILLISECONDS);

      schedule.get();
    }
  }
}
