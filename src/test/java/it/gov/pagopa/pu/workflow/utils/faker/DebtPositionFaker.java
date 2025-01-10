package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;

import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileFaker.buildIngestionFlowFileDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileFaker.buildIngestionFlowFileRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;

public class DebtPositionFaker {

  public static DebtPositionDTO buildDebtPositionDTO() {
    return DebtPositionDTO.builder()
      .debtPositionId(1L)
      .iupdOrg("codeIud")
      .iupdPagopa("gpdIupd")
      .description("description")
      .status("statusCode")
      .ingestionFlowFile(buildIngestionFlowFileDTO())
      .ingestionFlowFileLineNumber(1L)
      .gpdStatus('G')
      .org(buildOrganization())
      .debtPositionTypeOrg(buildDebtPositionTypeOrgDTO())
      .paymentOptions(List.of(buildPaymentOptionDTO()))
      .build();
  }

  public static DebtPositionRequestDTO buildDebtPositionRequestDTO() {
    return DebtPositionRequestDTO.builder()
      .debtPositionId(1L)
      .iupdOrg("codeIud")
      .iupdPagopa("gpdIupd")
      .description("description")
      .status("statusCode")
      .ingestionFlowFile(buildIngestionFlowFileRequestDTO())
      .ingestionFlowFileLineNumber(1L)
      .gpdStatus("G")
      .org(buildOrganizationRequestDTO())
      .debtPositionTypeOrg(buildDebtPositionTypeOrgRequestDTO())
      .paymentOptions(List.of(buildPaymentOptionRequestDTO()))
      .build();
  }
}
