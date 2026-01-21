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
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification.IoNotificationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification.IoNotificationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class IoNotificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private IoNotificationWF ioNotificationWFMock;

  private IoNotificationWFClient client;

  @BeforeEach
  void init() {
    client = new IoNotificationWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenSendSendIoNotificationThenSuccess(){
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap = new HashMap<>();
    iudSyncCompleteDTOMap.put("SYNC_IUD", SyncCompleteDTO.builder().newStatus(InstallmentStatus.UNPAID).build());
    GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessage =
      new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IoNotificationWF-1", "runId");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(
        IoNotificationWF.class,
        taskQueue,
        expectedResult.getWorkflowId()))
      .thenReturn(ioNotificationWFMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionDTO,
      iudSyncCompleteDTOMap, ioMessage);

    // when
    client.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage);

    // Then
    Mockito.verify(ioNotificationWFMock).sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, IoNotificationWFImpl.class);

  }


}
