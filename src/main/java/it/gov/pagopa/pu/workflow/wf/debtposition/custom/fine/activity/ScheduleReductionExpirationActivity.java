package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;

import java.time.OffsetDateTime;

@ActivityInterface
public interface ScheduleReductionExpirationActivity {

  @ActivityMethod
  String scheduleExpireFineReduction(Long debtPositionId, FineWfExecutionConfig executionParams, OffsetDateTime fineReductionExpirationDateTime);
}
