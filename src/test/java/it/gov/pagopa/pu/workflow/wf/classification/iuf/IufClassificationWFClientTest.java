package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class IufClassificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;

  private IufClassificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new IufClassificationWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, workflowStubMock);
  }

  @Test
  void testSignalMethodsExist() {
    TemporalTestUtils.assertSignalMethodExists(IufClassificationWF.class,
      IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY, IufClassificationNotifyTreasurySignalDTO.class);

    TemporalTestUtils.assertSignalMethodExists(IufClassificationWF.class,
        IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING, IufClassificationNotifyPaymentsReportingSignalDTO.class);
  }

  @Test
  void testClassifyForTreasury() {
    // Given
    IufClassificationNotifyTreasurySignalDTO signalDTO = IufClassificationNotifyTreasurySignalDTO.builder()
      .organizationId(1L)
      .iuf("iuf123")
      .treasuryId("2T")
      .build();

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IufClassificationWF-1-iuf123", "RUNID");

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowClientServiceMock.signalWithStart(
        same(workflowStubMock),
        eq(IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY),
        argThat(o -> o[0] == signalDTO),
        argThat(o -> o.length == 0)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.notifyTreasury(signalDTO);

    // Then
    assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, IufClassificationWFImpl.class);
  }

  @Test
  void testNotifyPaymentsReporting() {
    // Given
    IufClassificationNotifyPaymentsReportingSignalDTO signalDTO = IufClassificationNotifyPaymentsReportingSignalDTO.builder()
      .iuf("iuf123")
      .organizationId(1L)
      .transfers(List.of(PaymentsReportingTransferDTO.builder()
        .iur("iur")
        .iuv("iuv")
        .transferIndex(1)
        .orgId(1L)
        .paymentOutcomeCode("CODICEESITO")
        .build()))
      .build();

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IufClassificationWF-1-iuf123", "RUNID");

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowClientServiceMock.signalWithStart(
        same(workflowStubMock),
        eq(IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING),
        argThat(o -> o[0] == signalDTO),
        argThat(o -> o.length == 0)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.notifyPaymentsReporting(signalDTO);

    // Then
    assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, IufClassificationWFImpl.class);
  }
}
