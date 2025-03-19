package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.model.DebtPositionWorkflowType;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.repository.DebtPositionWorkflowTypeRepository;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeOrgRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/** @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1582792753/Scelta+del+Workflow+da+eseguire>Confluence page </a> */
@Service
public class WfExecutionConfigHandlerService {

  private final DebtPositionWorkflowTypeRepository debtPositionWorkflowTypeRepository;
  private final WorkflowTypeOrgRepository workflowTypeOrgRepository;
  private final WfExecutionConfigMergeService mergeService;

  public WfExecutionConfigHandlerService(DebtPositionWorkflowTypeRepository debtPositionWorkflowTypeRepository, WorkflowTypeOrgRepository workflowTypeOrgRepository, WfExecutionConfigMergeService mergeService) {
    this.debtPositionWorkflowTypeRepository = debtPositionWorkflowTypeRepository;
    this.workflowTypeOrgRepository = workflowTypeOrgRepository;
    this.mergeService = mergeService;
  }

  public void persistAndConfigure(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters){
    Optional<DebtPositionWorkflowType> storedExecutionConfig = debtPositionWorkflowTypeRepository.findById(Objects.requireNonNull(debtPositionDTO.getDebtPositionId()));

    if(storedExecutionConfig.isPresent()){
      wfExecutionParameters.setWfExecutionConfig(storedExecutionConfig.get().getExecutionConfig());
    } else {
      Optional<WorkflowTypeOrg> workflowTypeOrg = workflowTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId());
      WfExecutionConfig defaultConfig = workflowTypeOrg.map(WorkflowTypeOrg::getDefaultExecutionConfig)
        .orElse(null);
      wfExecutionParameters.setWfExecutionConfig(mergeService.merge(defaultConfig, wfExecutionParameters.getWfExecutionConfig()));

      if(wfExecutionParameters.getWfExecutionConfig()!=null){
        saveWfExecutionConfig(debtPositionDTO, workflowTypeOrg, wfExecutionParameters.getWfExecutionConfig());
      }
    }
  }

  private void saveWfExecutionConfig(DebtPositionDTO debtPositionDTO, Optional<WorkflowTypeOrg> workflowTypeOrg, WfExecutionConfig wfExecutionConfig) {
    DebtPositionWorkflowType entity = new DebtPositionWorkflowType();
    entity.setDebtPositionId(debtPositionDTO.getDebtPositionId());
    entity.setWorkflowTypeOrgId(workflowTypeOrg.map(WorkflowTypeOrg::getDebtPositionTypeOrgId).orElse(null));
    entity.setExecutionConfig(wfExecutionConfig);

    debtPositionWorkflowTypeRepository.save(entity);
  }
}
