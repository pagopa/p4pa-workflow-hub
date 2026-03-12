package it.gov.pagopa.pu.common.kafka.config.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelTraceContextBuilder;
import io.opentelemetry.sdk.trace.IdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;
import org.springframework.cloud.function.observability.FunctionContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * An ObservationHandler that creates a Micrometer Tracing Span from the current OpenTracing Span if no Micrometer Span is present in the Observation context.
 */
@Service
public class KafkaConsumerObservationHandler implements ObservationHandler<FunctionContext> {

  private final List<String> correlationFields;

  private final Tracer tracer;
  private final IdGenerator idGenerator;

  public KafkaConsumerObservationHandler(TracingProperties tracingProperties, @Lazy Tracer tracer) {
    this.correlationFields = tracingProperties.getBaggage().getCorrelation().getFields();
    this.tracer = tracer;

    idGenerator = IdGenerator.random();
  }

  @Override
  public boolean supportsContext(Observation.Context context) {
    if(tracer.currentSpan() == null && context instanceof FunctionContext functionContext) {
      Message<?> message = functionContext.getMessage();
      Pair<String, String> traceId2ParentId = extractTracingInfo(message);

      io.micrometer.tracing.Span.Builder otSpanBuilder = tracer.spanBuilder()
        .setParent(new OtelTraceContextBuilder()
          .traceId(traceId2ParentId.getKey())
          .parentId(traceId2ParentId.getValue())
          .spanId(idGenerator.generateSpanId())
          .sampled(false)
          .build());

      message.getHeaders().forEach((name, value) -> {
        if (correlationFields.contains(name)) {
          otSpanBuilder.tag(name, String.valueOf(value));
        }
      });

      if (context.get(TracingObservationHandler.TracingContext.class) == null) {
        context.put(TracingObservationHandler.TracingContext.class, new TracingObservationHandler.TracingContext() {
          @Override
          public io.micrometer.tracing.Span getSpan() {
            return otSpanBuilder.start();
          }
        });
      }
    }
    return false;
  }

  private Pair<String, String> extractTracingInfo(Message<?> message) {
    String traceParent = message.getHeaders().get("traceparent", String.class);

    if(!StringUtils.isEmpty(traceParent)) {
      String[] traceParts = traceParent.split("-");
      if(traceParts.length >= 3) {
        return Pair.of(traceParts[1], traceParts[2]);
      }
    }

    return Pair.of(idGenerator.generateTraceId(), null);
  }
}
