package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wfsendlegalfact;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.delete.DeleteSendLegalFactFileActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.config.DeleteSendLegalFactFileWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class DeleteSendLegalFactFileWFImplTest {

  @Mock
  private DeleteSendLegalFactFileActivity deleteSendLegalFactFileActivityMock;

  private DeleteSendLegalFactFileWFImpl wf;

  @BeforeEach
  void setUp() {
    DeleteSendLegalFactFileWfConfig wfConfigMock = Mockito.mock(DeleteSendLegalFactFileWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildDeleteSendLegalFactFileActivityStub()).thenReturn(deleteSendLegalFactFileActivityMock);
    Mockito.when(applicationContextMock.getBean(DeleteSendLegalFactFileWfConfig.class)).thenReturn(wfConfigMock);

    wf = new DeleteSendLegalFactFileWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      deleteSendLegalFactFileActivityMock
    );
  }

  @Test
  void givenNextFileExpirationDateWhenDeleteSendLegalFactExpiredFilesThenContinueAsNew() {
    String sendNotificationId = "sendNotificationId";
    OffsetDateTime nextFileExpirationDate = OffsetDateTime.now();

    Mockito.when(deleteSendLegalFactFileActivityMock.deleteSendLegalFactFile(sendNotificationId)).thenReturn(nextFileExpirationDate);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::currentTimeMillis).thenReturn(nextFileExpirationDate.minusMinutes(1).toInstant().toEpochMilli());
      workflowMock.when(() -> Workflow.sleep(Mockito.eq(Duration.of(1, ChronoUnit.MINUTES))))
        .then(invocation -> null);

      wf.deleteSendLegalFactExpiredFiles(sendNotificationId);

      workflowMock.verify(() -> Workflow.continueAsNew(sendNotificationId));
    }
  }

  @Test
  void givenNoNextFileExpirationDateWhenDeleteSendLegalFactExpiredFilesThenOk() {
    String sendNotificationId = "sendNotificationId";

    Mockito.when(deleteSendLegalFactFileActivityMock.deleteSendLegalFactFile(sendNotificationId)).thenReturn(null);
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      wf.deleteSendLegalFactExpiredFiles(sendNotificationId);

      workflowMock.verifyNoInteractions();
    }
  }
}

