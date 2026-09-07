package it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.GetSendNotificationEventsFromStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.GetSendStreamActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.stream.UpdateLastProcessedStreamEventIdActivity;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.SendStreamEventsProcessWFInputDTO;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.config.SendNotificationStreamWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SendNotificationStreamConsumeWFImplTest {

  public static final long ORGANIZATION_ID = 1L;
  public static final String SEND_EVENT_ID = "sendEventId";
  public static final String INVALID_SEND_STREAM_ID = "invalidSendStreamId";
  public static final String SEND_STREAM_ID = "sendStreamId";

  @Mock
  private GetSendStreamActivity getSendStreamActivityMock;
  @Mock
  private GetSendNotificationEventsFromStreamActivity getSendNotificationEventsFromStreamActivityMock;
  @Mock
  private UpdateLastProcessedStreamEventIdActivity updateLastProcessedStreamEventIdActivityMock;

  private SendNotificationStreamConsumeWFImpl wf;

  @BeforeEach
  void setUp() {
    SendNotificationStreamWfConfig wfConfigMock = mock(SendNotificationStreamWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(wfConfigMock.buildGetSendStreamActivityStub()).thenReturn(getSendStreamActivityMock);
    when(wfConfigMock.buildGetSendNotificationEventsFromStreamActivityStub()).thenReturn(getSendNotificationEventsFromStreamActivityMock);
    when(wfConfigMock.buildUpdateLastProcessedStreamEventIdActivityStub()).thenReturn(updateLastProcessedStreamEventIdActivityMock);

    when(applicationContextMock.getBean(SendNotificationStreamWfConfig.class)).thenReturn(wfConfigMock);

    wf = new SendNotificationStreamConsumeWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      getSendStreamActivityMock,
      getSendNotificationEventsFromStreamActivityMock,
      updateLastProcessedStreamEventIdActivityMock
    );
  }

  @Test
  void givenInvalidSendStreamIdWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    when(getSendStreamActivityMock.fetchSendStream(INVALID_SEND_STREAM_ID))
      .thenReturn(null); //for not entering do-while loop

    //WHEN
    IllegalStateBusinessException workflowInternalErrorException =
      Assertions.assertThrows(IllegalStateBusinessException.class, () -> wf.readSendStream(INVALID_SEND_STREAM_ID));

    //THEN
    verify(getSendStreamActivityMock).fetchSendStream(INVALID_SEND_STREAM_ID);
    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_SEND_STATUS_ERROR, workflowInternalErrorException.getCode());
    Assertions.assertEquals(
      "Workflow terminated during starting of readSendStream for sendStreamId %s with ERROR: cannot found SEND stream.".formatted(INVALID_SEND_STREAM_ID),
      workflowInternalErrorException.getMessage()
    );
  }

  @Test
  void givenErrorInFetchSendNotificationEventsFromStreamWhenReadSendStreamThenStreamNotFound() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    doThrow(new RuntimeException())
      .when(getSendNotificationEventsFromStreamActivityMock)
      .fetchSendNotificationEventsFromStream(ORGANIZATION_ID, SEND_STREAM_ID);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
    }

  }

  @Test
  void givenEmptyEventListInFetchSendNotificationEventsFromStreamWhenReadSendStreamThenDoNotCommit() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    doReturn(new ArrayList<>())
      .when(getSendNotificationEventsFromStreamActivityMock)
      .fetchSendNotificationEventsFromStream(ORGANIZATION_ID, SEND_STREAM_ID);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);

      //WHEN
      wf.readSendStream(SEND_STREAM_ID);

      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
    }

  }

  @Test
  void givenCorrectSendStreamEventProcessingWhenReadSendStreamThenCommit() {
    //GIVEN
    SendStreamDTO streamDTO = buildSendStreamDTO();

    ProgressResponseElementV28DTO sendEvent1 = buildSendEvent();
    List<ProgressResponseElementV28DTO> streamEvents = List.of(
      sendEvent1
    );

    when(getSendStreamActivityMock.fetchSendStream(SEND_STREAM_ID))
      .thenReturn(streamDTO)
      .thenReturn(null); //for breaking from do-while loop

    when(
      getSendNotificationEventsFromStreamActivityMock.fetchSendNotificationEventsFromStream(
        ORGANIZATION_ID, SEND_STREAM_ID
      )
    ).thenReturn(streamEvents);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class)))
        .then(invocation -> null);
      SendNotificationStreamConsumeChildWF childWF = Mockito.mock(SendNotificationStreamConsumeChildWFImpl.class);
      when(childWF.processingStreamEvents(Mockito.any(SendStreamEventsProcessWFInputDTO.class)))
        .thenReturn(sendEvent1.getEventId());
      workflowMock.when(() -> Workflow.newChildWorkflowStub(SendNotificationStreamConsumeChildWF.class))
        .thenReturn(childWF);
      //WHEN
      wf.readSendStream(SEND_STREAM_ID);
      //THEN
      verify(getSendStreamActivityMock, times(2)).fetchSendStream(SEND_STREAM_ID);
      verify(updateLastProcessedStreamEventIdActivityMock)
        .updateLastProcessedStreamEventId(
          SEND_STREAM_ID,
          sendEvent1.getEventId()
        );
    }

  }

  private static ProgressResponseElementV28DTO buildSendEvent() {
    ProgressResponseElementV28DTO sendEvent = new ProgressResponseElementV28DTO();
    sendEvent.setEventId(SEND_EVENT_ID);
    sendEvent.setNewStatus(NotificationStatusV26DTO.VIEWED);
    return sendEvent;
  }

  private static SendStreamDTO buildSendStreamDTO() {
    SendStreamDTO streamDTO = new SendStreamDTO();
    streamDTO.setStreamId(SEND_STREAM_ID);
    streamDTO.setOrganizationId(ORGANIZATION_ID);
    return streamDTO;
  }

}
