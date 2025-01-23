package it.gov.pagopa.pu.workflow.event.consumer.payments;

import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.workflow.event.consumer.payments.dto.PaymentEventDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
public class PaymentsConsumer implements Consumer<PaymentEventDTO> {

  private final TransferClassificationWFClient transferClassificationWFClient;

  public PaymentsConsumer(TransferClassificationWFClient transferClassificationWFClient) {
    this.transferClassificationWFClient = transferClassificationWFClient;
  }

  @Override
  public void accept(PaymentEventDTO paymentEventDTO) {
    if ("RT_RECEIVED".equals(paymentEventDTO.getEventType())) {
      log.info("Event RT_RECEIVED occurred on DebtPosition {}", paymentEventDTO.getPayload().getDebtPositionId());
      paymentEventDTO.getPayload().getPaymentOptions().stream()
        .flatMap((PaymentOptionDTO paymentOptionDTO) -> paymentOptionDTO.getInstallments().stream())
        .filter(i -> InstallmentDTO.StatusEnum.PAID.equals(i.getStatus()))
        .forEach(i -> i.getTransfers()
          .forEach(t ->
            // Once the IUD classification is ready, this should call the IUD Classification if there is a IUD
            transferClassificationWFClient.classify(paymentEventDTO.getPayload().getOrganizationId(), i.getIuv(), i.getIur(), t.getTransferIndex().intValue())));
    }
  }

}
