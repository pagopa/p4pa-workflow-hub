package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.OrganizationDTO;
import it.gov.pagopa.pu.workflow.dto.generated.OrganizationRequestDTO;

import java.time.LocalDate;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;

public class OrganizationFaker {

    public static OrganizationDTO buildOrganizationDTO() {
        return OrganizationDTO.builder()
                .orgId(1L)
                .ipaCode("ipaCode")
                .orgFiscalCode("orgFiscalCode")
                .orgName("orgName")
                .adminEmail("adminEmail")
                .creationDate(DATE.toInstant())
                .lastUpdateDate(DATE.toInstant())
                .fee(500L)
                .iban("iban")
                .urlOrgSendSILPaymentResult("urlOrgSendSILPaymentResult")
                .password("password")
                .creditBicSeller(true)
                .beneficiaryOrgName("beneficiaryOrgName")
                .beneficiaryOrgAddress("beneficiaryOrgAddress")
                .beneficiaryOrgCivic("beneficiaryOrgCivic")
                .beneficiaryOrgPostalCode("beneficiaryOrgPostalCode")
                .beneficiaryOrgLocation("beneficiaryOrgLocation")
                .beneficiaryOrgProvince("beneficiaryOrgProvince")
                .beneficiaryOrgNation("beneficiaryOrgNation")
                .beneficiaryOrgPhoneNumber("beneficiaryOrgPhoneNumber")
                .beneficiaryOrgWebSite("beneficiaryOrgWebSite")
                .beneficiaryOrgEmail("beneficiaryOrgEmail")
                .applicationCode("applicationCode")
                .cbillInterbankCode("cbillInterbankCode")
                .orgInformation("orgInformation")
                .orgLogoDesc("orgLogoDesc")
                .authorizationDesc("authorizationDesc")
                .status("status")
                .urlActiveExternal("urlActiveExternal")
                .additionalLanguage("additionalLanguage")
                .orgTypeCode("orgTypeCode")
                .startDate(LocalDate.of(2024, 1, 1))
                .brokerId(2L)
                .build();
    }

  public static OrganizationRequestDTO buildOrganizationRequestDTO() {
    return OrganizationRequestDTO.builder()
      .orgId(1L)
      .ipaCode("ipaCode")
      .orgFiscalCode("orgFiscalCode")
      .orgName("orgName")
      .adminEmail("adminEmail")
      .creationDate(OFFSET_DATE_TIME)
      .lastUpdateDate(OFFSET_DATE_TIME)
      .fee(500L)
      .iban("iban")
      .urlOrgSendSILPaymentResult("urlOrgSendSILPaymentResult")
      .password("password")
      .creditBicSeller(true)
      .beneficiaryOrgName("beneficiaryOrgName")
      .beneficiaryOrgAddress("beneficiaryOrgAddress")
      .beneficiaryOrgCivic("beneficiaryOrgCivic")
      .beneficiaryOrgPostalCode("beneficiaryOrgPostalCode")
      .beneficiaryOrgLocation("beneficiaryOrgLocation")
      .beneficiaryOrgProvince("beneficiaryOrgProvince")
      .beneficiaryOrgNation("beneficiaryOrgNation")
      .beneficiaryOrgPhoneNumber("beneficiaryOrgPhoneNumber")
      .beneficiaryOrgWebSite("beneficiaryOrgWebSite")
      .beneficiaryOrgEmail("beneficiaryOrgEmail")
      .applicationCode("applicationCode")
      .cbillInterbankCode("cbillInterbankCode")
      .orgInformation("orgInformation")
      .orgLogoDesc("orgLogoDesc")
      .authorizationDesc("authorizationDesc")
      .status("status")
      .urlActiveExternal("urlActiveExternal")
      .additionalLanguage("additionalLanguage")
      .orgTypeCode("orgTypeCode")
      .startDate(LocalDate.of(2024, 1, 1))
      .brokerId(2L)
      .build();
  }
}
