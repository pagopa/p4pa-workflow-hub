package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.IufClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL)
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
