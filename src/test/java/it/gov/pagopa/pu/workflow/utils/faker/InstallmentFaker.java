package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;

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
      .status(InstallmentDTO.StatusEnum.TO_SYNC)
      .syncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentSyncStatus.SyncStatusFromEnum.TO_SYNC).syncStatusTo(InstallmentSyncStatus.SyncStatusToEnum.UNPAID).build())
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .iupdPagopa("iupdPagopa")
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
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
}
