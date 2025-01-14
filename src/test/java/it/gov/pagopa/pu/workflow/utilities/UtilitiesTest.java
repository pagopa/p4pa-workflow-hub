package it.gov.pagopa.pu.workflow.utilities;

import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UtilitiesTest {

  @Test
  void whenGenerateWorkflowIdThenOk(){
    String workflowId = Utilities.generateWorkflowId(1L, "workflow");

    assertEquals("workflow-1", workflowId);
  }

  @Test
  void givenGenerateWorkflowIdWhenIdNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(null, "workflow");
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
    Instant result = Utilities.offsetDateTimeToInstant(null);
    assertNull(result);
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
    LocalDateTime result = Utilities.offsetDateTimeToLocalDateTime(null);
    assertNull(result);
  }

  private static void testGenerateWorkflowIdWhenNullErrors(Long id, String workflow) {
    WorkflowInternalErrorException exception = assertThrows(
      WorkflowInternalErrorException.class,
      () -> Utilities.generateWorkflowId(id, workflow)
    );

    assertEquals("The ID or the workflow must not be null", exception.getMessage());
  }
}
