package it.gov.pagopa.pu.workflow.mapper;

import it.gov.pagopa.payhub.activities.connector.debtposition.ReceiptService;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import org.mockito.MockedStatic;
import tools.jackson.databind.json.JsonMapper;
import it.gov.pagopa.pu.registries.dto.generated.RegistryEventSubType;
import it.gov.pagopa.pu.registries.dto.generated.RegistryOutcome;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.event.registries.dto.RegistryEventSendTimelineDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.wf.SendNotificationStreamConsumerWF;
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
import java.util.UUID;

import static it.gov.pagopa.pu.workflow.mapper.SendTimelineRegistryEventMapper.*;
import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendTimelineRegistryEventMapperTest {

  private static final String EVENT_ID = "eventId";
  private static final String IUN = "iun";
  private static final String NOTIFICATION_REQUEST_ID = "notificationRequestId";
  private static final String STREAM_ID = "streamId";
  private static final String TRACE_ID = "traceId";
  private static final long ORGANIZATION_ID = 1L;
  private static final String CAMPAIGN_ID = "campaignId";
  private static final String SEND_NOTIFICATION_ID = "sendNotificationId";

  @Mock
  private JsonMapper jsonMapperMock;
  @Mock
  private ReceiptService receiptServiceMock;

  @InjectMocks
  private SendTimelineRegistryEventMapper sendTimelineRegistryEventMapper;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      jsonMapperMock,
      receiptServiceMock
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

  private SendNotificationDTO buildSendNotification() {
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setOrganizationId(ORGANIZATION_ID);
    sendNotificationDTO.setCampaignId(CAMPAIGN_ID);
    sendNotificationDTO.setSendNotificationId(SEND_NOTIFICATION_ID);
    return sendNotificationDTO;
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
    SendNotificationDTO sendNotification = buildSendNotification();

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumerWF.class);

    when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, sendNotification, STREAM_ID, workflowId, TRACE_ID);

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
    Assertions.assertEquals(CAMPAIGN_ID, registryEvent.getCampaignId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(SEND_NOTIFICATION_ID, registryEvent.getSendNotificationId());
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
    SendNotificationDTO sendNotification = buildSendNotification();

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumerWF.class);

    when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapError(event, sendNotification, STREAM_ID, workflowId, TRACE_ID);

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
    Assertions.assertEquals(CAMPAIGN_ID, registryEvent.getCampaignId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(SEND_NOTIFICATION_ID, registryEvent.getSendNotificationId());
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
    SendNotificationDTO sendNotification = buildSendNotification();

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumerWF.class);

    when(jsonMapperMock.writeValueAsString(Mockito.any()))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, sendNotification, STREAM_ID, workflowId, TRACE_ID);

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
    Assertions.assertEquals(CAMPAIGN_ID, registryEvent.getCampaignId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(SEND_NOTIFICATION_ID, registryEvent.getSendNotificationId());
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
    SendNotificationDTO sendNotification = buildSendNotification();

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumerWF.class);

    when(jsonMapperMock.writeValueAsString(timelineElement))
      .thenReturn("serialized");

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, sendNotification, STREAM_ID, workflowId, TRACE_ID);

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
    Assertions.assertEquals(CAMPAIGN_ID, registryEvent.getCampaignId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(SEND_NOTIFICATION_ID, registryEvent.getSendNotificationId());
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
    SendNotificationDTO sendNotification = buildSendNotification();

    String expectedRegistryId = String.join(
      "-",
      STREAM_ID,
      event.getEventId()
    );
    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumerWF.class);

    when(jsonMapperMock.writeValueAsString(Mockito.any()))
      .thenThrow(new RuntimeException());

    //WHEN
    RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapSuccess(event, sendNotification, STREAM_ID, workflowId, TRACE_ID);

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
    Assertions.assertEquals(CAMPAIGN_ID, registryEvent.getCampaignId());
    Assertions.assertEquals(event.getEventId(), registryEvent.getEventId());
    Assertions.assertEquals(event.getElement().getCategory(), registryEvent.getEventType());
    Assertions.assertEquals(SEND_NOTIFICATION_ID, registryEvent.getSendNotificationId());
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


  @Test
  void testMapPayment() {
    // GIVEN
    SendNotificationDTO sendNotification = buildSendNotification();
    sendNotification.setNotificationRequestId(NOTIFICATION_REQUEST_ID);
    sendNotification.setIun(IUN);

    Long receiptId = 1L;
    OffsetDateTime paymentDate = OffsetDateTime.now();

    ReceiptDTO receipt = new ReceiptDTO();
    receipt.setPaymentDateTime(paymentDate);

    String workflowId = generateWorkflowId(STREAM_ID, SendNotificationStreamConsumerWF.class);

    when(receiptServiceMock.getByReceiptId(receiptId))
      .thenReturn(receipt);

    try (MockedStatic<Utilities> utilitiesMock = Mockito.mockStatic(Utilities.class)) {

      utilitiesMock.when(Utilities::getTraceId)
        .thenReturn("test-trace-id");

      // WHEN
      RegistryEventSendTimelineDTO registryEvent = sendTimelineRegistryEventMapper.mapPayment(sendNotification, receiptId, workflowId);

      // THEN
      Assertions.assertNotNull(registryEvent.getRegistryId());
      Assertions.assertEquals(registryEvent.getRegistryId(), UUID.fromString(registryEvent.getRegistryId()).toString());
      Assertions.assertEquals(REGISTRY_ORIGIN, registryEvent.getRegistryOrigin());
      Assertions.assertEquals(REGISTRY_SEND, registryEvent.getRegistryType());
      Assertions.assertNotNull(registryEvent.getDateTime());
      Assertions.assertEquals("test-trace-id", registryEvent.getTraceId());
      Assertions.assertEquals(RegistryEventSubType.RESP, registryEvent.getEventSubType());
      Assertions.assertEquals(REQUESTOR_ID_PAYMENT, registryEvent.getRequestorId());
      Assertions.assertEquals(workflowId, registryEvent.getGrantorId());
      Assertions.assertEquals(ORGANIZATION_ID, registryEvent.getOrganizationId());
      Assertions.assertEquals(String.valueOf(ORGANIZATION_ID), registryEvent.getStreamId());
      Assertions.assertEquals(SEND_NOTIFICATION_ID, registryEvent.getEventId());
      Assertions.assertEquals(TimelineElementCategoryV27DTO.PU_PAYMENT, registryEvent.getEventType());
      Assertions.assertEquals(NOTIFICATION_REQUEST_ID, registryEvent.getNotificationRequestId());
      Assertions.assertEquals(IUN, registryEvent.getIun());
      Assertions.assertNull(registryEvent.getRecipientIndex());
      Assertions.assertEquals(NotificationStatusV26DTO.PU_PAID.name(), registryEvent.getNewStatus());
      Assertions.assertEquals(paymentDate, registryEvent.getEventTimestamp());
      Assertions.assertNull(registryEvent.getLegalFactIds());
      Assertions.assertEquals(RegistryOutcome.OK, registryEvent.getOutcome());
      Assertions.assertEquals(registryEvent.getBody(), "INTERNAL_EVENT");

      verify(receiptServiceMock).getByReceiptId(receiptId);
    }
  }
}
