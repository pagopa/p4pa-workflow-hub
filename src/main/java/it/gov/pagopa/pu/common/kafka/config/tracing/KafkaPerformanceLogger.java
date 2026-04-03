package it.gov.pagopa.pu.common.kafka.config.tracing;

import io.micrometer.observation.ObservationRegistry;
import it.gov.pagopa.payhub.activities.performancelogger.PerformanceLogger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.function.context.catalog.ObservationFunctionAroundWrapperExt;
import org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry;
import org.springframework.cloud.function.observability.FunctionObservationConvention;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
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
    if (messageObj instanceof Message<?> message) {
      String contextData = "%s][%s".formatted(
        getTopicDetails(message),
        getMessageDetails(message)
      );
      int deliveryAttempt = getDeliveryAttempt(message);
      if(deliveryAttempt > 1){
        contextData += "][ATTEMPT=" + deliveryAttempt;
      }
      return PerformanceLogger.execute(
        "INCOMING_EVENT",
        contextData,
        () -> super.doApply(message, targetFunction),
        null,
        null);
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
