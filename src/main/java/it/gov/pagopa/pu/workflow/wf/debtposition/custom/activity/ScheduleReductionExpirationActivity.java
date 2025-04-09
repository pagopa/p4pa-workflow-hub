package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;

public interface ScheduleReductionExpirationActivity {

  String expireFineReduction(Long debtPositionId, FineWfExecutionConfig executionParams);
}
