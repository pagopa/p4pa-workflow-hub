package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

import java.time.OffsetDateTime;

public interface DebtPositionFineClient {

  WorkflowCreatedDTO expireFineReduction(Long debtPositionId, FineWfExecutionConfig executionParams);
  WorkflowCreatedDTO scheduleExpireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig, OffsetDateTime fineReductionExpirationDateTime);
  WorkflowCreatedDTO synchronizeFineDP(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, Boolean massive, FineWfExecutionConfig wfExecutionConfig);
}
