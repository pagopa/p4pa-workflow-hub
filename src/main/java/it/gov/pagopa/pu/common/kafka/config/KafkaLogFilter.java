package it.gov.pagopa.pu.common.kafka.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class KafkaLogFilter extends Filter<ILoggingEvent> {
  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (Thread.currentThread().getName().startsWith("pool-") && event.getMessage().contains("Not updating high watermark for partition")) {
      return FilterReply.DENY;
    } else {
      return FilterReply.NEUTRAL;
    }
  }
}
