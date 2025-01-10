package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.TransferDTO;
import it.gov.pagopa.payhub.activities.dto.debtposition.InstallmentDTO;
import it.gov.pagopa.pu.workflow.dto.generated.InstallmentRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.TransferRequestDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.ReceiptFaker.buildReceiptDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.ReceiptFaker.buildReceiptRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferRequestDTO;

public class InstallmentFaker {

  public static InstallmentDTO buildInstallmentDTO() {
    List<TransferDTO> transfers = new ArrayList<>();
    transfers.add(buildTransferDTO());
    return InstallmentDTO.builder()
      .installmentId(1L)
      .status("status")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .creationDate(DATE.toInstant())
      .updateDate(DATE.toInstant())
      .dueDate(LocalDate.of(2099, 5, 15))
      .paymentTypeCode("paymentTypeCode")
      .amount(100L)
      .fee(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .iuvCreationDate(DATE.toInstant())
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .flagGenerateIuv(true)
      .sessionId("sessionId")
      .flagIuvVolatile(true)
      .transfers(transfers)
      .receipt(buildReceiptDTO())
      .payer(buildPersonDTO())
      .build();
  }

  public static InstallmentRequestDTO buildInstallmentRequestDTO() {
    List<TransferRequestDTO> transfers = new ArrayList<>();
    transfers.add(buildTransferRequestDTO());
    return InstallmentRequestDTO.builder()
      .installmentId(1L)
      .status("status")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .dueDate(LocalDate.of(2099, 5, 15))
      .paymentTypeCode("paymentTypeCode")
      .amount(100L)
      .fee(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .iuvCreationDate(OFFSET_DATE_TIME)
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .flagGenerateIuv(true)
      .sessionId("sessionId")
      .flagIuvVolatile(true)
      .transfers(transfers)
      .receipt(buildReceiptRequestDTO())
      .payer(buildPersonRequestDTO())
      .build();
  }
}
