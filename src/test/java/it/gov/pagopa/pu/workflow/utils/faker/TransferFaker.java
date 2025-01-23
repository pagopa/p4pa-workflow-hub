package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.TransferRequestDTO;

public class TransferFaker {

    public static TransferDTO buildTransferDTO(){
        return TransferDTO.builder()
          .transferId(1L)
          .installmentId(1L)
          .orgFiscalCode("orgFiscalCode")
          .orgName("orgName")
          .amountCents(100L)
          .remittanceInformation("remittanceInformation")
          .stampType("stampType")
          .stampHashDocument("stampHashDocument")
          .stampProvincialResidence("stampProvincialResidence")
          .iban("iban")
          .postalIban("postalIban")
          .category("category")
          .transferIndex(1L)
          .build();
    }

  public static TransferRequestDTO buildTransferRequestDTO(){
    return TransferRequestDTO.builder()
      .transferId(1L)
      .installmentId(1L)
      .orgFiscalCode("orgFiscalCode")
      .orgName("orgName")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .stampType("stampType")
      .stampHashDocument("stampHashDocument")
      .stampProvincialResidence("stampProvincialResidence")
      .iban("iban")
      .postalIban("postalIban")
      .category("category")
      .transferIndex(1L)
      .build();
  }
}
