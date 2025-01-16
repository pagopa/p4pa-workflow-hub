package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.classification.IufClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class IufClassificationWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private WorkflowExecution workflowExecutionMock;

  private IufClassificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new IufClassificationWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void testClassifyForTreasury() {
    // Given
    Long organizationId = 1L;
    String treasuryId = "2T";
    String iuf = "iuf123";
    String expectedWorkflowId = "IufClassificationWF-1-iuf123";

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String workflowId = client.notifyTreasury(organizationId, treasuryId, iuf);

    // Then
    assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(workflowServiceMock).buildUntypedWorkflowStub(any(), eq(expectedWorkflowId));
    Mockito.verify(workflowStubMock).signalWithStart(
      eq(IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY),
      any(Object[].class),
      any(Object[].class)
    );
  }

  @Test
  void testNotifyPaymentsReporting() {
    // Given
    Long organizationId = 1L;
    String iuf = "iuf123";
    List<Transfer2ClassifyDTO> transfers2classify = Collections.emptyList();
    String expectedWorkflowId = "IufClassificationWF-1-iuf123";

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String workflowId = client.notifyPaymentsReporting(organizationId, iuf, transfers2classify);

    // Then
    assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(workflowServiceMock).buildUntypedWorkflowStub(any(), eq(expectedWorkflowId));
    Mockito.verify(workflowStubMock).signalWithStart(
      eq(IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING),
      any(Object[].class),
      any(Object[].class)
    );
  }

  @Test
  void testSignalMethodsExist() {
    assertDoesNotThrow(() -> {
      checkMethodExists(IufClassificationWF.class, IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY, IufClassificationNotifyTreasurySignalDTO.class);
      checkMethodExists(IufClassificationWF.class, IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING, IufClassificationNotifyPaymentsReportingSignalDTO.class);
      checkMethodExists(IufClassificationWFClient.class, IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY, Long.class, String.class, String.class);
      checkMethodExists(IufClassificationWFClient.class, IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING, Long.class, String.class, List.class);
    });
  }

  private void checkMethodExists(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
    clazz.getMethod(methodName, parameterTypes);
  }

}
