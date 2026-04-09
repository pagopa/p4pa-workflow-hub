package it.gov.pagopa.pu.workflow.wf.debtposition.massive;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.dto.MassiveIbanUpdateToSyncSignalDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate.MassiveIbanUpdateWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate.MassiveIbanUpdateWFImpl;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdatetosync.MassiveIbanUpdateToSyncWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MassiveDebtPositionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private MassiveIbanUpdateWF massiveIbanUpdateWFMock;
  @Mock
  private MassiveIbanUpdateToSyncWF massiveIbanUpdateWFToSyncMock;

  private MassiveDebtPositionWFClient client;

  @BeforeEach
  void init() {
    client = new MassiveDebtPositionWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void givenOrgIdAndIbansWhenStartMassiveIbanUpdateThenOk() {
    // Given
    Long orgId = 1L;
    Long dptoId = 2L;
    String oldIban = "IT60X0542811101000000123456";
    String newIban = "IT60X0542811101000000654321";
    String oldPostalIban = "IT60X0760111100000000123456";
    String newPostalIban = "IT60X0760111100000000654321";

    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("MassiveIbanUpdateWF-" + orgId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(MassiveIbanUpdateWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(massiveIbanUpdateWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult,
      orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

    // When
    WorkflowCreatedDTO result = client.startMassiveIbanUpdate(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

    // Then
    assertEquals(expectedResult, result);
    Mockito.verify(massiveIbanUpdateWFMock).massiveIbanUpdate(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, MassiveIbanUpdateWFImpl.class);
  }

  @Test
  void givenMassiveIbanUpdateToSyncSignalDTOWhenScheduleMassiveIbanUpdateToSyncThenOk() {
    // Given
    Long orgId = 1L;
    Long dptoId = 1L;
    String oldIban = "oldIban";
    String newIban = "newIban";
    String oldPostalIban = "oldPostalIban";
    String newPostalIban = "newPostalIban";

    MassiveIbanUpdateToSyncSignalDTO signalDTO = MassiveIbanUpdateToSyncSignalDTO.builder()
      .orgId(orgId)
      .dptoId(dptoId)
      .oldIban(oldIban)
      .newIban(newIban)
      .oldPostalIban(oldPostalIban)
      .newPostalIban(newPostalIban)
      .build();

    Duration scheduleDuration = Duration.ofMinutes(5);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("MassiveIbanUpdateToSyncWF-" + orgId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubDelayed(
        MassiveIbanUpdateToSyncWF.class,
        TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY,
        expectedResult.getWorkflowId(),
        scheduleDuration))
      .thenReturn(massiveIbanUpdateWFToSyncMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult,
      orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

    // When
    WorkflowCreatedDTO result = client.scheduleMassiveIbanUpdateToSync(signalDTO, scheduleDuration);

    // Then
    assertEquals(expectedResult, result);
  }
}
