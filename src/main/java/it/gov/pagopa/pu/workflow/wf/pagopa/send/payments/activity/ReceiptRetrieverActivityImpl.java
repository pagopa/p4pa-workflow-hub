package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.connector.debtposition.ReceiptService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_SEND_RESERVED_PAYMENT_HANDLER_LOCAL)
public class ReceiptRetrieverActivityImpl implements ReceiptRetrieverActivity {

  private final ReceiptService receiptService;

  public ReceiptRetrieverActivityImpl(ReceiptService receiptService) {
    this.receiptService = receiptService;
  }

  @Override
  public ReceiptDTO retrieveReceipt(Long receiptId) {
    return receiptService.getByReceiptId(receiptId);
  }
}
