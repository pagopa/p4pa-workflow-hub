package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
@Slf4j
@ActivityImpl(taskQueues = PaymentsReportingIngestionWFImpl.TASK_QUEUE)
public class NotifyPaymentsReportingToIufClassificationActivityImpl implements NotifyPaymentsReportingToIufClassificationActivity {

  private IufClassificationWFClient iufClassificationWFClient;

  public NotifyPaymentsReportingToIufClassificationActivityImpl(IufClassificationWFClient iufClassificationWFClient) {
    this.iufClassificationWFClient = iufClassificationWFClient;
  }

  @Override
  public void signalIufClassificationWithStart(Long organizationId, String iuf,
                                               String outComeCode, List<Transfer2ClassifyDTO> transfers2classify) {

    IufClassificationNotifyPaymentsReportingSignalDTO paymentsReportingSignalDTO =
      IufClassificationNotifyPaymentsReportingSignalDTO.builder()
        .organizationId(organizationId)
        .iuf(iuf)
        .outcomeCode(outComeCode)
        .transfers2classify(transfers2classify)
        .build();

    iufClassificationWFClient.notifyPaymentsReporting(paymentsReportingSignalDTO);

  }

}
