package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;

public interface DebtPositionFineClient {

  String expireFineReduction(Long debtPositionId, FineWfExecutionConfig executionParams);
}
