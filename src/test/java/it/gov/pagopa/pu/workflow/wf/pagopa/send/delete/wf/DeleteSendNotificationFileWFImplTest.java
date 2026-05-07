package it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.wf;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.delete.DeleteSendNotificationFileActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.delete.config.DeleteSendNotificationFileWfConfig;
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
class DeleteSendNotificationFileWFImplTest {

  @Mock
  private DeleteSendNotificationFileActivity deleteSendNotificationFileActivityMock;

  private DeleteSendNotificationFileWFImpl wf;

  @BeforeEach
  void setUp() {
    DeleteSendNotificationFileWfConfig wfConfigMock = Mockito.mock(DeleteSendNotificationFileWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildDeleteSendNotificationFileActivityStub()).thenReturn(deleteSendNotificationFileActivityMock);
    Mockito.when(applicationContextMock.getBean(DeleteSendNotificationFileWfConfig.class)).thenReturn(wfConfigMock);

    wf = new DeleteSendNotificationFileWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      deleteSendNotificationFileActivityMock
    );
  }

  @Test
  void givenNextFileExpirationDateWhenDeleteSendNotificationExpiredFilesThenContinueAsNew() {
    String sendNotificationId = "sendNotificationId";
    OffsetDateTime nextFileExpirationDate = OffsetDateTime.now();

    Mockito.when(deleteSendNotificationFileActivityMock.deleteSendNotificationExpiredFiles(sendNotificationId)).thenReturn(nextFileExpirationDate);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::currentTimeMillis).thenReturn(nextFileExpirationDate.minusMinutes(1).toInstant().toEpochMilli());
      workflowMock.when(() -> Workflow.sleep(Mockito.eq(Duration.of(1, ChronoUnit.MINUTES))))
        .then(invocation -> null);

      wf.deleteSendNotificationExpiredFiles(sendNotificationId);

      workflowMock.verify(() -> Workflow.continueAsNew(sendNotificationId));
    }
  }

  @Test
  void givenNoNextFileExpirationDateWhenDeleteSendNotificationExpiredFilesThenOk() {
    String sendNotificationId = "sendNotificationId";

    Mockito.when(deleteSendNotificationFileActivityMock.deleteSendNotificationExpiredFiles(sendNotificationId)).thenReturn(null);
    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      wf.deleteSendNotificationExpiredFiles(sendNotificationId);

      workflowMock.verifyNoInteractions();
    }
  }
}

