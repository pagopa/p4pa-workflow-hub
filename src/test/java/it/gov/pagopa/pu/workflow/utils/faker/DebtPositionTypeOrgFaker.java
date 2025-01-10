package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionTypeOrgRequestDTO;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeFaker.buildDebtPositionTypeRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;

public class DebtPositionTypeOrgFaker {

    public static DebtPositionTypeOrgDTO buildDebtPositionTypeOrgDTO() {
        return DebtPositionTypeOrgDTO.builder()
                .debtPositionTypeOrgId(1L)
                .org(buildOrganizationDTO())
                .debtPositionType(DebtPositionTypeFaker.buildDebtPositionType())
                .balance("balance")
                .code("code")
                .description("description")
                .postalIban("postalIban")
                .iban("iban")
                .postalAccountCode("1234567890")
                .xsdDefinitionRef("xsdDefinitionRef")
                .amount(100L)
                .externalPaymentUrl("externalPaymentUrl")
                .balanceDefaultDesc("balanceDefaultDesc")
                .flagAnonymousFiscalCode(false)
                .flagMandatoryDueDate(false)
                .holderPostalCC("holderPostalCC")
                .orgSector("orgSector")
                .flagNotifyIO(true)
                .flagNotifyOutcomePush(false)
                .maxAttemptForwardingOutcome(3)
                .orgSilId(2L)
                .flagActive(true)
                .taxonomyCode("taxonomyCode")
                .amountActualizationUrl("amountActualizationUrl")
                .amountActualizationUser("amountActualizationUser")
                .amountActualizationPwd("amountActualizationPwd")
                .urlNotifyActualizationPnd("urlNotifyActualizationPnd")
                .flagDisablePrintNotice(true)
                .build();
    }

  public static DebtPositionTypeOrgRequestDTO buildDebtPositionTypeOrgRequestDTO() {
    return DebtPositionTypeOrgRequestDTO.builder()
      .debtPositionTypeOrgId(1L)
      .org(buildOrganizationRequestDTO())
      .debtPositionType(buildDebtPositionTypeRequestDTO())
      .balance("balance")
      .code("code")
      .description("description")
      .postalIban("postalIban")
      .iban("iban")
      .postalAccountCode("1234567890")
      .xsdDefinitionRef("xsdDefinitionRef")
      .amount(100L)
      .externalPaymentUrl("externalPaymentUrl")
      .balanceDefaultDesc("balanceDefaultDesc")
      .flagAnonymousFiscalCode(false)
      .flagMandatoryDueDate(false)
      .holderPostalCC("holderPostalCC")
      .orgSector("orgSector")
      .flagNotifyIO(true)
      .flagNotifyOutcomePush(false)
      .maxAttemptForwardingOutcome(3)
      .orgSilId(2L)
      .flagActive(true)
      .taxonomyCode("taxonomyCode")
      .amountActualizationUrl("amountActualizationUrl")
      .amountActualizationUser("amountActualizationUser")
      .amountActualizationPwd("amountActualizationPwd")
      .urlNotifyActualizationPnd("urlNotifyActualizationPnd")
      .flagDisablePrintNotice(true)
      .build();
  }
}
