package it.gov.pagopa.pu.workflow.service.wf.send;

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
      .runId("RUNID")
      .build();

    Mockito.when(sendNotificationWFClientMock.startSendNotificationProcess(Mockito.same(sendNotificationId)))
      .thenReturn(expectedResult);

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
      .runId("RUNID")
      .build();

    Mockito.when(sendNotificationWFClientMock.startSendNotificationDateRetrieve(Mockito.same(sendNotificationId)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.sendNotificationDateRetrieve(sendNotificationId);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenSendNotificationIdWhenSendNotificationStreamConsumeThenOk() {
    // Given
    String sendStreamId = "sendStreamId";

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .runId("RUNID")
      .build();

    Mockito.when(sendNotificationWFClientMock.startSendNotificationStreamConsume(Mockito.same(sendStreamId)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.sendNotificationStreamConsume(sendStreamId);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

}
