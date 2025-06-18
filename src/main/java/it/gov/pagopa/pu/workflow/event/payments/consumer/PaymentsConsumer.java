package it.gov.pagopa.pu.workflow.event.payments.consumer;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.dto.DebtPositionEventDTO;
import it.gov.pagopa.pu.workflow.event.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.utilities.PaymentEventTypeUtils;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.assessments.CreateAssessmentsWFClient;
import it.gov.pagopa.pu.workflow.wf.assessments.CreateAssessmentsRegistryWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Slf4j
@Service
public class PaymentsConsumer implements Consumer<PaymentEventDTO<?>> {

  private final IudClassificationWFClient iudClassificationWFClient;
  private final CreateAssessmentsWFClient createAssessmentsWFClient;
  private final CreateAssessmentsRegistryWFClient createAssessmentsRegistryWFClient;

  public PaymentsConsumer(IudClassificationWFClient iudClassificationWFClient, CreateAssessmentsWFClient createAssessmentsWFClient,
    CreateAssessmentsRegistryWFClient createAssessmentsRegistryWFClient) {
    this.iudClassificationWFClient = iudClassificationWFClient;
    this.createAssessmentsWFClient = createAssessmentsWFClient;
    this.createAssessmentsRegistryWFClient = createAssessmentsRegistryWFClient;
  }

  @Override
  public void accept(PaymentEventDTO paymentEventDTO) {

    if(PaymentEventTypeUtils.CREATE_OR_UPDATE_STATUSES.contains(paymentEventDTO.getEventType())
       && paymentEventDTO.getPayload() instanceof DebtPositionDTO debtPosition) {
        List<String> iudList = Utilities.extractIudsFromDescription(paymentEventDTO.getEventDescription()).stream().toList();
        createAssessmentsRegistryWFClient.createAssessmentsRegistry(paymentEventDTO.getEventId(), debtPosition, iudList);
    }

    if (PaymentEventType.RT_RECEIVED.equals(paymentEventDTO.getEventType())) {
      if (paymentEventDTO.getPayload() instanceof DebtPositionDTO debtPosition) {
        log.info("Event RT_RECEIVED occurred on DebtPosition {}", debtPosition.getDebtPositionId());
        debtPosition.getPaymentOptions().stream()
          .flatMap((PaymentOptionDTO paymentOptionDTO) -> paymentOptionDTO.getInstallments().stream())
          .filter(i -> InstallmentStatus.PAID.equals(i.getStatus()))
          .forEach(i -> iudClassificationWFClient.notifyReceipt(
            IudClassificationNotifyReceiptSignalDTO.builder()
              .organizationId(debtPosition.getOrganizationId())
              .iud(i.getIud())
              .iuv(i.getIuv())
              .iur(i.getIur())
              .transferIndexes(i.getTransfers().stream()
                .map(TransferDTO::getTransferIndex)
                .toList())
              .build()
          ));

        handleCreateAssessments((DebtPositionEventDTO)paymentEventDTO, debtPosition);
      } else {
        log.error("Unexpected payload related to RT_RECEIVED event: provided {} having payload type {}"
          , paymentEventDTO.getClass().getName(),
          Optional.ofNullable(paymentEventDTO.getPayload()).map(p -> p.getClass().getName()).orElse("null"));
      }
    }
  }

  private void handleCreateAssessments(DebtPositionEventDTO event, DebtPositionDTO debtPosition) {
    Set<Long> receiptIds;
    try {
      receiptIds = Set.of(Long.valueOf(event.getEventDescription().replace("receiptId:", "")));
    } catch (Exception e) {
      log.error("It was not possible to retrieve the particular receiptId which originated the event, let's considering all installment's receiptIds");
      receiptIds = debtPosition.getPaymentOptions().stream()
        .flatMap(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream())
        .filter(installment -> Objects.nonNull(installment.getReceiptId()) && InstallmentStatus.PAID.equals(installment.getStatus()))
        .map(InstallmentDTO::getReceiptId)
        .collect(Collectors.toSet());
    }
    if (receiptIds.isEmpty()) {
      log.error("Cannot retrieve a receiptId related to the input event: " + event.getEventId());
    } else {
      receiptIds.forEach(createAssessmentsWFClient::createAssessments);
    }
  }

}
