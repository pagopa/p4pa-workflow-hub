package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflow.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.DATE;
import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferDTO;

public class InstallmentFaker {

  public static InstallmentDTO buildInstallmentDTO() {
    List<TransferDTO> transfers = new ArrayList<>();
    transfers.add(buildTransferDTO());
    return InstallmentDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentStatus.TO_SYNC)
      .syncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.TO_SYNC).syncStatusTo(InstallmentStatus.UNPAID).build())
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .iupdPagopa("iupdPagopa")
      .dueDate(DATE)
      .notificationFeeCents(1000L)
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .debtor(buildPersonDTO())
      .transfers(transfers)
      .notificationDate(OFFSET_DATE_TIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .build();
  }
  public static InstallmentDTO buildInstallmentDTO2(){
    List<TransferDTO> transfers = new ArrayList<>();
    transfers.add(buildTransferDTO());
    return TestUtils.getPodamFactory().manufacturePojo(InstallmentDTO.class)
      .installmentId(2L)
      .paymentOptionId(2L)
      .status(InstallmentStatus.UNPAID)
      .iupdPagopa("iupdPagopa")
      .iud("iud2")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .dueDate(DATE)
      .notificationFeeCents(1000L)
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .transfers(transfers)
      .debtor(buildPersonDTO());
  }
}
