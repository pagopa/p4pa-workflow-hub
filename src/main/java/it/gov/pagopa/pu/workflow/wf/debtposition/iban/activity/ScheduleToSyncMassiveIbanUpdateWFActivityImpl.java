package it.gov.pagopa.pu.workflow.wf.debtposition.iban.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.MassiveDebtPositionWFClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.dto.MassiveIbanUpdateToSyncSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY_LOCAL)
public class ScheduleToSyncMassiveIbanUpdateWFActivityImpl implements ScheduleToSyncMassiveIbanUpdateWFActivity {
  private final MassiveDebtPositionWFClient massiveDebtPositionWFClient;
  private final Duration scheduleDuration;

  public ScheduleToSyncMassiveIbanUpdateWFActivityImpl(
    MassiveDebtPositionWFClient massiveDebtPositionWFClient,
    @Value("${workflow.massive-debt-position.schedule-minutes-massive-iban-update-to-sync}") int scheduleMinutes
  ) {
    this.massiveDebtPositionWFClient = massiveDebtPositionWFClient;
    this.scheduleDuration = Duration.ofMinutes(scheduleMinutes);
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

    massiveDebtPositionWFClient.scheduleMassiveIbanUpdateToSync(signalDTO, scheduleDuration);
  }
}
