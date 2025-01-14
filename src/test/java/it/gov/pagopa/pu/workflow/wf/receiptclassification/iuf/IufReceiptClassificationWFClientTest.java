package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForTreasurySignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class IufReceiptClassificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private WorkflowExecution workflowExecutionMock;

  private IufReceiptClassificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new IufReceiptClassificationWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void testClassifyForTreasury() {
    // Given
    Long organizationId = 1L;
    String treasuryId = "2T";
    String iuf = "iuf123";
    String expectedWorkflowId = "IufReceiptClassificationWF-1-2T-iuf123";

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String workflowId = client.classifyForTreasury(organizationId, treasuryId, iuf);

    // Then
    assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(workflowServiceMock).buildUntypedWorkflowStub(any(), eq(expectedWorkflowId));
    Mockito.verify(workflowStubMock).signalWithStart(
      eq(IufReceiptClassificationForTreasurySignalDTO.SIGNAL_METHOD_NAME),
      any(Object[].class),
      any(Object[].class)
    );
  }

  @Test
  void testClassifyForReporting() {
    // Given
    Long organizationId = 1L;
    String iuf = "iuf123";
    String outcomeCode = "outcome123";
    List<Transfer2ClassifyDTO> transfers2classify = Collections.emptyList();
    String expectedWorkflowId = "IufReceiptClassificationWF-1-outcome123-iuf123-" + transfers2classify.hashCode();

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String workflowId = client.classifyForReporting(organizationId, iuf, outcomeCode, transfers2classify);

    // Then
    assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(workflowServiceMock).buildUntypedWorkflowStub(any(), eq(expectedWorkflowId));
    Mockito.verify(workflowStubMock).signalWithStart(
      eq(IufReceiptClassificationForReportingSignalDTO.SIGNAL_METHOD_NAME),
      any(Object[].class),
      any(Object[].class)
    );
  }
}
