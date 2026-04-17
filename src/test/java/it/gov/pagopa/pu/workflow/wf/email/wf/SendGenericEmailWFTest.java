package it.gov.pagopa.pu.workflow.wf.email.wf;

import it.gov.pagopa.payhub.activities.activity.email.SendEmailActivity;
import it.gov.pagopa.payhub.activities.dto.email.EmailDTO;
import it.gov.pagopa.pu.workflow.wf.email.config.SendGenericEmailWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendGenericEmailWFTest {

  @Mock
  private SendEmailActivity sendEmailActivityMock;

  private SendGenericEmailWFImpl workflow;

  @BeforeEach
  void setUp() {
    SendGenericEmailWFConfig configMock = mock(SendGenericEmailWFConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildSendEmailActivityStub()).thenReturn(sendEmailActivityMock);

    when(applicationContextMock.getBean(SendGenericEmailWFConfig.class)).thenReturn(configMock);

    workflow = new SendGenericEmailWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendEmailActivityMock);
  }

  @Test
  void test(){
    // Given
    EmailDTO emailDTO = new EmailDTO();

    // When
    workflow.sendGenericEmail(emailDTO);

    // Then
    Mockito.verify(sendEmailActivityMock)
      .sendEmail(Mockito.same(emailDTO));
  }
}
