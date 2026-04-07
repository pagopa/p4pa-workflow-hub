package it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity;

import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate.MassiveIbanUpdateWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class ScheduleToSyncMassiveIbanUpdateWFActivityImplTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private MassiveIbanUpdateWF massiveIbanUpdateWFMock;

  private ScheduleToSyncMassiveIbanUpdateWFActivity scheduleToSyncMassiveIbanUpdateWFActivity;

  private static final int SCHEDULE_MINUTES = 5;

  @BeforeEach
  void setUp() {
    scheduleToSyncMassiveIbanUpdateWFActivity = new ScheduleToSyncMassiveIbanUpdateWFActivityImpl(
      workflowServiceMock,
      workflowClientServiceMock,
      SCHEDULE_MINUTES
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenScheduleToSyncMassiveIbanUpdateWFThenOk() {
    Long orgId = 1L;
    Long dptoId = 1L;
    String oldIban = "oldIban";
    String newIban = "newIban";
    String oldPostalIban = "oldPostalIban";
    String newPostalIban = "newPostalIban";

    Duration scheduleDuration = Duration.ofMinutes(SCHEDULE_MINUTES);

    Mockito.when(workflowServiceMock.buildWorkflowStubDelayed(
        Mockito.eq(MassiveIbanUpdateWF.class),
        Mockito.eq(TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY),
        Mockito.anyString(),
        Mockito.eq(scheduleDuration)))
      .thenReturn(massiveIbanUpdateWFMock);

    scheduleToSyncMassiveIbanUpdateWFActivity.scheduleToSyncMassiveIbanUpdateWF(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

    Mockito.verify(workflowServiceMock).buildWorkflowStubDelayed(
      Mockito.eq(MassiveIbanUpdateWF.class),
      Mockito.eq(TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY),
      Mockito.anyString(),
      Mockito.eq(scheduleDuration)
    );

    Mockito.verify(workflowClientServiceMock)
      .start(
        Mockito.any(),
        Mockito.eq(orgId),
        Mockito.eq(dptoId),
        Mockito.eq(oldIban),
        Mockito.eq(newIban),
        Mockito.eq(oldPostalIban),
        Mockito.eq(newPostalIban)
      );
  }
}
