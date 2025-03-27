package it.gov.pagopa.pu.workflow.event.payments.consumer;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.dto.DebtPositionEventDTO;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.wf.assessments.CreateAssessmentsWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PaymentsConsumer implements Consumer<PaymentEventDTO<?>> {

  private final TransferClassificationWFClient transferClassificationWFClient;
  private final CreateAssessmentsWFClient createAssessmentsWFClient;

  public PaymentsConsumer(TransferClassificationWFClient transferClassificationWFClient, CreateAssessmentsWFClient createAssessmentsWFClient) {
    this.transferClassificationWFClient = transferClassificationWFClient;
    this.createAssessmentsWFClient = createAssessmentsWFClient;
  }

  @Override
  public void accept(PaymentEventDTO paymentEventDTO) {
    if (PaymentEventType.RT_RECEIVED.equals(paymentEventDTO.getEventType())) {
      if (paymentEventDTO.getPayload() instanceof DebtPositionDTO debtPosition) {
        log.info("Event RT_RECEIVED occurred on DebtPosition {}", debtPosition.getDebtPositionId());
        debtPosition.getPaymentOptions().stream()
          .flatMap((PaymentOptionDTO paymentOptionDTO) -> paymentOptionDTO.getInstallments().stream())
          .filter(i -> InstallmentStatus.PAID.equals(i.getStatus()))
          .forEach(i -> i.getTransfers()
            .forEach(t ->
              // Once the IUD classification is ready, this should call the IUD Classification if there is a IUD
              transferClassificationWFClient.startTransferClassification(
                TransferClassificationStartSignalDTO.builder()
                  .orgId(debtPosition.getOrganizationId())
                  .iuv(i.getIuv())
                  .iur(i.getIur())
                  .transferIndex(t.getTransferIndex())
                  .build()
              )));

        handleCreateAssessments((DebtPositionEventDTO)paymentEventDTO, debtPosition);

      } else {
        log.error("Unexpected payload related to RT_RECEIVED event: provided {} having payload type {}"
          , paymentEventDTO.getClass().getName(),
          Optional.ofNullable(paymentEventDTO.getPayload()).map(p -> p.getClass().getName()).orElse("null"));
      }
    }
  }

  void handleCreateAssessments(DebtPositionEventDTO event, DebtPositionDTO debtPosition) {
    List<Long> receiptIds;
    try {
      receiptIds = List.of(Long.valueOf(event.getEventDescription().replace("receiptId:", "")));
    } catch (Exception e) {
      log.error("It was not possible to retrieve the particular receiptId which originated the event, let's considering all installment's receiptIds");
      receiptIds = debtPosition.getPaymentOptions().stream()
        .flatMap(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream())
        .filter(installment -> Objects.nonNull(installment.getReceiptId()) && InstallmentStatus.PAID.equals(installment.getStatus()))
        .map(InstallmentDTO::getReceiptId)
        .collect(Collectors.toSet())
        .stream()
        .toList();
    }
    if (receiptIds.isEmpty()) {
      log.error("Cannot retrieve a receiptId related to the input event: " + event.getEventId());
    } else {
      receiptIds.forEach(createAssessmentsWFClient::createAssessments);
    }
  }

}
