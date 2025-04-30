package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface SynchronizeDebtPositionWfClient {
  WorkflowCreatedDTO synchronizeNoPagoPADP(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
  WorkflowCreatedDTO synchronizeDPSync(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
  WorkflowCreatedDTO synchronizeDPSyncAca(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
  WorkflowCreatedDTO synchronizeDPSyncGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
  WorkflowCreatedDTO synchronizeDPSyncAcaGpdPreLoad(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
  WorkflowCreatedDTO synchronizeDPAsyncGpd(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig);
}
