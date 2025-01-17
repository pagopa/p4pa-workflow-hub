package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import io.temporal.activity.ActivityInterface;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;

import java.util.List;

@ActivityInterface
public interface NotifyPaymentsReportingToIufClassificationActivity {

  void signalIufClassificationWithStart(Long organizationId, String iuf,
                                        String outComeCode, List<Transfer2ClassifyDTO> transfers2classify);

}
