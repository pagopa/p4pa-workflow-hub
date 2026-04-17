package it.gov.pagopa.pu.workflow.wf.classification.iud;

import io.temporal.client.WorkflowStub;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.workflow.service.organization.OrganizationRetrieverService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWF;
import it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWFImpl;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class IudClassificationWFClientTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private WorkflowStub workflowStubMock;
  @Mock
  private OrganizationRetrieverService organizationRetrieverServiceMock;

  private IudClassificationWFClient client;
  private final Class<IudClassificationWF> workflowClass = IudClassificationWF.class;

  @BeforeEach
  void setUp() {
    client = new IudClassificationWFClient(workflowServiceMock, workflowClientServiceMock, organizationRetrieverServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, workflowStubMock, organizationRetrieverServiceMock);
  }

  @Test
  void testSignalMethodsExist() {
    TemporalTestUtils.assertSignalMethodExists(workflowClass,
      IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_RECEIPT, IudClassificationNotifyReceiptSignalDTO.class);

    TemporalTestUtils.assertSignalMethodExists(workflowClass,
      IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION, IudClassificationNotifyPaymentNotificationSignalDTO.class);
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
    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(signalDTO.getOrganizationId())).thenReturn(true);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IudClassificationWF-1-iud123", "RUNID");

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;

    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(workflowClass, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowClientServiceMock.signalWithStart(
        same(workflowStubMock),
        eq(IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_RECEIPT),
        argThat(o -> o[0] == signalDTO),
        argThat(o -> o.length==0)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.notifyReceipt(signalDTO);

    // Then
    assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, IudClassificationWFImpl.class);
  }

  @Test
  void givenClassificationDisabledWhenNotifyReceiptThenReturnNull(){
    // Given
    IudClassificationNotifyReceiptSignalDTO signalDTO = IudClassificationNotifyReceiptSignalDTO.builder()
      .organizationId(1L)
      .build();

    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(signalDTO.getOrganizationId())).thenReturn(false);

    // When
    WorkflowCreatedDTO result = client.notifyReceipt(signalDTO);

    // Then
    assertNull(result);
  }

  @Test
  void notifyPaymentNotification() {
    // Given
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
      .organizationId(1L)
      .iud("iud123")
      .build();

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("IudClassificationWF-1-iud123", "RUNID");

    String taskQueue = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY;
    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(signalDTO.getOrganizationId())).thenReturn(true);
    Mockito.when(workflowServiceMock.buildUntypedWorkflowStub(workflowClass, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(workflowStubMock);
    Mockito.when(workflowClientServiceMock.signalWithStart(
        same(workflowStubMock),
        eq(IudClassificationWF.SIGNAL_METHOD_NAME_NOTIFY_PAYMENT_NOTIFICATION),
        argThat(o -> o[0] == signalDTO),
        argThat(o -> o.length==0)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = client.notifyPaymentNotification(signalDTO);

    // Then
    assertSame(expectedResult, result);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, IudClassificationWFImpl.class);
  }

  @Test
  void givenClassificationDisabledWhenNotifyPaymentNotificationThenReturnNull(){
    // Given
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
      .organizationId(1L)
      .build();

    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(signalDTO.getOrganizationId())).thenReturn(false);

    // When
    WorkflowCreatedDTO result = client.notifyPaymentNotification(signalDTO);

    // Then
    assertNull(result);
  }

  @ParameterizedTest
  @MethodSource("provideNullValues")
  void whenGenerateWorkflowIdThenWorkflowInternalErrorException(Long organizationId, String iud) {
    IudClassificationNotifyPaymentNotificationSignalDTO signalDTO = IudClassificationNotifyPaymentNotificationSignalDTO.builder()
      .organizationId(1L)
      .build();

    Mockito.when(organizationRetrieverServiceMock.isClassificationEnabled(signalDTO.getOrganizationId())).thenReturn(true);

    assertThrows(InvalidValueException.class,
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
