package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  @Mock
  private ApplicationContext applicationContextMock;

  private SendNotificationStreamConsumeWFImpl wf;

  @BeforeEach
  void setUp() {
    wf = new SendNotificationStreamConsumeWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {}

  @Test
  void readSendStream() {
    //TODO P4ADEV-3720 add test after activity pr on same ticket
  }
}
