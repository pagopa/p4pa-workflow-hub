package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.InstallmentRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.InstallmentRequestStatus;
import it.gov.pagopa.pu.workflow.dto.generated.InstallmentSyncStatusRequest;
import it.gov.pagopa.pu.workflow.dto.generated.TransferRequestDTO;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferRequestDTO;

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
      .dueDate(OFFSET_DATE_TIME)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .debtor(buildPersonDTO())
      .transfers(transfers)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .build();
  }

  public static InstallmentRequestDTO buildInstallmentRequestDTO() {
    List<TransferRequestDTO> transfers = new ArrayList<>();
    transfers.add(buildTransferRequestDTO());
    return InstallmentRequestDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentRequestStatus.TO_SYNC)
      .syncStatus(InstallmentSyncStatusRequest.builder().syncStatusFrom(InstallmentRequestStatus.TO_SYNC).syncStatusTo(InstallmentRequestStatus.UNPAID).build())
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .iupdPagopa("iupdPagopa")
      .dueDate(OFFSET_DATE_TIME)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .debtor(buildPersonRequestDTO())
      .transfers(transfers)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .build();
  }
}
