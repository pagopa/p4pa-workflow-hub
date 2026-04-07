package it.gov.pagopa.pu.workflow.wf.debtposition.massive.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.MassiveDebtPositionWFClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.dto.MassiveIbanUpdateToSyncSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY_LOCAL)
public class ScheduleToSyncMassiveIbanUpdateWFActivityImpl implements ScheduleToSyncMassiveIbanUpdateWFActivity {
  private final MassiveDebtPositionWFClient massiveDebtPositionWFClient;

  public ScheduleToSyncMassiveIbanUpdateWFActivityImpl(MassiveDebtPositionWFClient massiveDebtPositionWFClient) {
    this.massiveDebtPositionWFClient = massiveDebtPositionWFClient;
  }

  @Override
  public void scheduleToSyncMassiveIbanUpdateWF(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    MassiveIbanUpdateToSyncSignalDTO signalDTO = MassiveIbanUpdateToSyncSignalDTO.builder()
      .orgId(orgId)
      .dptoId(dptoId)
      .oldIban(oldIban)
      .newIban(newIban)
      .oldPostalIban(oldPostalIban)
      .newPostalIban(newPostalIban)
      .build();
    massiveDebtPositionWFClient.startMassiveIbanUpdateToSync(signalDTO);
  }
}
