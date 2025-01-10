package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.TransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.TransferRequestDTO;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;

public class TransferFaker {

    public static TransferDTO buildTransferDTO(){
        return TransferDTO.builder()
                .transferId(1L)
                .orgFiscalCode("orgFiscalCode")
                .beneficiaryName("beneficiaryName")
                .iban("iban")
                .amount(100L)
                .creationDate(DATE.toInstant())
                .lastUpdateDate(DATE.toInstant())
                .remittanceInformation("remittanceInformation")
                .stampType("stampType")
                .category("category")
                .documentHash("documentHash")
                .provincialResidence("provincialResidence")
                .transferIndex(1)
                .build();
    }

  public static TransferRequestDTO buildTransferRequestDTO(){
    return TransferRequestDTO.builder()
      .transferId(1L)
      .orgFiscalCode("orgFiscalCode")
      .beneficiaryName("beneficiaryName")
      .iban("iban")
      .amount(100L)
      .creationDate(OFFSET_DATE_TIME)
      .lastUpdateDate(OFFSET_DATE_TIME)
      .remittanceInformation("remittanceInformation")
      .stampType("stampType")
      .category("category")
      .documentHash("documentHash")
      .provincialResidence("provincialResidence")
      .transferIndex(1)
      .build();
  }
}
