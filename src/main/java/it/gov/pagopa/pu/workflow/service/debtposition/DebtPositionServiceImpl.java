package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.SynchronizeSyncAcaWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.HandleDebtPositionWfClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  private final HandleDebtPositionWfClient handleDebtPositionWfClient;
  private final SynchronizeSyncAcaWfClient synchronizeSyncAcaWfClient;


  public DebtPositionServiceImpl(HandleDebtPositionWfClient handleDebtPositionWfClient, SynchronizeSyncAcaWfClient synchronizeSyncAcaWfClient) {
    this.handleDebtPositionWfClient = handleDebtPositionWfClient;
    this.synchronizeSyncAcaWfClient = synchronizeSyncAcaWfClient;
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

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}
