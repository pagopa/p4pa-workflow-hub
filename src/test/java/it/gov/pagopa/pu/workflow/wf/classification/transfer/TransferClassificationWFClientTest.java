package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  private TransferClassificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new TransferClassificationWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, workflowStubMock);
  }

  @Test
  void testSignalMethodsExist() {
    TemporalTestUtils.assertSignalMethodExists(TransferClassificationWF.class,
        TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION, TransferClassificationStartSignalDTO.class);
  }

  @Test
  void whenClassifyThenOk() {
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(String.format("%s-%d-%s-%s-%d", "TransferClassificationWF", ORGANIZATION, IUV, IUR, INDEX), "RUNID");
    TransferClassificationStartSignalDTO signalDTO = new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX);

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(TransferClassificationWFImpl.TASK_QUEUE_TRANSFER_CLASSIFICATION_WF, expectedResult.getWorkflowId()))
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
    assertThrows(WorkflowInternalErrorException.class,
      () -> client.startTransferClassification(transferClassificationStartSignalDTO),
      "The ID or the workflow must not be null");
  }
}
