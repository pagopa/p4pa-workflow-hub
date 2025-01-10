package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.ReceiptDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ReceiptRequestDTO;

import java.time.LocalDate;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonRequestDTO;

public class ReceiptFaker {

  public static ReceiptDTO buildReceiptDTO(){
    return ReceiptDTO.builder()
      .receiptId(1L)
      .creationDate(DATE.toInstant())
      .paymentReceiptId("iur")
      .noticeNumber("noticeNumber")
      .fiscalCode("fiscalCode")
      .outcome("outcome")
      .creditorReferenceId("creditorReferenceId")
      .paymentAmount(100L)
      .description("description")
      .companyName("companyName")
      .officeName("officeName")
      .debtor(buildPersonDTO())
      .idPsp("idPsp")
      .pspFiscalCode("pspFiscalCode")
      .pspPartitaIva("pspPartitaIva")
      .pspCompanyName("pspCompanyName")
      .idChannel("idChannel")
      .channelDescription("channelDescription")
      .payer(buildPersonDTO())
      .paymentMethod("paymentMethod")
      .fee(100L)
      .paymentDateTime(LocalDate.of(2024, 5, 15))
      .applicationDate(LocalDate.of(2024, 5, 15))
      .transferDate(LocalDate.of(2024, 5, 15))
      .receiptBytes("receiptBytes".getBytes())
      .standin(true)
      .status("status")
      .dtProcessing(DATE.toInstant())
      .numTriesProcessing(1)
      .build();
  }

  public static ReceiptRequestDTO buildReceiptRequestDTO(){
    return ReceiptRequestDTO.builder()
      .receiptId(1L)
      .creationDate(OFFSET_DATE_TIME)
      .paymentReceiptId("iur")
      .noticeNumber("noticeNumber")
      .fiscalCode("fiscalCode")
      .outcome("outcome")
      .creditorReferenceId("creditorReferenceId")
      .paymentAmount(100L)
      .description("description")
      .companyName("companyName")
      .officeName("officeName")
      .debtor(buildPersonRequestDTO())
      .idPsp("idPsp")
      .pspFiscalCode("pspFiscalCode")
      .pspPartitaIva("pspPartitaIva")
      .pspCompanyName("pspCompanyName")
      .idChannel("idChannel")
      .channelDescription("channelDescription")
      .payer(buildPersonRequestDTO())
      .paymentMethod("paymentMethod")
      .fee(100L)
      .paymentDateTime(LocalDate.of(2024, 5, 15))
      .applicationDate(LocalDate.of(2024, 5, 15))
      .transferDate(LocalDate.of(2024, 5, 15))
      .receiptBytes("receiptBytes".getBytes())
      .standin(true)
      .status("status")
      .dtProcessing(OFFSET_DATE_TIME)
      .numTriesProcessing(1)
      .build();
  }
}
