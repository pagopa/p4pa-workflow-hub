package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionSyncService debtPositionSyncService;
  private final CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient;

  public DebtPositionServiceImpl(DebtPositionSyncService debtPositionSyncService, CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient) {
    this.debtPositionSyncService = debtPositionSyncService;
    this.checkDebtPositionExpirationWfClient = checkDebtPositionExpirationWfClient;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType, WfExecutionParameters wfExecutionParameters, String accessToken) {
    log.debug("Starting workflow to sync DebtPosition: {} (massive: {}, partial: {})", debtPositionDTO.getDebtPositionId(), wfExecutionParameters.isMassive(), wfExecutionParameters.isPartialChange());
    String workflowId = debtPositionSyncService.invokeWorkflow(debtPositionDTO, paymentEventType, wfExecutionParameters, accessToken);

    return buildWorkflowCreatedDTO(workflowId);
  }

  @Override
  public WorkflowCreatedDTO checkDpExpiration(Long debtPositionId) {
    log.debug("Starting workflow for checking expiration of debt position with debtPositionId: {}", debtPositionId);
    String workflowId = checkDebtPositionExpirationWfClient.checkDpExpiration(debtPositionId);

    return buildWorkflowCreatedDTO(workflowId);
  }

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}
