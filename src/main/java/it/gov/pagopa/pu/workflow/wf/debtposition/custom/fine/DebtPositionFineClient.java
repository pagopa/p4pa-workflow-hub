package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;

import java.time.OffsetDateTime;

public interface DebtPositionFineClient {

  String expireFineReduction(Long debtPositionId, FineWfExecutionConfig executionParams);
  String scheduleExpireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig, OffsetDateTime fineReductionExpirationDateTime);
}
