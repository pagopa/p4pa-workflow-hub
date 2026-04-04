package it.gov.pagopa.pu.common.kafka.config.tracing;

import io.micrometer.observation.ObservationRegistry;
import it.gov.pagopa.payhub.activities.performancelogger.PerformanceLogger;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.function.context.catalog.ObservationFunctionAroundWrapperExt;
import org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry;
import org.springframework.cloud.function.observability.FunctionObservationConvention;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Extension of {@link ObservationFunctionAroundWrapperExt} used to performance log kafka messages
 */
@Service
public class KafkaPerformanceLogger extends ObservationFunctionAroundWrapperExt {

  private static final String UNKNOWN = "UNKNOWN";

  public KafkaPerformanceLogger(ObservationRegistry registry,
                                ObjectProvider<FunctionObservationConvention> functionObservationConvention) {
    super(registry, functionObservationConvention);
  }

  @Override
  protected Object doApply(Object messageObj, SimpleFunctionRegistry.FunctionInvocationWrapper targetFunction) {
    if (messageObj instanceof Message<?> message && targetFunction.isConsumer()) {
      String contextData = "%s][%s".formatted(
        getTopicDetails(message),
        getMessageDetails(message)
      );
      int deliveryAttempt = getDeliveryAttempt(message);
      if(deliveryAttempt > 1){
        contextData += "][ATTEMPT=" + deliveryAttempt;
      }

      String[] traceIdHolder = new String[1];
      Message<Object> messageWrapper = new Message<>() {
        @Override
        public Object getPayload() {
          traceIdHolder[0] = Utilities.getTraceId();
          return message.getPayload();
        }

        @Override
        public MessageHeaders getHeaders() {
          return message.getHeaders();
        }
      };

      //noinspection EmptyFinallyBlock
      try {
        return PerformanceLogger.execute(
          "INCOMING_EVENT",
          contextData,
          () -> {
            try {
              return super.doApply(messageWrapper, targetFunction);
            }finally {
              MDC.put("traceId", traceIdHolder[0]);
            }
          },
          null,
          null);
      } finally {
        // traceId not removed in order to make it available to ErrorHandler
      }
    } else {
      return super.doApply(messageObj, targetFunction);
    }
  }

  private int getDeliveryAttempt(Message<?> message) {
    AtomicInteger deliveryAttempt = message.getHeaders().get(IntegrationMessageHeaderAccessor.DELIVERY_ATTEMPT, AtomicInteger.class);
    return deliveryAttempt != null ? deliveryAttempt.get() : 0;
  }

  static String getTopicDetails(Message<?> message) {
    return message.getHeaders().getOrDefault(KafkaHeaders.RECEIVED_TOPIC, UNKNOWN).toString();
  }

  static String getMessageDetails(Message<?> message) {
    return "partition: %s offset: %s".formatted(
      message.getHeaders().getOrDefault(KafkaHeaders.RECEIVED_PARTITION, UNKNOWN),
      message.getHeaders().getOrDefault(KafkaHeaders.OFFSET, UNKNOWN)
    );
  }
}
