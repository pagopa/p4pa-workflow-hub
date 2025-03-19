package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;

import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO;

public class PaymentOptionFaker {

  public static PaymentOptionDTO buildPaymentOptionDTO() {
    return PaymentOptionDTO.builder()
      .paymentOptionId(1L)
      .debtPositionId(1L)
      .totalAmountCents(1L)
      .status(PaymentOptionDTO.StatusEnum.TO_SYNC)
      .description("description")
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.DOWN_PAYMENT)
      .installments(List.of(buildInstallmentDTO()))
      .build();
  }
}
