package it.gov.pagopa.pu.workflow.service.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionFineServiceImpl implements DebtPositionFineService {

  private final DebtPositionFineClient debtPositionFineClient;

  public DebtPositionFineServiceImpl(DebtPositionFineClient debtPositionFineClient) {
    this.debtPositionFineClient = debtPositionFineClient;
  }

  @Override
  public WorkflowCreatedDTO handleFineReductionExpiration(Long debtPositionId, PaymentEventRequestDTO paymentEventRequestDTO, boolean massive, FineWfExecutionConfig executionParams) {
    log.debug("Starting workflow to handle fine reduction expiration: {} (massive: {}, paymentEventType: {})", debtPositionId, massive, paymentEventRequestDTO.getPaymentEventType());
    String workflowId = debtPositionFineClient.handleFineReductionExpiration(debtPositionId, paymentEventRequestDTO, massive, executionParams);

    return WorkflowCreatedDTO.builder().workflowId(workflowId).build();
  }
}
