package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import io.temporal.activity.ActivityInterface;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;

@ActivityInterface
public interface NotifyPaymentsReportingToIufClassificationActivity {

  void signalIufClassificationWithStart(String iuf, PaymentsReportingTransferDTO paymentsReportingTransferDTO);

}
