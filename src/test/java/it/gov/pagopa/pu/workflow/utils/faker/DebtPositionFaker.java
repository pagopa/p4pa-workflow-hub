package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestStatus;

import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;

public class DebtPositionFaker {

  public static DebtPositionDTO buildDebtPositionDTO() {
    return DebtPositionDTO.builder()
      .debtPositionId(1L)
      .iupdOrg("iupdOrg")
      .description("description")
      .status(DebtPositionDTO.StatusEnum.TO_SYNC)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(1L)
      .organizationId(1L)
      .debtPositionTypeOrgId(1L)
      .notificationDate(OFFSET_DATE_TIME)
      .validityDate(OFFSET_DATE_TIME)
      .flagIuvVolatile(false)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .paymentOptions(List.of(buildPaymentOptionDTO()))
      .build();
  }

  public static DebtPositionRequestDTO buildDebtPositionRequestDTO() {
    return DebtPositionRequestDTO.builder()
      .debtPositionId(1L)
      .iupdOrg("iupdOrg")
      .description("description")
      .status(DebtPositionRequestStatus.TO_SYNC)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(1L)
      .organizationId(1L)
      .debtPositionTypeOrgId(1L)
      .notificationDate(OFFSET_DATE_TIME)
      .validityDate(OFFSET_DATE_TIME)
      .flagIuvVolatile(false)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .paymentOptions(List.of(buildPaymentOptionRequestDTO()))
      .build();
  }
}
