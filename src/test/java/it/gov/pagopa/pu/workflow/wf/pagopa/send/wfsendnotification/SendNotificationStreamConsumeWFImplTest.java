package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import it.gov.pagopa.pu.workflow.wf.pagopa.send.config.SendNotificationProcessWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  private SendNotificationStreamConsumeWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationProcessWfConfig wfConfigMock = Mockito.mock(SendNotificationProcessWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(applicationContextMock.getBean(SendNotificationProcessWfConfig.class)).thenReturn(wfConfigMock);

    wf = new SendNotificationStreamConsumeWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions();
  }

  @Test
  void readSendStream() {
    //TODO P4ADEV-3720 add test after activity pr on same ticket
  }
}
