package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification.SyncDpIONotificationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification.SyncDpIONotificationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class SyncDpIONotificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private SyncDpIONotificationWF syncDpIONotificationWFMock;

  private SyncDpIONotificationWFClient client;

  @BeforeEach
  void init() {
    client = new SyncDpIONotificationWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenSendSendIoNotificationThenSuccess() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap = new HashMap<>();
    iudSyncCompleteDTOMap.put("SYNC_IUD", SyncCompleteDTO.builder().newStatus(InstallmentStatus.UNPAID).build());
    GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessage =
      new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;

    String fixedDateTime = "2026-01-21T16:30:00";
    LocalDateTime fixedNow = LocalDateTime.parse(fixedDateTime);
    try (MockedStatic<LocalDateTime> mockedDateTime = Mockito.mockStatic(LocalDateTime.class)) {
      mockedDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

      String expectedWorkflowId = String.format("IoNotificationWF-%s-%s",
        debtPositionDTO.getDebtPositionId(), fixedDateTime);

      WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(expectedWorkflowId, "runId");

      Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
          SyncDpIONotificationWF.class,
          taskQueue,
          expectedResult.getWorkflowId()))
        .thenReturn(syncDpIONotificationWFMock);

      TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionDTO,
        iudSyncCompleteDTOMap, ioMessage);

      // when
      client.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage);

      // Then
      Mockito.verify(syncDpIONotificationWFMock).sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage);

      TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SyncDpIONotificationWFImpl.class);

    }
  }
}
