package it.gov.pagopa.pu.workflow.mapper;

import tools.jackson.databind.json.JsonMapper;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumeWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static it.gov.pagopa.pu.workflow.mapper.SendTimelineRegistryEventMapper.*;
import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendTimelineRegistryEventMapperTest {

  private static final String EVENT_ID = "eventId";
  private static final String IUN = "iun";
  private static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
  private static final String STREAM_ID = "streamId";
  private static final String TRACE_ID = "traceId";
  private static final long ORGANIZATION_ID = 1L;
  @Mock
  private JsonMapper jsonMapperMock;

  @InjectMocks
  private SendTimelineRegistryEventMapper sendTimelineRegistryEventMapper;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      jsonMapperMock
    );
  }

  private ProgressResponseElementV28DTO buildTimelineEvent(TimelineElementV27DTO timelineElement) {
    ProgressResponseElementV28DTO event = new ProgressResponseElementV28DTO();
    event.setEventId(EVENT_ID);
    event.setIun(IUN);
    event.setNewStatus(NotificationStatusV26DTO.ACCEPTED);
    event.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    event.setElement(timelineElement);
    return event;
  }

  private TimelineElementV27DTO buildTimelineElement(TimelineElementDetailsV27DTO timelineDetails) {
    TimelineElementV27DTO timelineElement = new TimelineElementV27DTO();
    timelineElement.setCategory(TimelineElementCategoryV27DTO.NOTIFICATION_VIEWED);
    timelineElement.setDetails(timelineDetails);
    timelineElement.setEventTimestamp(OffsetDateTime.now());
    return timelineElement;
  }

  private TimelineElementDetailsV27DTO buildTimelineElementDetails() {
    TimelineElementDetailsV27DTO timelineDetails = new TimelineElementDetailsV27DTO();
    timelineDetails.setRecIndex(1);
    return timelineDetails;
  }

  private List<LegalFactsIdV20DTO> buildLegalFactsIds() {
    return List.of(
      LegalFactsIdV20DTO.builder()
        .key("legalFactId")
        .category(LegalFactCategoryDTO.SENDER_ACK.getValue())
        .build()
    );
  }

  @Test
  void testMapSuccess() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = buildTimelineElementDetails();
    TimelineElementV27DTO timelineElement = buildTimelineElement(timelineDetails);
    ProgressResponseElementV28DTO event = buildTimelineEvent(timelineElement);

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumeWF.class);

    when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, ORGANIZATION_ID, STREAM_ID, workflowId, TRACE_ID);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(TRACE_ID, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(ORGANIZATION_ID, registryEvent.getOrganizationId());
    Assertions.assertEquals(STREAM_ID, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertNotNull(event.getNewStatus());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(event.getElement().getEventTimestamp(), registryEvent.getEventTimestamp());
    Assertions.assertEquals(RegistryOutcome.OK, registryEvent.getOutcome());
    Assertions.assertEquals("serialized", registryEvent.getBody());

    Assertions.assertEquals(timelineDetails.getRecIndex(), registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent, "legalFactIds");
  }

  @Test
  void testMapError() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = buildTimelineElementDetails();
    TimelineElementV27DTO timelineElement = buildTimelineElement(timelineDetails);
    ProgressResponseElementV28DTO event = buildTimelineEvent(timelineElement);

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumeWF.class);

    when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapError(event, ORGANIZATION_ID, STREAM_ID, workflowId, TRACE_ID);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(TRACE_ID, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(ORGANIZATION_ID, registryEvent.getOrganizationId());
    Assertions.assertEquals(STREAM_ID, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertNotNull(event.getNewStatus());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(event.getElement().getEventTimestamp(), registryEvent.getEventTimestamp());
    Assertions.assertEquals(RegistryOutcome.KO, registryEvent.getOutcome());
    Assertions.assertEquals("serialized", registryEvent.getBody());

    Assertions.assertEquals(timelineDetails.getRecIndex(), registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent, "legalFactIds");
  }

  @Test
  void testMapSuccessWithNoElementDetails() {
    //GIVEN
    TimelineElementV27DTO timelineElement = buildTimelineElement(null);
    ProgressResponseElementV28DTO event = buildTimelineEvent(timelineElement);

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumeWF.class);

    when(jsonMapperMock.writeValueAsString(Mockito.any()))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, ORGANIZATION_ID, STREAM_ID, workflowId, TRACE_ID);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(TRACE_ID, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(ORGANIZATION_ID, registryEvent.getOrganizationId());
    Assertions.assertEquals(STREAM_ID, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertNotNull(event.getNewStatus());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(event.getElement().getEventTimestamp(), registryEvent.getEventTimestamp());
    Assertions.assertEquals(RegistryOutcome.OK, registryEvent.getOutcome());
    Assertions.assertEquals("serialized", registryEvent.getBody());

    Assertions.assertEquals(0, registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent, "body", "legalFactIds");
  }

  @Test
  void testMapSuccessWithLegalFactIds() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = buildTimelineElementDetails();
    TimelineElementV27DTO timelineElement = buildTimelineElement(timelineDetails);
    timelineElement.setLegalFactsIds(buildLegalFactsIds());
    ProgressResponseElementV28DTO event = buildTimelineEvent(timelineElement);

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumeWF.class);

    when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, ORGANIZATION_ID, STREAM_ID, workflowId, TRACE_ID);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(TRACE_ID, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(ORGANIZATION_ID, registryEvent.getOrganizationId());
    Assertions.assertEquals(STREAM_ID, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertNotNull(event.getNewStatus());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(event.getElement().getEventTimestamp(), registryEvent.getEventTimestamp());
    Assertions.assertEquals(RegistryOutcome.OK, registryEvent.getOutcome());
    Assertions.assertEquals("serialized", registryEvent.getBody());

    Assertions.assertEquals(timelineDetails.getRecIndex(), registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent);
  }

  @Test
  void testMapSuccessWithException() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = buildTimelineElementDetails();
    TimelineElementV27DTO timelineElement = buildTimelineElement(timelineDetails);
    ProgressResponseElementV28DTO event = buildTimelineEvent(timelineElement);

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumeWF.class);

    when(jsonMapperMock.writeValueAsString(Mockito.any()))
      .thenThrow(new RuntimeException());

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, ORGANIZATION_ID, STREAM_ID, workflowId, TRACE_ID);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(TRACE_ID, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(ORGANIZATION_ID, registryEvent.getOrganizationId());
    Assertions.assertEquals(STREAM_ID, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertNotNull(event.getNewStatus());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(event.getElement().getEventTimestamp(), registryEvent.getEventTimestamp());
    Assertions.assertEquals(RegistryOutcome.OK, registryEvent.getOutcome());
    Assertions.assertNull(registryEvent.getBody());

    Assertions.assertEquals(1, registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent, "body", "legalFactIds");
  }
}
