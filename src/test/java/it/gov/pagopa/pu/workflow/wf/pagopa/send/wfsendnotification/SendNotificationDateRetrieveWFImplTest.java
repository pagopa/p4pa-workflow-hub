package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.utils.faker.SendNotificationDTOFaker.buildSendNotificationDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationDateRetrieveWFImplTest {

  @Mock
  private SendNotificationDateRetrieveActivity sendNotificationDateRetrieveActivityMock;

  private SendNotificationDateRetrieveWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildSendNotificationDateRetrieveActivityStub()).thenReturn(sendNotificationDateRetrieveActivityMock);

    Mockito.when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfConfigMock);

    wf = new SendNotificationDateRetrieveWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      sendNotificationDateRetrieveActivityMock
    );
  }

  @Test
  void givenSendNotificationIdWhenSendNotificationDateRetrieveThenOk() {
    // Given
    String sendNotificationId = "testId";
    SendNotificationDTO expectedResponse = buildSendNotificationDTO();

    Mockito.when(sendNotificationDateRetrieveActivityMock.sendNotificationDateRetrieve(sendNotificationId))
      .thenReturn(expectedResponse);

    // When
    wf.sendNotificationDateRetrieve(sendNotificationId);

    // Then
    Mockito.verify(sendNotificationDateRetrieveActivityMock).sendNotificationDateRetrieve(sendNotificationId);
  }

}

