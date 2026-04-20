package it.gov.pagopa.pu.workflow.wf.pagopa.send.create.wf;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.DeliveryNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.GetSendNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.PreloadSendFileActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.create.UploadSendFileActivity;
import it.gov.pagopa.payhub.activities.exception.sendnotification.SendNotificationConflictException;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationPaymentsDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utils.faker.SendNotificationDTOFaker;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.config.SendNotificationProcessWfConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.create.mapper.SendNotification2DebtPositionSendNotificationsMapper;
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
  private GetSendNotificationActivity getSendNotificationActivityMock;
  @Mock
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;

  private SendNotificationProcessWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildPreloadSendFileActivityStub()).thenReturn(preloadSendFileActivityMock);
    Mockito.when(wfConfigMock.buildUploadSendFileActivityStub()).thenReturn(uploadSendFileActivityMock);
    Mockito.when(wfConfigMock.buildDeliveryNotificationActivityStub()).thenReturn(deliveryNotificationActivityMock);
    Mockito.when(wfConfigMock.buildGetSendNotificationActivityStub()).thenReturn(getSendNotificationActivityMock);
    Mockito.when(wfConfigMock.buildPublishSendNotificationPaymentEventActivityStub()).thenReturn(publishSendNotificationPaymentEventActivityMock);

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
      getSendNotificationActivityMock,
      publishSendNotificationPaymentEventActivityMock
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

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      wf.sendNotificationProcess(sendNotificationId);
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);
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
  void givenConflictWhenSendNotificationProcessThenThrowWorkflowInternalErrorException() {
    String sendNotificationId = "testId";

    Mockito.doNothing().when(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.doNothing().when(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);

    Mockito.doThrow(SendNotificationConflictException.class)
      .when(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      IllegalStateBusinessException exception = assertThrows(
        IllegalStateBusinessException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals("SEND_DELIVERY_CONFLICT", exception.getCode());
      assertEquals(
        "Workflow terminated during deliverySendNotification for sendNotificationId " + sendNotificationId,
        exception.getMessage()
      );
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliverySendNotification(sendNotificationId);

    Mockito.verifyNoInteractions(getSendNotificationActivityMock);
    Mockito.verifyNoInteractions(publishSendNotificationPaymentEventActivityMock);
  }

}

