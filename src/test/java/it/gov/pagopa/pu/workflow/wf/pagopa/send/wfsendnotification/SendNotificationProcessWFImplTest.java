package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.DeliveryNotificationActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.NotificationStatusActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.PreloadSendFileActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.UploadSendFileActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.NotificationStatus;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.utils.faker.SendNotificationDTOFaker;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.activity.PublishSendNotificationPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;

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
  private PublishSendNotificationPaymentEventActivity publishSendNotificationPaymentEventActivityMock;

  private SendNotificationProcessWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildPreloadSendFileActivityStub()).thenReturn(preloadSendFileActivityMock);
    Mockito.when(wfConfigMock.buildUploadSendFileActivityStub()).thenReturn(uploadSendFileActivityMock);
    Mockito.when(wfConfigMock.buildDeliveryNotificationActivityStub()).thenReturn(deliveryNotificationActivityMock);
    Mockito.when(wfConfigMock.buildNotificationStatusActivityStub()).thenReturn(notificationStatusActivityMock);
    Mockito.when(wfConfigMock.buildPublishSendNotificationPaymentEventActivityStub()).thenReturn(publishSendNotificationPaymentEventActivityMock);

    Mockito.when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfConfigMock);

    wf = new SendNotificationProcessWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @Test
  void givenStatusAcceptedWhenSendNotificationProcessThenOk() {
    String sendNotificationId = "testId";
    SendNotificationDTO expectedResponse = SendNotificationDTOFaker.buildSendNotificationDTO();
    expectedResponse.status(NotificationStatus.ACCEPTED);

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(expectedResponse);

    wf.sendNotificationProcess(sendNotificationId);

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliveryNotification(sendNotificationId);
    Mockito.verify(notificationStatusActivityMock).getSendNotificationStatus(sendNotificationId);
    SendNotificationDTOFaker.buildListDebtPositionSendNotificationDTO(expectedResponse).forEach(p ->
      Mockito.verify(publishSendNotificationPaymentEventActivityMock).publishSendNotificationEvent(p, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_CREATED, null))
    );
  }

  @Test
  void givenStatusNotAcceptedWhenSendNotificationProcessThenRetries() {
    String sendNotificationId = "testId";
    SendNotificationDTO expectedResponse = SendNotificationDTOFaker.buildSendNotificationDTO();
    expectedResponse.setStatus(NotificationStatus.COMPLETE);

    Mockito.when(notificationStatusActivityMock.getSendNotificationStatus(sendNotificationId))
      .thenReturn(expectedResponse);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      Exception exception = assertThrows(
        WorkflowInternalErrorException.class,
        () -> wf.sendNotificationProcess(sendNotificationId)
      );

      assertEquals(
        "Max retries reached: notification status not ACCEPTED for sendNotificationId " + sendNotificationId + ". Last status was: COMPLETE",
        exception.getMessage()
      );
    }

    Mockito.verify(preloadSendFileActivityMock).preloadSendFile(sendNotificationId);
    Mockito.verify(uploadSendFileActivityMock).uploadSendFile(sendNotificationId);
    Mockito.verify(deliveryNotificationActivityMock).deliveryNotification(sendNotificationId);
    Mockito.verify(notificationStatusActivityMock, Mockito.times(10)).getSendNotificationStatus(sendNotificationId);
    SendNotificationDTOFaker.buildListDebtPositionSendNotificationDTO(expectedResponse).forEach(p ->
      Mockito.verify(publishSendNotificationPaymentEventActivityMock).publishSendNotificationErrorEvent(p, new PaymentEventRequestDTO(PaymentEventType.SEND_NOTIFICATION_ERROR, "Exceeded max retry attempts to wait for ACCEPTED status: 10"))
    );
  }

}

