package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;

import java.util.List;

@ActivityInterface
public interface NotifyPaymentsReportingToIufClassificationActivity {

  @ActivityMethod
  void signalIufClassificationWithStart(Long organizationId, String iuf, List<PaymentsReportingTransferDTO> transfers);

}
