package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class TransferClassificationWFClientTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUR = "IUR";
  private static final int INDEX = 1;

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private WorkflowExecution workflowExecutionMock;

  private TransferClassificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new TransferClassificationWFClient(workflowServiceMock);
  }

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenClassifyThenOk() {
    // Given
    String expectedWorkflowId = String.format("%s-%d-%s-%s-%d", "TransferClassificationWF", ORGANIZATION, IUV, IUR, INDEX);
    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String actualWorkflowId = client.startTransferClassification(new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX));

    // Then
    assertEquals(expectedWorkflowId, actualWorkflowId);
    Mockito.verify(workflowServiceMock).buildUntypedWorkflowStub(any(), eq(expectedWorkflowId));
    Mockito.verify(workflowStubMock).signalWithStart(
      eq(TransferClassificationWF.SIGNAL_METHOD_NAME_START_TRANSFER_CLASSIFICATION),
      any(Object[].class),
      any(Object[].class)
    );
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
