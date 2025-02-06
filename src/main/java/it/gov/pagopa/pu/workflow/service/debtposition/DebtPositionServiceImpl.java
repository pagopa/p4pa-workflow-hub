package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.SynchronizeSyncAcaWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.HandleDebtPositionExpirationWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.HandleDebtPositionWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  private final HandleDebtPositionWfClient handleDebtPositionWfClient;
  private final SynchronizeSyncAcaWfClient synchronizeSyncAcaWfClient;
  private final HandleDebtPositionExpirationWfClient handleDebtPositionExpirationWfClient;

  public DebtPositionServiceImpl(HandleDebtPositionWfClient handleDebtPositionWfClient, SynchronizeSyncAcaWfClient synchronizeSyncAcaWfClient, HandleDebtPositionExpirationWfClient handleDebtPositionExpirationWfClient) {
    this.handleDebtPositionWfClient = handleDebtPositionWfClient;
    this.synchronizeSyncAcaWfClient = synchronizeSyncAcaWfClient;
    this.handleDebtPositionExpirationWfClient = handleDebtPositionExpirationWfClient;
  }

  @Override
  public WorkflowCreatedDTO handleDPSync(DebtPositionDTO debtPositionDTO) {
    log.debug("Starting workflow to handle debt position sync with debtPositionId: {}", debtPositionDTO.getDebtPositionId());
    String workflowId = handleDebtPositionWfClient.handleDPSync(debtPositionDTO);

    return buildWorkflowCreatedDTO(workflowId);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncAca(DebtPositionDTO debtPositionDTO) {
    log.debug("Starting workflow for creation debt position sync on ACA with debtPositionId: {}", debtPositionDTO.getDebtPositionId());
    String workflowId = synchronizeSyncAcaWfClient.synchronizeDPSyncAca(debtPositionDTO);

    return buildWorkflowCreatedDTO(workflowId);
  }

  @Override
  public WorkflowCreatedDTO handleDpExpiration(Long debtPositionId) {
    log.debug("Starting workflow for handling expiration of debt position with debtPositionId: {}", debtPositionId);
    String workflowId = handleDebtPositionExpirationWfClient.handleDpExpiration(debtPositionId);

    return buildWorkflowCreatedDTO(workflowId);
  }

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}
