package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.classifications.PaymentsReportingTransferDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@ActivityImpl(taskQueues = PaymentsReportingIngestionWFImpl.TASK_QUEUE_PAYMENTS_REPORTING_INGESTION_LOCAL_ACTIVITY)
public class NotifyPaymentsReportingToIufClassificationActivityImpl implements NotifyPaymentsReportingToIufClassificationActivity {

  private final IufClassificationWFClient iufClassificationWFClient;

  public NotifyPaymentsReportingToIufClassificationActivityImpl(IufClassificationWFClient iufClassificationWFClient) {
    this.iufClassificationWFClient = iufClassificationWFClient;
  }

  @Override
  public void signalIufClassificationWithStart(Long organizationId, String iuf, List<PaymentsReportingTransferDTO> transfers) {
    IufClassificationNotifyPaymentsReportingSignalDTO paymentsReportingSignalDTO =
      IufClassificationNotifyPaymentsReportingSignalDTO.builder()
        .iuf(iuf)
        .organizationId(organizationId)
        .transfers(transfers)
        .build();
    iufClassificationWFClient.notifyPaymentsReporting(paymentsReportingSignalDTO);
  }
}
