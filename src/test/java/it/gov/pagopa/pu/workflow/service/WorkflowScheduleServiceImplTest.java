package it.gov.pagopa.pu.workflow.service;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.OrganizationsBrokeredRetrieveWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch.OrganizationsBrokeredRetrieveWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowScheduleServiceImplTest {
  @Mock
  private ScheduleClient scheduleClientMock;

  private WorkflowScheduleService workflowScheduleService;

  @BeforeEach
  void setUp() {
    workflowScheduleService = new WorkflowScheduleServiceImpl(scheduleClientMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(scheduleClientMock);
  }

  @Test
  void buildSchedule() {
    // Given
    Class<?> workflowInterface = OrganizationsBrokeredRetrieveWF.class;
    String taskQueue = OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE;
    String workflowId = "test-workflow-id";
    String scheduleId = "test-schedule-id";
    Duration startEvery = Duration.ofSeconds(10);
    ScheduleHandle expectedHandle = mock(ScheduleHandle.class);
    Schedule schedule = prepareSchedule(workflowInterface, taskQueue, workflowId, startEvery);

    lenient().when(scheduleClientMock.createSchedule(
      eq(scheduleId),
      argThat(arg -> schedule.getAction().equals(arg.getAction()) && schedule.getSpec().equals(arg.getSpec())),
      argThat(options -> ScheduleOptions.newBuilder().build().equals(options))
    ))
    .thenReturn(expectedHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.buildSchedule(
      workflowInterface, taskQueue, workflowId, scheduleId, startEvery);

    // Then
    assertSame(expectedHandle, actualHandle);
  }

  @Test
  void getSchedule() {
    // Given
    String scheduleId = "test-schedule-id";
    ScheduleHandle expectedHandle = mock(ScheduleHandle.class);

    lenient().when(scheduleClientMock.getHandle(scheduleId))
      .thenReturn(expectedHandle);

    // When
    ScheduleHandle actualHandle = workflowScheduleService.getSchedule(scheduleId);

    // Then
    assertSame(expectedHandle, actualHandle);
  }

  private Schedule prepareSchedule(Class<?> workflowInterface, String taskQueue, String workflowId, Duration startEvery) {
    WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
      .setWorkflowId(workflowId)
      .setTaskQueue(taskQueue)
      .build();

    ScheduleSpec scheduleSpec = ScheduleSpec.newBuilder()
      .setIntervals(
        Collections.singletonList(new ScheduleIntervalSpec(startEvery)))
      .build();

    return Schedule.newBuilder()
      .setAction(ScheduleActionStartWorkflow.newBuilder()
        .setWorkflowType(workflowInterface)
        .setOptions(workflowOptions)
        .build())
      .setSpec(scheduleSpec)
      .build();
  }
}
