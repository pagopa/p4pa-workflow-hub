package it.gov.pagopa.pu.workflow.mapper;

import io.temporal.client.schedules.ScheduleActionExecution;
import io.temporal.client.schedules.ScheduleActionExecutionStartWorkflow;
import io.temporal.client.schedules.ScheduleActionResult;
import io.temporal.client.schedules.ScheduleInfo;
import it.gov.pagopa.pu.workflow.dto.generated.RecentScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleExecutionInfoDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

class ScheduleInfoDTOMapperTest {

  private final ScheduleInfoDTOMapper mapper = new ScheduleInfoDTOMapper();

  @Test
  void whenMapThenReturnDTO(){
    // Given
    Instant createdAt = Instant.now().minusSeconds(10);
    Instant lastUpdateAt = Instant.now().minusSeconds(1);
    Instant scheduledAt1 = Instant.now();
    Instant startedAt1 = Instant.now().plusSeconds(1);
    Instant scheduledAt2 = Instant.now().plusSeconds(10);
    Instant startedAt2 = Instant.now().plusSeconds(100);
    Instant nextSchedule = Instant.now().plusSeconds(1000);

    ScheduleEnum scheduleId = ScheduleEnum.PAYMENTS_REPORTING_PAGOPA_BROKERS_FETCH;
    ScheduleInfo scheduleInfo = new ScheduleInfo(
      1L,
      2L,
      3L,
      List.of(
        new ScheduleActionExecutionStartWorkflow("WFID1", "RUNID1"),
        Mockito.mock(ScheduleActionExecution.class)),
      List.of(
        new ScheduleActionResult(scheduledAt1, startedAt1, new ScheduleActionExecutionStartWorkflow("WFID2", "RUNID2")),
        new ScheduleActionResult(scheduledAt2, startedAt2, null)),
      List.of(nextSchedule),
      createdAt,
      lastUpdateAt
    );

    ScheduleInfoDTO expectedResult = ScheduleInfoDTO.builder()
      .scheduleId(scheduleId)
      .numActions(1L)
      .numActionsMissedCatchupWindow(2L)
      .numActionsSkippedOverlap(3L)
      .runningActions(List.of(
        ScheduleExecutionInfoDTO.builder().workflowId("WFID1").runId("RUNID1").build()))
      .recentActions(List.of(
        RecentScheduleExecutionInfoDTO.builder().scheduledAt(Utilities.instantToOffsetDateTime(scheduledAt1)).startedAt(Utilities.instantToOffsetDateTime(startedAt1)).workflowId("WFID2").runId("RUNID2").build(),
        RecentScheduleExecutionInfoDTO.builder().scheduledAt(Utilities.instantToOffsetDateTime(scheduledAt2)).startedAt(Utilities.instantToOffsetDateTime(startedAt2)).build()))
      .nextActionTimes(List.of(Utilities.instantToOffsetDateTime(nextSchedule)))
      .createdAt(Utilities.instantToOffsetDateTime(createdAt))
      .lastUpdatedAt(Utilities.instantToOffsetDateTime(lastUpdateAt))
      .build();

    // When
    ScheduleInfoDTO result = mapper.map(scheduleId, scheduleInfo);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
