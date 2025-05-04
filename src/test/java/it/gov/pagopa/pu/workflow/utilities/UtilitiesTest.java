package it.gov.pagopa.pu.workflow.utilities;

import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UtilitiesTest {

  @Test
  void whenGenerateWorkflowIdThenOk(){
    String workflowId = Utilities.generateWorkflowId(1L, Utilities.class);

    assertEquals("Utilities-1", workflowId);
  }

  @Test
  void givenGenerateWorkflowIdWhenIdNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(null, Utilities.class);
  }

  @Test
  void givenGenerateWorkflowIdWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(1L, null);
  }

  @Test
  void givenOffsetDateTimeToInstantThenSuccess(){
    OffsetDateTime offsetDateTime = OffsetDateTime.of(2025, 1, 9, 10, 30, 0, 0, ZoneOffset.UTC);
    Instant expectedInstant = Instant.parse("2025-01-09T10:30:00Z");

    Instant result = Utilities.offsetDateTimeToInstant(offsetDateTime);
    assertEquals(expectedInstant, result);

  }

  @Test
  void givenOffsetDateTimeToInstantWhenNullThenSuccess() {
    assertNull(Utilities.offsetDateTimeToInstant(null));
  }

  @Test
  void givenOffsetDateTimeToLocalDateTimeThenSuccess() {
    OffsetDateTime offsetDateTime = OffsetDateTime.of(2025, 1, 9, 10, 30, 0, 0, ZoneOffset.UTC);
    LocalDateTime expectedLocalDateTime = LocalDateTime.of(2025, 1, 9, 10, 30, 0);

    LocalDateTime result = Utilities.offsetDateTimeToLocalDateTime(offsetDateTime);
    assertEquals(expectedLocalDateTime, result);

    result = null;
    assertNull(result);
  }

  @Test
  void givenOffsetDateTimeToLocalDateTimeWhenNullThenSuccess() {
    assertNull(Utilities.offsetDateTimeToLocalDateTime(null));
  }

  private static void testGenerateWorkflowIdWhenNullErrors(Long id, Class<?> workflow) {
    WorkflowInternalErrorException exception = assertThrows(
      WorkflowInternalErrorException.class,
      () -> Utilities.generateWorkflowId(id, workflow)
    );

    assertEquals("The ID or the workflow must not be null", exception.getMessage());
  }

  @Test
  void whenGenerateWorkflowStringIdThenOk(){
    String workflowId = Utilities.generateWorkflowId("00000020f51bb4362eee2a4d", Utilities.class);

    assertEquals("Utilities-00000020f51bb4362eee2a4d", workflowId);
  }

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }
}
