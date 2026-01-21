package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.IoNotificationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@ActivityImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC_LOCAL)
public class StartIONotificationWFActivityImpl implements StartIONotificationWFActivity {

  private final IoNotificationWFClient ioNotificationWFClient;

  public StartIONotificationWFActivityImpl(IoNotificationWFClient ioNotificationWFClient) {
    this.ioNotificationWFClient = ioNotificationWFClient;
  }

  @Override
  public void startIONotificationWF(DebtPositionDTO debtPositionDTO, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
    log.info("Starting IO notification process on debtPosition: {}", debtPositionDTO.getDebtPositionId());
    ioNotificationWFClient.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessages);
  }
}
