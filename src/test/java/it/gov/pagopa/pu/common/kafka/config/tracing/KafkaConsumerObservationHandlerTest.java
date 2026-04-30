package it.gov.pagopa.pu.common.kafka.config.tracing;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelTraceContextBuilder;
import io.opentelemetry.sdk.trace.IdGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;
import org.springframework.cloud.function.observability.FunctionContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerObservationHandlerTest {

  @Mock
  private TracingProperties tracingPropertiesMock;
  @Mock
  private Tracer tracerMock;
  @Mock
  private IdGenerator idGeneratorMock;


  private KafkaConsumerObservationHandler handler;

  @BeforeEach
  void init() {
    TracingProperties.Baggage baggageMock = Mockito.mock(TracingProperties.Baggage.class);
    TracingProperties.Baggage.Correlation correlationMock = Mockito.mock(TracingProperties.Baggage.Correlation.class);

    Mockito.when(tracingPropertiesMock.getBaggage())
      .thenReturn(baggageMock);
    Mockito.when(baggageMock.getCorrelation())
      .thenReturn(correlationMock);
    Mockito.when(correlationMock.getFields())
      .thenReturn(List.of("propagatedField"));

    try (MockedStatic<IdGenerator> idGeneratorMockedStatic = Mockito.mockStatic(IdGenerator.class)) {
      idGeneratorMockedStatic.when(IdGenerator::random)
        .thenReturn(idGeneratorMock);

      handler = new KafkaConsumerObservationHandler(tracingPropertiesMock, tracerMock);
    }
  }

  @Test
  void givenMicrometerSpanWhenSupportsContextThenDoNothing() {
    // Given
    io.micrometer.tracing.Span micrometerSpanMock = Mockito.mock(io.micrometer.tracing.Span.class);

    Mockito.when(tracerMock.currentSpan())
      .thenReturn(micrometerSpanMock);

    // When
    boolean result = handler.supportsContext(null);

    // Then
    Assertions.assertFalse(result);
  }

  @Test
  void givenUnsupportedContextWhenSupportsContextThenDoNothing() {
    // Given no context configured

    // When
    boolean result = handler.supportsContext(null);

    // Then
    Assertions.assertFalse(result);
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void givenTracingInfoWhenSupportsContextThenGenerateRandom() {
    // Given
    String traceId = "TRACEID";
    String parentSpanId = "PARENTSPANID";
    String spanId = "SPANID";

    FunctionContext functionContextMock = Mockito.mock(FunctionContext.class);
    Message message = new GenericMessage<>("BODY", Map.of(
      "traceparent", "00-%s-%s-00".formatted(traceId, parentSpanId),
      "propagatedField", "propagatedValue"
    ));
    boolean propagatedFieldConfigured = true;

    io.micrometer.tracing.Span.Builder micrometerSpanBuilderMock = Mockito.mock(io.micrometer.tracing.Span.Builder.class);
    io.micrometer.tracing.Span injectedMicrometerSpan = Mockito.mock(io.micrometer.tracing.Span.class);

    configureMicrometerMocks(functionContextMock, message, spanId, micrometerSpanBuilderMock, parentSpanId, traceId, injectedMicrometerSpan, propagatedFieldConfigured);

    Mockito.when(idGeneratorMock.generateSpanId())
      .thenReturn(spanId);

    // When
    boolean result = handler.supportsContext(functionContextMock);

    // Then
    Assertions.assertFalse(result);

    verifyMicrometerMocks(functionContextMock, injectedMicrometerSpan, micrometerSpanBuilderMock);
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void givenTracingInfoUnexpectedStructureWhenSupportsContextThenGenerateRandom() {
    // Given
    String traceId = "TRACEID";
    String parentSpanId = null;
    String spanId = "SPANID";

    FunctionContext functionContextMock = Mockito.mock(FunctionContext.class);
    Message message = new GenericMessage<>("BODY", Map.of(
      "traceparent", "UNEXPECTEDVALUE",
      "propagatedField", "propagatedValue"
    ));
    boolean propagatedFieldConfigured = true;

    io.micrometer.tracing.Span.Builder micrometerSpanBuilderMock = Mockito.mock(io.micrometer.tracing.Span.Builder.class);
    io.micrometer.tracing.Span injectedMicrometerSpan = Mockito.mock(io.micrometer.tracing.Span.class);

    configureMicrometerMocks(functionContextMock, message, spanId, micrometerSpanBuilderMock, parentSpanId, traceId, injectedMicrometerSpan, propagatedFieldConfigured);

    Mockito.when(idGeneratorMock.generateSpanId())
      .thenReturn(spanId);

    Mockito.when(idGeneratorMock.generateTraceId())
      .thenReturn(traceId);

    // When
    boolean result = handler.supportsContext(functionContextMock);

    // Then
    Assertions.assertFalse(result);

    verifyMicrometerMocks(functionContextMock, injectedMicrometerSpan, micrometerSpanBuilderMock);
  }

  @SuppressWarnings({"rawtypes"})
  @Test
  void givenNoTracingInfoWhenSupportsContextThenGenerateRandom() {
    // Given
    String traceId = "TRACEID";
    String parentSpanId = null;
    String spanId = "SPANID";

    FunctionContext functionContextMock = Mockito.mock(FunctionContext.class);
    Message message = new GenericMessage<>("BODY", Map.of());
    boolean propagatedFieldConfigured = false;

    io.micrometer.tracing.Span.Builder micrometerSpanBuilderMock = Mockito.mock(io.micrometer.tracing.Span.Builder.class);
    io.micrometer.tracing.Span injectedMicrometerSpan = Mockito.mock(io.micrometer.tracing.Span.class);

    configureMicrometerMocks(functionContextMock, message, spanId, micrometerSpanBuilderMock, parentSpanId, traceId, injectedMicrometerSpan, propagatedFieldConfigured);

    Mockito.when(idGeneratorMock.generateSpanId())
      .thenReturn(spanId);

    Mockito.when(idGeneratorMock.generateTraceId())
      .thenReturn(traceId);

    // When
    boolean result = handler.supportsContext(functionContextMock);

    // Then
    Assertions.assertFalse(result);

    verifyMicrometerMocks(functionContextMock, injectedMicrometerSpan, micrometerSpanBuilderMock);
  }

  @SuppressWarnings("unchecked")
  private void configureMicrometerMocks(FunctionContext functionContextMock, Message message, String spanId, io.micrometer.tracing.Span.Builder micrometerSpanBuilderMock, String parentSpanId, String traceId, io.micrometer.tracing.Span injectedMicrometerSpan, boolean propagatedFieldConfigured) {
    Mockito.when(functionContextMock.getMessage())
      .thenReturn(message);

    Mockito.when(tracerMock.spanBuilder())
      .thenReturn(micrometerSpanBuilderMock);
    Mockito.when(micrometerSpanBuilderMock.setParent(
        new OtelTraceContextBuilder()
          .parentId(parentSpanId)
          .traceId(traceId)
          .spanId(spanId)
          .sampled(false)
          .build()
      ))
      .thenReturn(micrometerSpanBuilderMock);

    if (propagatedFieldConfigured) {
      Mockito.when(micrometerSpanBuilderMock.tag("propagatedField", "propagatedValue"))
        .thenReturn(micrometerSpanBuilderMock);
    }

    Mockito.when(micrometerSpanBuilderMock.start())
      .thenReturn(injectedMicrometerSpan);
  }

  private static void verifyMicrometerMocks(FunctionContext functionContextMock, io.micrometer.tracing.Span injectedMicrometerSpan, io.micrometer.tracing.Span.Builder micrometerSpanBuilderMock) {
    Mockito.verify(functionContextMock)
      .get(TracingObservationHandler.TracingContext.class);

    Mockito.verify(functionContextMock)
      .put(Mockito.eq(TracingObservationHandler.TracingContext.class), Mockito.<TracingObservationHandler.TracingContext>argThat(context ->
        injectedMicrometerSpan == context.getSpan()));

    Mockito.verifyNoMoreInteractions(
      micrometerSpanBuilderMock,
      functionContextMock
    );
  }

}
