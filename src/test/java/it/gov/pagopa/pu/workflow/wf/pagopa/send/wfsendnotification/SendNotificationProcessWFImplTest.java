package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendNotificationConflictException;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.utils.faker.SendNotificationDTOFaker;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.ScheduleSendNotificationDateRetrieveActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.mapper.SendNotification2DebtPositionSendNotificationsMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SendNotificationProcessWFImplTest {

  @Mock
  private PreloadSendFileActivity preloadSendFileActivityMock;
  @Mock
  private UploadSendFileActivity uploadSendFileActivityMock;
  @Mock
  private DeliveryNotificationActivity deliveryNotificationActivityMock;
  @Mock
  private NotificationStatusActivity notificationStatusActivityMock;
  @Mock
  private GetSendNotificationActivity getSendNotificationActivityMock;
  @Mock
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;
  @Mock
  private ScheduleSendNotificationDateRetrieveActivity scheduleSendNotificationDateRetrieveActivityMock;

  private SendNotificationProcessWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildPreloadSendFileActivityStub()).thenReturn(preloadSendFileActivityMock);
    Mockito.when(wfConfigMock.buildUploadSendFileActivityStub()).thenReturn(uploadSendFileActivityMock);
    Mockito.when(wfConfigMock.buildDeliveryNotificationActivityStub()).thenReturn(deliveryNotificationActivityMock);
    Mockito.when(wfConfigMock.buildNotificationStatusActivityStub()).thenReturn(notificationStatusActivityMock);
    Mockito.when(wfConfigMock.buildGetSendNotificationActivityStub()).thenReturn(getSendNotificationActivityMock);
    Mockito.when(wfConfigMock.buildPublishSendNotificationPaymentEventActivityStub()).thenReturn(publishSendNotificationPaymentEventActivityMock);
    Mockito.when(wfConfigMock.buildScheduleSendNotificationDateRetrieveActivityStub()).thenReturn(scheduleSendNotificationDateRetrieveActivityMock);

    Mockito.when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfConfigMock);

    wf = new SendNotificationProcessWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      preloadSendFileActivityMock,
      uploadSendFileActivityMock,
      deliveryNotificationActivityMock,
      notificationStatusActivityMock,
      getSendNotificationActivityMock,
      publishSendNotificationPaymentEventActivityMock,
      scheduleSendNotificationDateRetrieveActivityMock
    );
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenStatusAcceptedWhenSendNotificationProcessThenOk(boolean isPaymentEmpty) {
    String sendNotificationId = "testId";
    SendNotificationDTO expectedResponse = SendNotificationDTOFaker.buildSendNotificationDTO();
    expectedResponse.status(NotificationStatus.ACCEPTED);
    if (isPaymentEmpty) {
      expectedResponse.setPayments(new ArrayList<>());
    }

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(expectedResponse);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.sendNotificationProcess(sendNotificationId);
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
    Mockito.verify(notificationStatusActivityMock).getSendNotificationStatus(sendNotificationId);
    if (!isPaymentEmpty) {
      SendNotificationDTOFaker.buildListDebtPositionSendNotificationDTO(expectedResponse).forEach(p ->
        Mockito.verify(publishSendNotificationPaymentEventActivityMock).publishSendNotificationEvent(p, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null))
      );
      Mockito.verify(scheduleSendNotificationDateRetrieveActivityMock).scheduleSendNotificationDateRetrieveWF(sendNotificationId, Duration.ofMinutes(30));
    }
  }

  @Test
  void givenStatusNotAcceptedWhenSendNotificationProcessThenRetries() {
    String sendNotificationId = "testId";
    SendNotificationDTO expectedResponse = SendNotificationDTOFaker.buildSendNotificationDTO();
    expectedResponse.setStatus(NotificationStatus.COMPLETE);

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(expectedResponse);
    Mockito.when(getSendNotificationActivityMock.getSendNotification(sendNotificationId))
      .thenReturn(expectedResponse);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      Exception exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals(
        "Exceeded max retry attempts to wait for ACCEPTED status (attempts:10) on sendNotificationId testId. Last status was: COMPLETE",
        exception.getMessage()
      );
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
    Mockito.verify(notificationStatusActivityMock, Mockito.times(10)).getSendNotificationStatus(sendNotificationId);
    SendNotificationDTOFaker.buildListDebtPositionSendNotificationDTO(expectedResponse).forEach(p ->
      Mockito.verify(publishSendNotificationPaymentEventActivityMock).publishSendNotificationErrorEvent(p, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, "Exceeded max retry attempts to wait for ACCEPTED status (attempts:10) on sendNotificationId testId. Last status was: COMPLETE"))
    );
  }

  @Test
  void givenStatusErrorWhenSendNotificationProcessThenThrowWorkflowInternalErrorException() {
    String sendNotificationId = "testId";

    SendNotificationDTO expectedResponse = SendNotificationDTOFaker.buildSendNotificationDTO();
    expectedResponse.status(NotificationStatus.ERROR);
    expectedResponse.setErrors(List.of("Error1", "Error2"));

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(expectedResponse);

    Mockito.when(getSendNotificationActivityMock.getSendNotification(sendNotificationId))
      .thenReturn(expectedResponse);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      WorkflowInternalErrorException exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals(
        "[SEND_STATUS_ERROR] Workflow terminated during getSendNotificationStatus for sendNotificationId " + sendNotificationId +
          " with ERROR: " + expectedResponse.getErrors(),
        exception.getMessage()
      );

      Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
      Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
      Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
      Mockito.verify(notificationStatusActivityMock).getSendNotificationStatus(sendNotificationId);
      Mockito.verify(getSendNotificationActivityMock).getSendNotification(sendNotificationId);
      SendNotification2DebtPositionSendNotificationsMapper.map(expectedResponse).forEach(p ->
        Mockito.verify(publishSendNotificationPaymentEventActivityMock).publishSendNotificationErrorEvent(
          Mockito.eq(p),
          Mockito.argThat(event -> event.getPaymentEventType() == PaymentEventType.SEND_NOTIFICATION_ERROR &&
            event.getEventDescription().contains("Workflow terminated during getSendNotificationStatus"))
        )
      );
      Mockito.verify(scheduleSendNotificationDateRetrieveActivityMock, Mockito.never())
        .scheduleSendNotificationDateRetrieveWF(Mockito.anyString(), Mockito.any());
    }
  }

  @ParameterizedTest
  @MethodSource("notificationScenarios")
  void givenRuntimeExceptionWhenSendNotificationProcessThenHandleCorrectly(SendNotificationDTO notification, boolean shouldPublishEvents) {
    String sendNotificationId = "testId";

    Mockito.when(getSendNotificationActivityMock.getSendNotification(sendNotificationId))
      .thenReturn(notification);

    Mockito.doThrow(new RuntimeException("Simulated failure"))
      .when(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);

    assertThrows(RuntimeException.class, () -> wf.sendNotificationProcess(sendNotificationId));

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
    Mockito.verify(getSendNotificationActivityMock).getSendNotification(sendNotificationId);
    if (shouldPublishEvents) {
      SendNotification2DebtPositionSendNotificationsMapper.map(notification).forEach(p ->
        Mockito.verify(publishSendNotificationPaymentEventActivityMock)
          .publishSendNotificationErrorEvent(
            Mockito.eq(p),
            Mockito.argThat(event -> event.getPaymentEventType() == PaymentEventType.SEND_NOTIFICATION_ERROR &&
              event.getEventDescription().contains("Simulated failure"))
          )
      );
    } else {
      Mockito.verify(publishSendNotificationPaymentEventActivityMock, Mockito.never())
        .publishSendNotificationErrorEvent(Mockito.any(), Mockito.any());
    }
  }

  private static Stream<Arguments> notificationScenarios() {
    SendNotificationDTO withPayments = SendNotificationDTOFaker.buildSendNotificationDTO();
    withPayments.setPayments(List.of(new SendNotificationPaymentsDTO()));

    SendNotificationDTO withoutPayments = SendNotificationDTOFaker.buildSendNotificationDTO();
    withoutPayments.setPayments(Collections.emptyList());

    return Stream.of(
      Arguments.of(null, false),
      Arguments.of(withoutPayments, false),
      Arguments.of(withPayments, true)
    );
  }

  @Test
  void givenStatusNotAcceptedAndNotificationNullWhenSendNotificationProcessThenLogAndThrow() {
    String sendNotificationId = "testId";

    SendNotificationDTO statusResponse = new SendNotificationDTO();
    statusResponse.setStatus(NotificationStatus.COMPLETE);

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(statusResponse);
    Mockito.when(getSendNotificationActivityMock.getSendNotification(sendNotificationId))
      .thenReturn(null);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      WorkflowInternalErrorException exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals(
        "Exceeded max retry attempts to wait for ACCEPTED status (attempts:10) on sendNotificationId testId. Last status was: COMPLETE",
        exception.getMessage()
      );
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
    Mockito.verify(notificationStatusActivityMock, Mockito.times(10)).getSendNotificationStatus(sendNotificationId);
    Mockito.verify(getSendNotificationActivityMock).getSendNotification(sendNotificationId);
    Mockito.verifyNoInteractions(publishSendNotificationPaymentEventActivityMock);
    Mockito.verify(scheduleSendNotificationDateRetrieveActivityMock, Mockito.never())
      .scheduleSendNotificationDateRetrieveWF(Mockito.anyString(), Mockito.any());
  }

  @Test
  void givenNullNotificationStatusWhenSendNotificationProcessThenThrowWithNullStatusMessage() {
    String sendNotificationId = "testId";

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(null);
    Mockito.when(getSendNotificationActivityMock.getSendNotification(sendNotificationId))
      .thenReturn(null);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      WorkflowInternalErrorException exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals(
        "Exceeded max retry attempts to wait for ACCEPTED status (attempts:10) on sendNotificationId testId. Last status was: null",
        exception.getMessage()
      );
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
    Mockito.verify(notificationStatusActivityMock, Mockito.times(10)).getSendNotificationStatus(sendNotificationId);
    Mockito.verify(getSendNotificationActivityMock).getSendNotification(sendNotificationId);
    Mockito.verifyNoInteractions(publishSendNotificationPaymentEventActivityMock);
    Mockito.verify(scheduleSendNotificationDateRetrieveActivityMock, Mockito.never())
      .scheduleSendNotificationDateRetrieveWF(Mockito.anyString(), Mockito.any());
  }

  @Test
  void givenConflictWhenSendNotificationProcessThenThrowWorkflowInternalErrorException() {
    String sendNotificationId = "testId";

    Mockito.doNothing().when(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.doNothing().when(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);

    Mockito.doThrow(SendNotificationConflictException.class)
      .when(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      WorkflowInternalErrorException exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals(
        "[SEND_DELIVERY_CONFLICT] Workflow terminated during deliverySendNotification for sendNotificationId " + sendNotificationId,
        exception.getMessage()
      );
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);

    Mockito.verifyNoInteractions(notificationStatusActivityMock);
    Mockito.verifyNoInteractions(getSendNotificationActivityMock);
    Mockito.verifyNoInteractions(publishSendNotificationPaymentEventActivityMock);
    Mockito.verifyNoInteractions(scheduleSendNotificationDateRetrieveActivityMock);
  }

}

