package it.gov.pagopa.pu.workflow.event.consumer.payments;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflow.event.consumer.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker;
import it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker;
import it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PaymentsConsumerTest {

  @Mock
  private TransferClassificationWFClient wfClientMock;

  private PaymentsConsumer paymentsConsumer;

  @BeforeEach
  void init() {
    this.paymentsConsumer = new PaymentsConsumer(wfClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(wfClientMock);
  }

  @Test
  void givenExpectedEventWhenAcceptThenInvokeClient() {
    // Given
    PaymentEventDTO paymentEventDTO = new PaymentEventDTO(
      buildPaidDebtPosition(),
      "RT_RECEIVED"
    );

    // When
    paymentsConsumer.accept(paymentEventDTO);

    // Then
    Mockito.verify(wfClientMock)
      .classify(1L, "iuv1", "iur1", 1);
    Mockito.verify(wfClientMock)
      .classify(1L, "iuv3", "iur2", 1);
    Mockito.verify(wfClientMock)
      .classify(1L, "iuv5", "iur1", 1);
  }

  @Test
  void givenNotHandledEventWhenAcceptThenInvokeClient() {
    // Given
    PaymentEventDTO paymentEventDTO = new PaymentEventDTO(
      buildPaidDebtPosition(),
      "NOTHANDLED"
    );

    // When
    paymentsConsumer.accept(paymentEventDTO);

    Mockito.verifyNoInteractions(wfClientMock);
  }

  private DebtPositionDTO buildPaidDebtPosition() {
    DebtPositionDTO out = DebtPositionFaker.buildDebtPositionDTO();
    out.setPaymentOptions(List.of(
      buildPaymentOption(List.of(
        buildInstallment(InstallmentDTO.StatusEnum.PAID, "iuv1", "iur1"),
        buildInstallment(InstallmentDTO.StatusEnum.UNPAID, "iuv2", null),
        buildInstallment(InstallmentDTO.StatusEnum.PAID, "iuv3", "iur2")
      )),
      buildPaymentOption(List.of(
        buildInstallment(InstallmentDTO.StatusEnum.UNPAID, "iuv4", null),
        buildInstallment(InstallmentDTO.StatusEnum.PAID, "iuv5", "iur1")
      ))
    ));
    return out;
  }

  private static PaymentOptionDTO buildPaymentOption(List<InstallmentDTO> installments) {
    PaymentOptionDTO out = PaymentOptionFaker.buildPaymentOptionDTO();
    out.setInstallments(installments);
    return out;
  }

  private static InstallmentDTO buildInstallment(InstallmentDTO.StatusEnum status, String iuv, String iur) {
    InstallmentDTO out = InstallmentFaker.buildInstallmentDTO();
    out.setStatus(status);
    out.setIuv(iuv);
    out.setIur(iur);
    out.setTransfers(List.of(
      TransferDTO.builder()
        .orgFiscalCode("FC_ORG1")
        .orgName("ORG1")
        .transferIndex(1L)
        .amountCents(10_00L)
        .remittanceInformation("REMITTANCE_" + iuv)
        .category("CATEGORY")
        .build()
    ));
    return out;
  }
}
