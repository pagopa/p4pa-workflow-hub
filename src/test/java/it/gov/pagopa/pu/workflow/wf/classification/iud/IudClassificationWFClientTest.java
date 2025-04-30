package it.gov.pagopa.pu.workflow.wf.classification.iud;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class IudClassificationWFClientTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private WorkflowExecution workflowExecutionMock;

  private IudClassificationWFClient client;

  @BeforeEach
  void setUp() {
    client = new IudClassificationWFClient(workflowServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void notifyReceipt() {
    // Given
    IudClassificationNotifyReceiptSignalDTO signalDTO = IudClassificationNotifyReceiptSignalDTO.builder()
      .organizationId(1L)
      .iud("iud123")
      .iur("iur123")
      .iuv("iuv123")
      .transferIndexes(Collections.singletonList(1))
      .build();

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IudClassificationWF-1-iud123", "RUNID");

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(
        eq(IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_RECEIPT),
        any(),
        any()))
      .thenReturn(workflowExecutionMock);

    // When
    WorkflowCreatedDTO result = client.notifyReceipt(signalDTO);

    // Then
    assertEquals(expectedResult, result);
  }

  @Test
  void notifyPaymentNotification() {
    // Given
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
      .organizationId(1L)
      .iud("iud123")
      .build();

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IudClassificationWF-1-iud123", "RUNID");

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(any(String.class), any(String.class)))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowStubMock.signalWithStart(
        eq(IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION),
        any(),
        any()))
      .thenReturn(workflowExecutionMock);

    // When
    WorkflowCreatedDTO result = client.notifyPaymentNotification(signalDTO);

    // Then
    assertEquals(expectedResult, result);
  }

  @Test
  void testSignalMethodsExist() {
    assertDoesNotThrow(() -> {
      checkMethodExistsOnWfAndClientClasses(
        IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_RECEIPT,
        IudClassificationNotifyReceiptSignalDTO.class);

      checkMethodExistsOnWfAndClientClasses(
        IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION,
        IudClassificationNotifyPaymentNotificationSignalDTO.class);
    });
  }

  // Helper method to check that the method exists on both classes
  private void checkMethodExistsOnWfAndClientClasses(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
    Assertions.assertNotNull(IudClassificationWF.class.getMethod(methodName, parameterTypes));
    Assertions.assertNotNull(IudClassificationWFClient.class.getMethod(methodName, parameterTypes));
  }

  @ParameterizedTest
  @MethodSource("provideNullValues")
  void whenGenerateWorkflowIdThenWorkflowInternalErrorException(Long organizationId, String iud) {
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
        .organizationId(organizationId)
        .iud(iud)
        .build();

    assertThrows(WorkflowInternalErrorException.class,
        () -> client.notifyPaymentNotification(signalDTO),
        "The organizationId or iud must not be null");
  }

  private static Stream<Arguments> provideNullValues() {
    return Stream.of(
        Arguments.of(null, "iud123"),
        Arguments.of(1L, null),
        Arguments.of(null, null)
    );
  }
}
