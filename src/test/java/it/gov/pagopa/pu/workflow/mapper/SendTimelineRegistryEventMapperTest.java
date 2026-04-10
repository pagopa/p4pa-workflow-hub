package it.gov.pagopa.pu.workflow.mapper;

import tools.jackson.databind.json.JsonMapper;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification.SendNotificationStreamConsumeWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.mapper.SendTimelineRegistryEventMapper.*;
import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@ExtendWith(MockitoExtension.class)
class SendTimelineRegistryEventMapperTest {

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

  @BeforeEach
  void setUp() {
    sendTimelineRegistryEventMapper = new SendTimelineRegistryEventMapper(jsonMapperMock);
  }

  @Test
  void testMapSuccess() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = new TimelineElementDetailsV27DTO();
    timelineDetails.setRecIndex(1);
    TimelineElementV27DTO timelineElement = new TimelineElementV27DTO();
    timelineElement.setCategory(TimelineElementCategoryV27DTO.NOTIFICATION_VIEWED);
    timelineElement.setDetails(timelineDetails);
    ProgressResponseElementV28DTO event = new ProgressResponseElementV28DTO();
    event.setEventId("eventId");
    event.setIun("iun");
    event.setNewStatus(NotificationStatusV26DTO.ACCEPTED);
    event.setNotificationRequestId("notificationRequestId");
    event.setElement(timelineElement);

    long organizationId = 1L;
    String streamId = "streamId";
    String expectedRegistryId = String.join(
      "-",
      streamId,
      event.getEventId()
    );
    String traceId = "traceId";
    String workflowId = generateWorkflowId(streamId, SendNotificationStreamConsumeWF.class);

    Mockito.when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, organizationId, streamId, workflowId, traceId);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(traceId, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(organizationId, registryEvent.getOrganizationId());
    Assertions.assertEquals(streamId, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(RegistryOutcome.OK, registryEvent.getOutcome());
    Assertions.assertEquals("serialized", registryEvent.getBody());

    Assertions.assertEquals(timelineDetails.getRecIndex(), registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent);
  }

  @Test
  void testMapError() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = new TimelineElementDetailsV27DTO();
    timelineDetails.setRecIndex(1);
    TimelineElementV27DTO timelineElement = new TimelineElementV27DTO();
    timelineElement.setCategory(TimelineElementCategoryV27DTO.NOTIFICATION_VIEWED);
    timelineElement.setDetails(timelineDetails);
    ProgressResponseElementV28DTO event = new ProgressResponseElementV28DTO();
    event.setEventId("eventId");
    event.setIun("iun");
    event.setNewStatus(NotificationStatusV26DTO.ACCEPTED);
    event.setNotificationRequestId("notificationRequestId");
    event.setElement(timelineElement);

    long organizationId = 1L;
    String streamId = "streamId";
    String expectedRegistryId = String.join(
      "-",
      streamId,
      event.getEventId()
    );
    String traceId = "traceId";
    String workflowId = generateWorkflowId(streamId, SendNotificationStreamConsumeWF.class);

    Mockito.when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapError(event, organizationId, streamId, workflowId, traceId);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(traceId, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(organizationId, registryEvent.getOrganizationId());
    Assertions.assertEquals(streamId, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(RegistryOutcome.KO, registryEvent.getOutcome());
    Assertions.assertEquals("serialized", registryEvent.getBody());

    Assertions.assertEquals(timelineDetails.getRecIndex(), registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent);
  }

  @Test
  void testMapWithException() {
    //GIVEN
    TimelineElementDetailsV27DTO timelineDetails = new TimelineElementDetailsV27DTO();
    timelineDetails.setRecIndex(1);
    TimelineElementV27DTO timelineElement = new TimelineElementV27DTO();
    timelineElement.setCategory(TimelineElementCategoryV27DTO.NOTIFICATION_VIEWED);
    timelineElement.setDetails(timelineDetails);
    ProgressResponseElementV28DTO event = new ProgressResponseElementV28DTO();
    event.setEventId("eventId");
    event.setIun("iun");
    event.setNewStatus(NotificationStatusV26DTO.ACCEPTED);
    event.setNotificationRequestId("notificationRequestId");
    event.setElement(timelineElement);

    long organizationId = 1L;
    String streamId = "streamId";
    String expectedRegistryId = String.join(
      "-",
      streamId,
      event.getEventId()
    );
    String traceId = "traceId";
    String workflowId = generateWorkflowId(streamId, SendNotificationStreamConsumeWF.class);

    Mockito.when(jsonMapperMock.writeValueAsString(Mockito.any()))
      .thenThrow(new RuntimeException());

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapError(event, organizationId, streamId, workflowId, traceId);

    //THEN
    Assertions.assertEquals(expectedRegistryId, registryEvent.getRegistryId());
    Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
    Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
    Assertions.assertNotNull(registryEvent.getDateTime());
    Assertions.assertEquals(traceId, registryEvent.getTraceId());
    Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
    Assertions.assertEquals(REQUESTOR_ID, registryEvent.getRequestorId());
    Assertions.assertEquals(workflowId, registryEvent.getGrantorId());

    Assertions.assertEquals(organizationId, registryEvent.getOrganizationId());
    Assertions.assertEquals(streamId, registryEvent.getStreamId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(event.getNotificationRequestId(), registryEvent.getNotificationRequestId());
    Assertions.assertEquals(event.getIun(), registryEvent.getIun());
    Assertions.assertEquals(event.getNewStatus().name(), registryEvent.getNewStatus());
    Assertions.assertEquals(RegistryOutcome.KO, registryEvent.getOutcome());
    Assertions.assertNull(registryEvent.getBody());

    Assertions.assertEquals(timelineDetails.getRecIndex(), registryEvent.getRecipientIndex());

    TestUtils.checkNotNullFields(registryEvent, "body");
  }
}
