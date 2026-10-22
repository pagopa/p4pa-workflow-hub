
package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.wf;

import it.gov.pagopa.payhub.activities.activity.sendnotification.payments.NotifyPaymentActivity;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.*;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.config.SendNotificationPaymentHandlerWFConfig;
import it.gov.pagopa.pu.workflow.wf.pagopa.send.stream.activity.PublishSendTimelineEventActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

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
  private PublishSendTimelineEventActivity publishSendTimelineEventActivityMock;

  private SendNotificationPaymentHandlerWFImpl workflow;

  private final DebtPositionDTO debtPosition = new DebtPositionDTO();
  private final Long receiptId = 1L;

  @BeforeEach
  void setUp() {
    when(applicationContextMock.getBean(SendNotificationPaymentHandlerWFConfig.class))
      .thenReturn(wfConfigMock);

    when(wfConfigMock.buildNotifyPaymentActivityStub())
      .thenReturn(notifyPaymentActivityMock);

    when(wfConfigMock.buildPublishSendTimelineEventActivityStub())
      .thenReturn(publishSendTimelineEventActivityMock);

    workflow = new SendNotificationPaymentHandlerWFImpl();
    workflow.setApplicationContext(applicationContextMock);

    verify(applicationContextMock).getBean(SendNotificationPaymentHandlerWFConfig.class);
    verify(wfConfigMock).buildNotifyPaymentActivityStub();
    verify(wfConfigMock).buildPublishSendTimelineEventActivityStub();
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      applicationContextMock,
      wfConfigMock,
      notifyPaymentActivityMock,
      publishSendTimelineEventActivityMock
    );
  }

  @Test
  void whenNotifyPaymentReturnsNullThenSkip() {
    // Given
    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(null);

    // When
    workflow.notifyPayment(debtPosition, receiptId);

    // Then
    verify(notifyPaymentActivityMock).notifyPayment(debtPosition, receiptId);
    verifyNoInteractions(publishSendTimelineEventActivityMock);
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
  void whenHistoryContainsOnlyNullThenSkip() {
    // Given
    SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
    sendNotificationDTO.setHistory(Collections.singletonList(null));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenHistoryDoesNotContainPaidStatusThenSkip() {
    // Given
    NotificationStatusV26DTO otherStatus =
      Arrays.stream(NotificationStatusV26DTO.values())
        .filter(status -> status != NotificationStatusV26DTO.PU_PAID)
        .findFirst()
        .orElseThrow();

    StreamEventSummaryDTO lastEvent = new StreamEventSummaryDTO(otherStatus, TimelineElementCategoryV27DTO.PU_PAYMENT);
    SendNotificationDTO sendNotificationDTO = buildSendNotificationDTO(List.of(lastEvent));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenHistoryDoesNotContainPaymentCategoryThenSkip() {
    // Given
    TimelineElementCategoryV27DTO otherCategory =
      Arrays.stream(TimelineElementCategoryV27DTO.values())
        .filter(category -> category != TimelineElementCategoryV27DTO.PU_PAYMENT)
        .findFirst()
        .orElseThrow();

    StreamEventSummaryDTO lastEvent = new StreamEventSummaryDTO(NotificationStatusV26DTO.PU_PAID, otherCategory);
    SendNotificationDTO sendNotificationDTO = buildSendNotificationDTO(List.of(lastEvent));

    // When & Then
    verifySkippedPublication(sendNotificationDTO);
  }

  @Test
  void whenPreviousHistoryIsPaidButLastIsNotThenPublishTimelineEvent() {
    // Given
    StreamEventSummaryDTO paidEvent = buildPaidEvent();
    StreamEventSummaryDTO lastEvent = new StreamEventSummaryDTO(null, TimelineElementCategoryV27DTO.PU_PAYMENT);

    SendNotificationDTO sendNotificationDTO = buildSendNotificationDTO(List.of(paidEvent, lastEvent));

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    // When
    workflow.notifyPayment(debtPosition, receiptId);

    // Then
    verify(notifyPaymentActivityMock).notifyPayment(debtPosition, receiptId);
    verify(publishSendTimelineEventActivityMock).publishPaymentTimelineEvent(sendNotificationDTO, receiptId);
  }

  @Test
  void whenHistoryContainsPaidEventThenPublishTimelineEvent() {
    // Given
    SendNotificationDTO sendNotificationDTO = buildSendNotificationDTO(List.of(new StreamEventSummaryDTO(), buildPaidEvent()));

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    // When
    workflow.notifyPayment(debtPosition, receiptId);

    // Then
    InOrder inOrder = inOrder(notifyPaymentActivityMock, publishSendTimelineEventActivityMock);

    inOrder.verify(notifyPaymentActivityMock)
      .notifyPayment(debtPosition, receiptId);

    inOrder.verify(publishSendTimelineEventActivityMock)
      .publishPaymentTimelineEvent(sendNotificationDTO, receiptId);

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void whenPublishTimelineEventFailsThenPropagateException() {
    // Given
    SendNotificationDTO sendNotificationDTO =
      buildSendNotificationDTO(List.of(buildPaidEvent()));

    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    doThrow(new IllegalStateException("Timeline publication failed"))
      .when(publishSendTimelineEventActivityMock)
      .publishPaymentTimelineEvent(sendNotificationDTO, receiptId);

    // When & Then
    assertThrows(IllegalStateException.class, () -> workflow.notifyPayment(debtPosition, receiptId));

    verify(notifyPaymentActivityMock).notifyPayment(debtPosition, receiptId);
    verify(publishSendTimelineEventActivityMock).publishPaymentTimelineEvent(sendNotificationDTO, receiptId);
  }

  private void verifySkippedPublication(SendNotificationDTO sendNotificationDTO) {
    when(notifyPaymentActivityMock.notifyPayment(debtPosition, receiptId))
      .thenReturn(sendNotificationDTO);

    workflow.notifyPayment(debtPosition, receiptId);

    verify(notifyPaymentActivityMock).notifyPayment(debtPosition, receiptId);
    verifyNoInteractions(publishSendTimelineEventActivityMock);
  }

  private SendNotificationDTO buildSendNotificationDTO(
    List<StreamEventSummaryDTO> history) {

    SendNotificationDTO dto = new SendNotificationDTO();
    dto.setHistory(history);
    return dto;
  }

  private StreamEventSummaryDTO buildPaidEvent() {
    return new StreamEventSummaryDTO(NotificationStatusV26DTO.PU_PAID, TimelineElementCategoryV27DTO.PU_PAYMENT);
  }
}
