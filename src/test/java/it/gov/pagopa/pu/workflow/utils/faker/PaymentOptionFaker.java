package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.debtposition.PaymentOptionDTO;
import it.gov.pagopa.payhub.activities.enums.PaymentOptionType;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionTypeRequest;

import java.time.LocalDate;
import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;

public class PaymentOptionFaker {

  public static PaymentOptionDTO buildPaymentOptionDTO() {
    return PaymentOptionDTO.builder()
      .org(buildOrganization())
      .totalAmount(100L)
      .status("status")
      .dueDate(LocalDate.of(2024, 5, 15))
      .installments(List.of(buildInstallmentDTO()))
      .multiDebtor(false)
      .paymentOptionType(PaymentOptionType.DOWN_PAYMENT)
      .description("description")
      .build();
  }

  public static PaymentOptionRequestDTO buildPaymentOptionRequestDTO() {
    return PaymentOptionRequestDTO.builder()
      .org(buildOrganizationRequestDTO())
      .totalAmount(100L)
      .status("status")
      .dueDate(LocalDate.of(2024, 5, 15))
      .installments(List.of(buildInstallmentRequestDTO()))
      .multiDebtor(false)
      .paymentOptionType(PaymentOptionTypeRequest.DOWN_PAYMENT)
      .description("description")
      .build();
  }
}
