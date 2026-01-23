package it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;

import java.util.Map;

/** It will invoke IO notification */
@ActivityInterface
public interface StartIONotificationWFActivity {

  @ActivityMethod
  void startIONotificationWF(DebtPositionDTO debtPositionDTO, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages);
}
