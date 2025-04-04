package it.gov.pagopa.pu.workflow.service.send;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.SendNotificationWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

  @Mock
  private SendNotificationWFClient sendNotificationWFClientMock;

  private SendNotificationService service;

  @BeforeEach
  void init(){
    service = new SendNotificationServiceImpl(sendNotificationWFClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(sendNotificationWFClientMock);
  }

  @Test
  void givenSendNotificationIdWhenSendNotificationProcessThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .build();

    Mockito.when(sendNotificationWFClientMock.startSendNotificationProcess(Mockito.same(sendNotificationId)))
      .thenReturn("WFID");

    // When
    WorkflowCreatedDTO result = service.sendNotificationProcess(sendNotificationId);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenSendNotificationIdWhenRetrieveNotificationDateThenOk() {
    // Given
    String sendNotificationId = "sendNotificationId";

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .build();

    Mockito.when(sendNotificationWFClientMock.startSendNotificationDateRetrieve(Mockito.same(sendNotificationId)))
      .thenReturn("WFID");

    // When
    WorkflowCreatedDTO result = service.sendNotificationDateRetrieve(sendNotificationId);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
