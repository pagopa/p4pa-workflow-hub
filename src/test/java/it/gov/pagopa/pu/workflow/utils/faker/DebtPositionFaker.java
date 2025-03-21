package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionStatus;

import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;

public class DebtPositionFaker {

  public static DebtPositionDTO buildDebtPositionDTO() {
    return DebtPositionDTO.builder()
      .debtPositionId(1L)
      .iupdOrg("iupdOrg")
      .description("description")
      .status(DebtPositionStatus.TO_SYNC)
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY)
      .organizationId(1L)
      .debtPositionTypeOrgId(1L)
      .validityDate(DATE)
      .flagIuvVolatile(false)
      .multiDebtor(false)
      .flagPagoPaPayment(false)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .paymentOptions(List.of(buildPaymentOptionDTO()))
      .build();
  }
}
