package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import it.gov.pagopa.payhub.activities.activity.debtposition.receipt.ReceiptRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.sendnotification.payments.NotifyPaymentActivity;
import it.gov.pagopa.payhub.activities.util.Utilities;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.config.SendNotificationPaymentHandlerWFConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationPaymentHandlerWFTest {

  @Mock
  private ApplicationContext applicationContextMock;

  @Mock
  private SendNotificationPaymentHandlerWFConfig wfConfigMock;

  @Mock
  private NotifyPaymentActivity notifyPaymentActivityMock;

  @Mock
  private ReceiptRetrieverActivity receiptRetrieverActivityMock;

  @Mock
  private PublishSendTimelineEventActivity publishSendTimelineEventActivityMock;

  private SendNotificationPaymentHandlerWFImpl workflow;
  private MockedStatic<Utilities> utilitiesMock;

  private final DebtPositionDTO debtPosition = new DebtPositionDTO();
  private final Long receiptId = 1L;

  private static final String TRACE_ID = "TRACE_ID";

  @BeforeEach
  void setUp() {
    utilitiesMock = Mockito.mockStatic(Utilities.class);
    utilitiesMock.when(Utilities::getTraceId).thenReturn(TRACE_ID);

    when(applicationContextMock.getBean(SendNotificationPaymentHandlerWFConfig.class))
      .thenReturn(wfConfigMock);

    when(wfConfigMock.buildNotifyPaymentActivityStub())
      .thenReturn(notifyPaymentActivityMock);

    when(wfConfigMock.buildReceiptRetrieverActivityStub())
      .thenReturn(receiptRetrieverActivityMock);

    when(wfConfigMock.buildPublishSendTimelineEventActivityStub())
      .thenReturn(publishSendTimelineEventActivityMock);

    workflow = new SendNotificationPaymentHandlerWFImpl();
    workflow.setApplicationContext(applicationContextMock);

    verify(applicationContextMock)
      .getBean(SendNotificationPaymentHandlerWFConfig.class);

    verify(wfConfigMock).buildNotifyPaymentActivityStub();
    verify(wfConfigMock).buildReceiptRetrieverActivityStub();
    verify(wfConfigMock).buildPublishSendTimelineEventActivityStub();
  }

  @AfterEach
  void tearDown() {
    try {
      verifyNoMoreInteractions(
        applicationContextMock,
        wfConfigMock,
        notifyPaymentActivityMock,
        receiptRetrieverActivityMock,
        publishSendTimelineEventActivityMock);
    } finally {
      utilitiesMock.close();
    }
  }

  @Test
  void whenNotifyPaymentReturnsNullThenSkip() {
    // Given
    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(null);

    // When
    workflow.notifyPayment(debtPosition, receiptId);

    // Then
    verify(notifyPaymentActivityMock)
      .notifyPayment(debtPosition, receiptId);

    verifyNoInteractions(
      receiptRetrieverActivityMock,
      publishSendTimelineEventActivityMock);
  }

  @Test
  void whenHistoryIsNullThenSkip() {
    // Given
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setHistory(null);

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenHistoryIsEmptyThenSkip() {
    // Given
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setHistory(List.of());

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenLastHistoryElementIsNullThenSkip() {
    // Given
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setHistory(Collections.singletonList(null));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenLastHistoryStatusIsNotPaidThenSkip() {
    // Given
    NotificationStatusV26DTO otherStatus =
      Arrays.stream(NotificationStatusV26DTO.values())
        .filter(status -> status != NotificationStatusV26DTO.PU_PAID)
        .findFirst()
        .orElseThrow();

    StreamEventSummaryDTO lastEvent = new StreamEventSummaryDTO(
      otherStatus,
      TimelineElementCategoryV27DTO.PU_PAYMENT);

    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(lastEvent));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenLastHistoryCategoryIsNotPaymentThenSkip() {
    // Given
    TimelineElementCategoryV27DTO otherCategory =
      Arrays.stream(TimelineElementCategoryV27DTO.values())
        .filter(category -> category != TimelineElementCategoryV27DTO.PU_PAYMENT)
        .findFirst()
        .orElseThrow();

    StreamEventSummaryDTO lastEvent = new StreamEventSummaryDTO(
      NotificationStatusV26DTO.PU_PAID,
      otherCategory);

    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(lastEvent));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenPreviousHistoryIsPaidButLastIsNotThenSkip() {
    // Given
    StreamEventSummaryDTO paidEvent = buildPaidEvent();

    StreamEventSummaryDTO lastEvent =
      new StreamEventSummaryDTO(null,
        TimelineElementCategoryV27DTO.PU_PAYMENT);

    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(paidEvent, lastEvent));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenLastHistoryIsPaidThenPublishTimelineEvent() {
    // Given
    OffsetDateTime paymentDateTime = OffsetDateTime.parse("2026-10-16T10:00:00+02:00");

    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(
        new StreamEventSummaryDTO(),
        buildPaidEvent()));

    ReceiptDTO receipt = new ReceiptDTO();
    receipt.setPaymentDateTime(paymentDateTime);

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    when(receiptRetrieverActivityMock.retrieveReceipt(receiptId))
      .thenReturn(receipt);

    // When
    workflow.notifyPayment(debtPosition, receiptId);

    // Then
    InOrder inOrder = inOrder(
      notifyPaymentActivityMock,
      receiptRetrieverActivityMock,
      publishSendTimelineEventActivityMock);

    inOrder.verify(notifyPaymentActivityMock)
      .notifyPayment(debtPosition, receiptId);

    inOrder.verify(receiptRetrieverActivityMock)
      .retrieveReceipt(receiptId);

    inOrder.verify(publishSendTimelineEventActivityMock)
      .publishPaymentTimelineEvent(
        sendNotificationDTO,
        paymentDateTime,
        TRACE_ID);
  }

  @Test
  void whenPaymentDateTimeIsNullThenPublishTimelineEvent() {
    // Given
    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(buildPaidEvent()));

    ReceiptDTO receipt = new ReceiptDTO();
    receipt.setPaymentDateTime(null);

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    when(receiptRetrieverActivityMock.retrieveReceipt(receiptId))
      .thenReturn(receipt);

    // When
    workflow.notifyPayment(debtPosition, receiptId);

    // Then
    verify(notifyPaymentActivityMock)
      .notifyPayment(debtPosition, receiptId);

    verify(receiptRetrieverActivityMock)
      .retrieveReceipt(receiptId);

    verify(publishSendTimelineEventActivityMock)
      .publishPaymentTimelineEvent(
        sendNotificationDTO,
        null,
        TRACE_ID);
  }

  @Test
  void whenReceiptRetrievalFailsThenDoNotPublish() {
    // Given
    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(buildPaidEvent()));

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    when(receiptRetrieverActivityMock.retrieveReceipt(receiptId))
      .thenThrow(new IllegalStateException("Receipt not available"));

    // When & Then
    assertThrows(
      IllegalStateException.class,
      () -> workflow.notifyPayment(debtPosition, receiptId));

    verify(notifyPaymentActivityMock)
      .notifyPayment(debtPosition, receiptId);

    verify(receiptRetrieverActivityMock)
      .retrieveReceipt(receiptId);

    verifyNoInteractions(publishSendTimelineEventActivityMock);
  }

  private void verifySkippedPublication(
    SendNotificationDTO sendNotificationDTO) {

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    workflow.notifyPayment(debtPosition, receiptId);

    verify(notifyPaymentActivityMock)
      .notifyPayment(debtPosition, receiptId);

    verifyNoInteractions(
      receiptRetrieverActivityMock,
      publishSendTimelineEventActivityMock);
  }

  private SendNotificationDTO buildSendNotificationDTO(
    List<StreamEventSummaryDTO> history) {

    SendNotificationDTO dto = new SendNotificationDTO();
    dto.setHistory(history);
    return dto;
  }

  private StreamEventSummaryDTO buildPaidEvent() {
    return new StreamEventSummaryDTO(
      NotificationStatusV26DTO.PU_PAID,
      TimelineElementCategoryV27DTO.PU_PAYMENT);
  }
}

