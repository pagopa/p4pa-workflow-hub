package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionRequestDTO;

import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentRequestDTO;

public class PaymentOptionFaker {

  public static PaymentOptionDTO buildPaymentOptionDTO() {
    return PaymentOptionDTO.builder()
      .paymentOptionId(1L)
      .totalAmountCents(1L)
      .status("status")
      .multiDebtor(false)
      .dueDate(OFFSET_DATE_TIME)
      .description("description")
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.DOWN_PAYMENT)
      .installments(List.of(buildInstallmentDTO()))
      .build();
  }

  public static PaymentOptionRequestDTO buildPaymentOptionRequestDTO() {
    return PaymentOptionRequestDTO.builder()
      .paymentOptionId(1L)
      .totalAmountCents(1L)
      .status("status")
      .multiDebtor(false)
      .dueDate(OFFSET_DATE_TIME)
      .description("description")
      .paymentOptionType(PaymentOptionRequestDTO.PaymentOptionTypeEnum.DOWN_PAYMENT)
      .installments(List.of(buildInstallmentRequestDTO()))
      .build();
  }
}
