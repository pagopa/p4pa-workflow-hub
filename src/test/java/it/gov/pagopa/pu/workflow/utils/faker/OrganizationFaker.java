package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.organization.dto.generated.Link;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.dto.generated.LinkRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.OrganizationRequestDTO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.LinkFaker.buildLink;
import static it.gov.pagopa.pu.workflow.utils.faker.LinkFaker.buildLinkRequestDTO;

public class OrganizationFaker {

  public static Organization buildOrganization() {
    Map<String, Link> links = new HashMap<>();
    links.put("key", buildLink());
    return Organization.builder()
      .organizationId(1L)
      .ipaCode("ipaCode")
      .orgFiscalCode("orgFiscalCode")
      .orgName("orgName")
      .adminEmail("adminEmail")
      .creationDate(OFFSET_DATE_TIME)
      .lastUpdateDate(OFFSET_DATE_TIME)
      .fee(500L)
      .iban("iban")
      .urlOrgSendSilPaymentResult("urlOrgSendSILPaymentResult")
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
      .cbillInterBankCode("cbillInterbankCode")
      .orgInformation("orgInformation")
      .orgLogoDesc("orgLogoDesc")
      .authorizationDesc("authorizationDesc")
      .status("status")
      .urlActiveExternal("urlActiveExternal")
      .additionalLanguage("additionalLanguage")
      .orgTypeCode("orgTypeCode")
      .startDate(LocalDate.of(2024, 1, 1))
      .brokerId(2L)
      .links(links)
      .build();
  }

  public static OrganizationRequestDTO buildOrganizationRequestDTO() {
    Map<String, LinkRequestDTO> links = new HashMap<>();
    links.put("key", buildLinkRequestDTO());
    return OrganizationRequestDTO.builder()
      .organizationId(1L)
      .ipaCode("ipaCode")
      .orgFiscalCode("orgFiscalCode")
      .orgName("orgName")
      .adminEmail("adminEmail")
      .creationDate(OFFSET_DATE_TIME)
      .lastUpdateDate(OFFSET_DATE_TIME)
      .fee(500L)
      .iban("iban")
      .urlOrgSendSilPaymentResult("urlOrgSendSILPaymentResult")
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
      .cbillInterBankCode("cbillInterbankCode")
      .orgInformation("orgInformation")
      .orgLogoDesc("orgLogoDesc")
      .authorizationDesc("authorizationDesc")
      .status("status")
      .urlActiveExternal("urlActiveExternal")
      .additionalLanguage("additionalLanguage")
      .orgTypeCode("orgTypeCode")
      .startDate(LocalDate.of(2024, 1, 1))
      .brokerId(2L)
      .links(links)
      .build();
  }
}
