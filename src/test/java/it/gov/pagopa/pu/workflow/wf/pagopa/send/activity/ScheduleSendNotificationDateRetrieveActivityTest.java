package it.gov.pagopa.pu.workflow.wf.pagopa.send.activity;

import it.gov.pagopa.pu.workflow.wf.pagopa.send.SendNotificationWFClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleSendNotificationDateRetrieveActivityTest {

  @Mock
  private SendNotificationWFClient sendNotificationWFClientMock;

  private ScheduleSendNotificationDateRetrieveActivity activity;

  @BeforeEach
  void init(){
    activity = new ScheduleSendNotificationDateRetrieveActivityImpl(sendNotificationWFClientMock);
  }

  @Test
  void whenScheduleSendNotificationDateRetrieveThenInvokeClient(){
    // Given
    String sendNotificationId = "sendNotificationId";

    // When
    activity.scheduleSendNotificationDateRetrieveWF(sendNotificationId);

    // Then
    Mockito.verify(sendNotificationWFClientMock).sendNotificationDateRetrieve(sendNotificationId);
  }
}
