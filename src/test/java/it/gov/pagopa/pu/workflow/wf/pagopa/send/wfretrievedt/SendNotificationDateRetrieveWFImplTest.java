package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfretrievedt;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.*;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.utils.faker.SendNotificationDTOFaker.buildSendNotificationDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

  @Test
  void givenSendNotificationIdWhenSendNotificationDateRetrieveThenRetriesUntilSuccess() {
    // Given
    String sendNotificationId = "testId";
    SendNotificationDTO expectedResponse = buildSendNotificationDTO();

    Mockito.when(sendNotificationDateRetrieveActivityMock.sendNotificationDateRetrieve(sendNotificationId))
      .thenReturn(null)
      .thenReturn(null)
      .thenReturn(expectedResponse);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.anyLong()))
        .then(invocation -> null);

      // When
      SendNotificationDTO actualResponse = wf.sendNotificationDateRetrieve(sendNotificationId);

      // Then
      Mockito.verify(sendNotificationDateRetrieveActivityMock, Mockito.times(3)).sendNotificationDateRetrieve(sendNotificationId);
      assertEquals(expectedResponse, actualResponse);
    }
  }
}
