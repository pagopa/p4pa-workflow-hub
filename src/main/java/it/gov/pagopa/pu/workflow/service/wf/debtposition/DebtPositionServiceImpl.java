package it.gov.pagopa.pu.workflow.service.wf.debtposition;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowCompletionService;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.CheckDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.MassiveDebtPositionWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionSyncService debtPositionSyncService;
  private final CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient;
  private final MassiveDebtPositionWFClient massiveDPClient;
  private final WorkflowCompletionService workflowCompletionService;


  public DebtPositionServiceImpl(DebtPositionSyncService debtPositionSyncService, CheckDebtPositionExpirationWfClient checkDebtPositionExpirationWfClient,
                                 MassiveDebtPositionWFClient massiveDPClient, WorkflowCompletionService workflowCompletionService) {
    this.debtPositionSyncService = debtPositionSyncService;
    this.checkDebtPositionExpirationWfClient = checkDebtPositionExpirationWfClient;
    this.massiveDPClient = massiveDPClient;
    this.workflowCompletionService = workflowCompletionService;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, PaymentEventRequestDTO paymentEventRequest, WfExecutionParameters wfExecutionParameters, String accessToken) {
    log.debug("Starting workflow to sync DebtPosition: {} (massive: {}, partial: {})", debtPositionDTO.getDebtPositionId(), wfExecutionParameters.isMassive(), wfExecutionParameters.isPartialChange());
    return debtPositionSyncService.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters, accessToken);
  }

  @Override
  public WorkflowCreatedDTO checkDpExpiration(Long debtPositionId) {
    log.debug("Starting workflow for checking expiration of debt position with debtPositionId: {}", debtPositionId);
    return checkDebtPositionExpirationWfClient.checkDpExpiration(debtPositionId);
  }

  @Override
  public WorkflowCreatedDTO massiveIbanUpdate(Long orgId, MassiveDebtPositionIbanUpdateRequestDTO requestDTO) {
    String baseWfId = "MassiveIbanUpdateWF-" + orgId;
    String syncWfName = baseWfId + "_TO_SYNC";

    workflowCompletionService.checkWorkflowExistsAndNotTerminated(baseWfId);
    workflowCompletionService.checkWorkflowExistsAndNotTerminated(syncWfName);

    log.info("Starting massive IBAN update for orgId: {}", orgId);

    return massiveDPClient.startMassiveIbanUpdate(
      orgId,
      requestDTO.getDebtPositionTypeOrgId(),
      requestDTO.getOldIban(),
      requestDTO.getNewIban(),
      requestDTO.getOldPostalIban(),
      requestDTO.getNewPostalIban()
    );
  }

}
