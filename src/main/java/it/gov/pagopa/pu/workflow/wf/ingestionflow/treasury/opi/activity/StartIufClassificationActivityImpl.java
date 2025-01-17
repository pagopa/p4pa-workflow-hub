package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity;

import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@Slf4j
@ActivityImpl(taskQueues = PaymentsReportingIngestionWFImpl.TASK_QUEUE)
public class StartIufClassificationActivityImpl implements StartIufClassificationActivity {

  private IufClassificationWFClient iufClassificationWFClient;

  public StartIufClassificationActivityImpl(IufClassificationWFClient IufClassificationWFClient) {
    this.iufClassificationWFClient = IufClassificationWFClient;
  }

  @Override
  public void signalIufClassificationWithStart(Long organizationId, String iuf, String treasuryId) {

    IufClassificationNotifyTreasurySignalDTO treasurySignalDTO =
      IufClassificationNotifyTreasurySignalDTO.builder()
        .organizationId(organizationId)
        .iuf(iuf)
        .treasuryId(treasuryId)
        .build();

    iufClassificationWFClient.notifyTreasury(treasurySignalDTO);

  }

}
