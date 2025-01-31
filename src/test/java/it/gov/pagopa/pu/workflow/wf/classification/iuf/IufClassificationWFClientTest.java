package it.gov.pagopa.pu.workflow.wf.classification.iuf;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.wfclassification.IufClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
    IufClassificationNotifyTreasurySignalDTO signalDTO = IufClassificationNotifyTreasurySignalDTO.builder()
      .organizationId(1L)
      .iuf("iuf123")
      .treasuryId("2T")
      .build();

    String expectedWorkflowId = "IufClassificationWF-1-iuf123";

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String workflowId = client.notifyTreasury(signalDTO);

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

    String expectedWorkflowId = "IufClassificationWF-1-iuf123";

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(any(), any(), any()))
      .thenReturn(workflowExecutionMock);
    Mockito.when(workflowExecutionMock.getWorkflowId())
      .thenReturn(expectedWorkflowId);

    // When
    String workflowId = client.notifyPaymentsReporting(signalDTO);

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
      // Check that the methods exist on the IufClassificationWF and IufClassificationWFClient classes
      checkMethodExistsOnWfAndClientClasses(
        IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_TREASURY,
        IufClassificationNotifyTreasurySignalDTO.class);

      checkMethodExistsOnWfAndClientClasses(
        IufClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENTS_REPORTING,
        IufClassificationNotifyPaymentsReportingSignalDTO.class);

    });
  }

  // Helper method to check that the method exists on both classes
  private void checkMethodExistsOnWfAndClientClasses(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
    Assertions.assertNotNull(IufClassificationWF.class.getMethod(methodName, parameterTypes));
    Assertions.assertNotNull(IufClassificationWFClient.class.getMethod(methodName, parameterTypes));
  }
}
