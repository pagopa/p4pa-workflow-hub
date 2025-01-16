package it.gov.pagopa.pu.workflow.wf.classification.transfer;

import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification.TransferClassificationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferClassificationWFClientTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUR = "IUR";
  private static final int INDEX = 1;

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private TransferClassificationWF wfMock;

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

    doReturn(wfMock).when(workflowServiceMock)
      .buildWorkflowStub(TransferClassificationWF.class, TransferClassificationWFImpl.TASK_QUEUE, expectedWorkflowId);

    // When
    String actualWorkflowId = client.classify(ORGANIZATION, IUV, IUR, INDEX);

    // Then
    assertEquals(expectedWorkflowId, actualWorkflowId);
    verify(wfMock).classify(ORGANIZATION, IUV, IUR, INDEX);

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
    assertThrows(WorkflowInternalErrorException.class,
      () -> client.classify(orgId, iuv, iur, transferIndex), "The ID or the workflow must not be null");
  }
}
