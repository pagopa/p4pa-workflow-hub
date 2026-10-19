package it.gov.pagopa.pu.workflow.wf.pagopa.send.payments.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;

@ActivityInterface
public interface ReceiptRetrieverActivity {

  @ActivityMethod
  ReceiptDTO retrieveReceipt(Long receiptId);
}
