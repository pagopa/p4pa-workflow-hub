package it.gov.pagopa.pu.workflow.event.payments.consumer;

import it.gov.pagopa.pu.debtposition.dto.generated.*;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.dto.DebtPositionEventDTO;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker;
import it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker;
import it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker;
import it.gov.pagopa.pu.workflow.wf.assessments.CreateAssessmentsWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PaymentsConsumerTest {

  @Mock
  private IudClassificationWFClient wfClientMock;
  @Mock
  private CreateAssessmentsWFClient createAssessmentsWFClientMock;

  private PaymentsConsumer paymentsConsumer;

  @BeforeEach
  void init() {
    this.paymentsConsumer = new PaymentsConsumer(wfClientMock, createAssessmentsWFClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(wfClientMock, createAssessmentsWFClientMock);
  }

  @Test
  void givenExpectedEventWhenAcceptThenInvokeClient() {
    // Given
    DebtPositionEventDTO paymentEventDTO = DebtPositionEventDTO.builder()
      .eventId("EVENTID")
      .payload(buildPaidDebtPosition())
      .eventType(PaymentEventType.RT_RECEIVED)
      .eventDescription("receiptId:2")
      .build();

    // When
    paymentsConsumer.accept(paymentEventDTO);

    // Then
    Mockito.verify(wfClientMock)
      .notifyReceipt(new IudClassificationNotifyReceiptSignalDTO(1L, "iud1", "iuv1", "iur1", Collections.singletonList(1)));
    Mockito.verify(wfClientMock)
      .notifyReceipt(new IudClassificationNotifyReceiptSignalDTO(1L, "iud3", "iuv3", "iur2", Collections.singletonList(1)));
    Mockito.verify(wfClientMock)
      .notifyReceipt(new IudClassificationNotifyReceiptSignalDTO(1L, "iud5", "iuv5", "iur1",  Collections.singletonList(1)));
    Mockito.verify(createAssessmentsWFClientMock)
      .createAssessments(2L);
  }

  @Test
  void givenExpectedEventWithUncorrectReceiptIdWhenAcceptThenInvokeClient() {
    // Given
    DebtPositionEventDTO paymentEventDTO = DebtPositionEventDTO.builder()
      .eventId("EVENTID")
      .payload(buildPaidDebtPosition())
      .eventType(PaymentEventType.RT_RECEIVED)
      .eventDescription("receiptId:undefined")
      .build();

    // When
    paymentsConsumer.accept(paymentEventDTO);

    // Then
    Mockito.verify(wfClientMock)
      .notifyReceipt(new IudClassificationNotifyReceiptSignalDTO(1L, "iud1", "iuv1", "iur1",  Collections.singletonList(1)));
    Mockito.verify(wfClientMock)
      .notifyReceipt(new IudClassificationNotifyReceiptSignalDTO(1L, "iud3", "iuv3", "iur2",  Collections.singletonList(1)));
    Mockito.verify(wfClientMock)
      .notifyReceipt(new IudClassificationNotifyReceiptSignalDTO(1L, "iud5", "iuv5", "iur1",  Collections.singletonList(1)));
    Mockito.verify(createAssessmentsWFClientMock)
      .createAssessments(1L);
  }

  @Test
  void givenNotHandledEventWhenAcceptThenNoAction() {
    // Given
    DebtPositionEventDTO paymentEventDTO = DebtPositionEventDTO.builder()
      .eventId("EVENTID")
      .payload(buildPaidDebtPosition())
      .eventType(PaymentEventType.SYNC_ERROR)
      .build();

    // When
    paymentsConsumer.accept(paymentEventDTO);

    Mockito.verifyNoInteractions(wfClientMock);
  }

  @Test
  void givenRtReceivedAndWrongPayloadWhenAcceptThenNoAction() {
    // Given
    PaymentEventDTO<?> paymentEventDTO = PaymentEventDTO.builder()
      .eventId("EVENTID")
      .payload(new Object())
      .eventType(PaymentEventType.RT_RECEIVED)
      .build();

    // When
    paymentsConsumer.accept(paymentEventDTO);

    Mockito.verifyNoInteractions(wfClientMock);
  }

  private DebtPositionDTO buildPaidDebtPosition() {
    DebtPositionDTO out = DebtPositionFaker.buildDebtPositionDTO();
    out.setPaymentOptions(List.of(
      buildPaymentOption(List.of(
        buildInstallment(InstallmentStatus.PAID, "iuv1", "iur1", "iud1"),
        buildInstallment(InstallmentStatus.UNPAID, "iuv2", null, "iud2"),
        buildInstallment(InstallmentStatus.PAID, "iuv3", "iur2", "iud3")
      )),
      buildPaymentOption(List.of(
        buildInstallment(InstallmentStatus.UNPAID, "iuv4", null, "iud4"),
        buildInstallment(InstallmentStatus.PAID, "iuv5", "iur1", "iud5")
      ))
    ));
    return out;
  }

  private static PaymentOptionDTO buildPaymentOption(List<InstallmentDTO> installments) {
    PaymentOptionDTO out = PaymentOptionFaker.buildPaymentOptionDTO();
    out.setInstallments(installments);
    return out;
  }

  private static InstallmentDTO buildInstallment(InstallmentStatus status, String iuv, String iur, String iud) {
    InstallmentDTO out = InstallmentFaker.buildInstallmentDTO();
    out.setStatus(status);
    out.setIuv(iuv);
    out.setIur(iur);
    out.setIud(iud);
    out.setTransfers(List.of(
      TransferDTO.builder()
        .orgFiscalCode("FC_ORG1")
        .orgName("ORG1")
        .transferIndex(1)
        .amountCents(10_00L)
        .remittanceInformation("REMITTANCE_" + iuv)
        .category("CATEGORY")
        .build()
    ));
    return out;
  }
}
