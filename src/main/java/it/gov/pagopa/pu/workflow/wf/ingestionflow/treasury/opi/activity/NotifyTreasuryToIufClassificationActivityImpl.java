package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = NotifyTreasuryToIufClassificationActivityImpl.TASK_QUEUE_NOTIFY_TREASURY_TO_IUF_CLASSIFICATION_ACTIVITY)
public class NotifyTreasuryToIufClassificationActivityImpl implements NotifyTreasuryToIufClassificationActivity {
  public static final String TASK_QUEUE_NOTIFY_TREASURY_TO_IUF_CLASSIFICATION_ACTIVITY = "NOTIFY_TREASURY_TO_IUF_CLASSIFICATION";

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
