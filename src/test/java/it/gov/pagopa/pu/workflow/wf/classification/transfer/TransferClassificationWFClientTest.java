package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransferClassificationWFClientTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUR = "IUR";
  private static final int INDEX = 1;

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private OrganizationRetrieverService organizationRetrieverServiceMock;

  private TransferClassificationWFClient client;
  private final Class<TransferClassificationWF> wfInterface = TransferClassificationWF.class;

  @BeforeEach
  void setUp() {
    client = new TransferClassificationWFClient(workflowServiceMock, workflowClientServiceMock, organizationRetrieverServiceMock);
  }

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, workflowStubMock, organizationRetrieverServiceMock);
  }

  @Test
  void testSignalMethodsExist() {
    TemporalTestUtils.assertSignalMethodExists(wfInterface,
        TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION, TransferClassificationStartSignalDTO.class);
  }

  @Test
  void whenClassifyThenOk() {
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(String.format("%s-%d-%s-%s-%d", "TransferClassificationWF", ORGANIZATION, IUV, IUR, INDEX), "RUNID");
    TransferClassificationStartSignalDTO signalDTO = new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX);

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(ORGANIZATION)).thenReturn(true);
    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(wfInterface, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowClientServiceMock.signalWithStart(
        same(workflowStubMock),
        eq(TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION),
        argThat(o -> o[0] == signalDTO),
        argThat(o -> o.length == 0)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.startTransferClassification(signalDTO);

    // Then
    assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, TransferClassificationWFImpl.class);
  }

  @Test
  void givenClassificationDisabledWhenClassifyThenError(){
    // Given
    TransferClassificationStartSignalDTO signalDTO = new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX);

    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(ORGANIZATION))
      .thenReturn(false);

    // When
    WorkflowCreatedDTO result = client.startTransferClassification(signalDTO);

    // Then
    assertNull(result);
  }

  @Test
  void givenGenerateWorkflowIdWhenOrgIdNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(null, IUV, IUR, INDEX);
  }

  @Test
  void givenGenerateWorkflowIuvWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(ORGANIZATION, null, IUR, INDEX);
  }

  @Test
  void givenGenerateWorkflowIurWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(ORGANIZATION, IUV, null, INDEX);
  }

  private void testGenerateWorkflowIdWhenNullErrors(Long orgId, String iuv, String iur, int transferIndex) {
    TransferClassificationStartSignalDTO transferClassificationStartSignalDTO = new TransferClassificationStartSignalDTO(orgId, iuv, iur, transferIndex);
    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(orgId)).thenReturn(true);
    assertThrows(WorkflowInternalErrorException.class,
      () -> client.startTransferClassification(transferClassificationStartSignalDTO),
      "The ID or the workflow must not be null");
  }
}
