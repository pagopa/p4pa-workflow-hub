package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.CreateDebtPositionWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.mapper.DebtPositionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  private final CreateDebtPositionWfClient client;
  private final DebtPositionMapper debtPositionMapper;


  public DebtPositionServiceImpl(CreateDebtPositionWfClient client, DebtPositionMapper debtPositionMapper) {
    this.client = client;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  public WorkflowCreatedDTO createDPSync(DebtPositionRequestDTO debtPositionRequestDTO) {
    log.info("Mapping the debt position to create debt position sync");
    DebtPositionDTO debtPosition = mapDebtPositionRequestDTO2DebtPositionDTO(debtPositionRequestDTO);

    log.debug("Starting workflow for creation debt position sync with debtPositionId: {}", debtPositionRequestDTO.getDebtPositionId());
    String workflowId = client.createDPSync(debtPosition);

    return buildWorkflowCreatedDTO(workflowId);
  }

  @Override
  public WorkflowCreatedDTO createDPSyncAca(DebtPositionRequestDTO debtPositionRequestDTO) {
    log.info("Mapping the debt position to create debt position sync on ACA");
    DebtPositionDTO debtPosition = mapDebtPositionRequestDTO2DebtPositionDTO(debtPositionRequestDTO);

    log.debug("Starting workflow for creation debt position sync on ACA with debtPositionId: {}", debtPositionRequestDTO.getDebtPositionId());
    String workflowId = client.createDPSyncAca(debtPosition);

    return buildWorkflowCreatedDTO(workflowId);
  }

  private WorkflowCreatedDTO buildWorkflowCreatedDTO(String workflowId) {
    return WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();
  }

  private DebtPositionDTO mapDebtPositionRequestDTO2DebtPositionDTO(DebtPositionRequestDTO debtPositionRequestDTO) {
    return debtPositionMapper.map(debtPositionRequestDTO);
  }
}
