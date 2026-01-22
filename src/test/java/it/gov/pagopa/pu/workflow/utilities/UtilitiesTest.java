package it.gov.pagopa.pu.workflow.utilities;

import com.google.protobuf.Timestamp;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.*;

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

  private static void testGenerateWorkflowIdWhenNullErrors(Long id, Class<?> workflow) {
    WorkflowInternalErrorException exception = assertThrows(
      WorkflowInternalErrorException.class,
      () -> Utilities.generateWorkflowId(id, workflow)
    );

    assertEquals("[INVALID_WORKFLOW_ID] The ID or the workflow must not be null", exception.getMessage());
  }

  @Test
  void givenNormalExceptionWhenGetWorkflowExceptionMessageThenReturnItsMessage(){
    // Given
    String expectedResult = "DUMMY";
    RuntimeException exception = new RuntimeException(expectedResult);

    // When
    String result = Utilities.getWorkflowExceptionMessage(exception);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenActivityExceptionHavingNormalExceptionWhenGetWorkflowExceptionMessageThenReturnItsMessage(){
    // Given
    RuntimeException cause = new RuntimeException("DUMMY");
    RuntimeException exception = new ActivityFailure("X", 0, 0, "", "", null, "", cause);

    // When
    String result = Utilities.getWorkflowExceptionMessage(exception);

    // Then
    Assertions.assertEquals("Activity with activityType='' failed: 'X'. scheduledEventId=0, startedEventId=0, activityId=, identity='', retryState=null", result);
  }

  @Test
  void givenActivityExceptionHavingApplicationFailureWhenGetWorkflowExceptionMessageThenReturnItsMessage(){
    // Given
    RuntimeException exception = new ActivityFailure("X", 0, 0, "", "", null, "", ApplicationFailure.newFailure("DUMMY","Y"));

    // When
    String result = Utilities.getWorkflowExceptionMessage(exception);

    // Then
    Assertions.assertEquals("DUMMY", result);
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
  void givenInstantToOffsetDateTimeThenSuccess(){
    Instant instant = Instant.parse("2025-01-09T10:30:00Z");
    OffsetDateTime expectedOffsetDateTime = OffsetDateTime.of(2025, 1, 9, 10, 30, 0, 0, ZoneOffset.UTC)
      .atZoneSameInstant(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID)
      .toOffsetDateTime();

    OffsetDateTime result = Utilities.instantToOffsetDateTime(instant);
    assertEquals(expectedOffsetDateTime, result);

  }

  @Test
  void givenInstantToOffsetDateTimeWhenNullThenSuccess() {
    assertNull(Utilities.instantToOffsetDateTime(null));
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

  @Test
  void givenEmptyTimeStampWhenProtobufTimestamp2OffsetDateTimeThenNull(){
    Assertions.assertNull(Utilities.protobufTimestamp2OffsetDateTime(Timestamp.getDefaultInstance()));
  }

  @Test
  void whenProtobufTimestamp2OffsetDateTimeThenReturnConversion(){
    // Given
    OffsetDateTime now = OffsetDateTime.now(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID);
    Timestamp ts = Timestamp.newBuilder()
      .setSeconds(now.toEpochSecond())
      .setNanos(now.getNano())
      .build();

    // When
    OffsetDateTime result = Utilities.protobufTimestamp2OffsetDateTime(ts);

    // Then
    Assertions.assertEquals(now, result);
  }

  @Test
  void whenProtobufDuration2DurationThenReturnConversion(){
    // Given
    Duration expectedResult = Duration.ofMillis(537L);
    com.google.protobuf.Duration d = com.google.protobuf.Duration.newBuilder()
      .setSeconds(expectedResult.getSeconds())
      .setNanos(expectedResult.getNano())
      .build();

    // When
    Duration result = Utilities.protobufDuration2Duration(d);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void whenExtractIudsThenVerifyPresents() {
    String str1 = "IUD: id1, id2, id3; CODE: code1, code2;";
    String str2 = "Hello world. IUD: id4, id5 ; CODE: c1;";
    String str3 = "Hello. CODE: c2; IUD: id6, id7, id8  ;";
    String str4 = "IUD: id1, id2, id3 ";

    Set<String> expected1 = Set.of("id1", "id2", "id3");
    Set<String> expected2 = Set.of("id4", "id5");
    Set<String> expected3 = Set.of("id6", "id7", "id8");
    Set<String> expected4 = Set.of("id1", "id2", "id3");

    assertEquals(expected1, Utilities.extractIudsFromDescription(str1));
    assertEquals(expected2, Utilities.extractIudsFromDescription(str2));
    assertEquals(expected3, Utilities.extractIudsFromDescription(str3));
    assertEquals(expected4, Utilities.extractIudsFromDescription(str4));
  }

  @Test
  void whenExtractIudsThenVerifyAbsent() {
    String str1 = "Hello there. IUD code1, code2";
    String str2 = "Completely unrelated text.";
    String str3 = "";
    String str4 = null;

    assertTrue(Utilities.extractIudsFromDescription(str1).isEmpty());
    assertTrue(Utilities.extractIudsFromDescription(str2).isEmpty());
    assertTrue(Utilities.extractIudsFromDescription(str3).isEmpty());
    assertTrue(Utilities.extractIudsFromDescription(str4).isEmpty());
  }
}
