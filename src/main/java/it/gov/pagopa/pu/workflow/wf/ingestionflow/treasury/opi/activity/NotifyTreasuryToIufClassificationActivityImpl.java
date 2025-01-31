package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.wfingestion.TreasuryOpiIngestionWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TreasuryOpiIngestionWFImpl.TASK_QUEUE_TREASURY_OPI_INGESTION_LOCAL_ACTIVITY)
public class NotifyTreasuryToIufClassificationActivityImpl implements NotifyTreasuryToIufClassificationActivity {

  private final IufClassificationWFClient iufClassificationWFClient;

  public NotifyTreasuryToIufClassificationActivityImpl(IufClassificationWFClient iufClassificationWFClient) {
    this.iufClassificationWFClient = iufClassificationWFClient;
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
