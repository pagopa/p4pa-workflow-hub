package it.gov.pagopa.pu.workflow.service.debtposition.sync.config;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.model.DebtPositionWorkflowType;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.repository.DebtPositionWorkflowTypeRepository;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeOrgRepository;
import it.gov.pagopa.pu.workflow.service.DataCipherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1582792753/Scelta+del+Workflow+da+eseguire>Confluence page </a>
 */
@Slf4j
@Service
public class WfExecutionConfigHandlerService {

  private final DataCipherService dataCipherService;
  private final DebtPositionWorkflowTypeRepository debtPositionWorkflowTypeRepository;
  private final WorkflowTypeOrgRepository workflowTypeOrgRepository;
  private final WfExecutionConfigMergeService mergeService;

  public WfExecutionConfigHandlerService(
    DataCipherService dataCipherService,
    DebtPositionWorkflowTypeRepository debtPositionWorkflowTypeRepository,
    WorkflowTypeOrgRepository workflowTypeOrgRepository,
    WfExecutionConfigMergeService mergeService) {
    this.dataCipherService = dataCipherService;
    this.debtPositionWorkflowTypeRepository = debtPositionWorkflowTypeRepository;
    this.workflowTypeOrgRepository = workflowTypeOrgRepository;
    this.mergeService = mergeService;
  }

  public void persistAndConfigure(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters) {
    Optional<WfExecutionConfig> storedExecutionConfig = debtPositionWorkflowTypeRepository
      .findById(Objects.requireNonNull(debtPositionDTO.getDebtPositionId()))
      .map(DebtPositionWorkflowType::getExecutionConfig)
      .map(cipherData -> dataCipherService.decryptObj(cipherData, WfExecutionConfig.class));

    if (storedExecutionConfig.isPresent()) {
      WfExecutionConfig stored = storedExecutionConfig.get();
      if (wfExecutionParameters.getWfExecutionConfig() == null) {
        log.info("WfExecutionConfig already persisted for DebtPosition {}, setting them ({})"
          , debtPositionDTO.getDebtPositionId()
          , stored.getClass());
      } else {
        log.warn("WfExecutionConfig already persisted for DebtPosition {}, setting them ignoring provided input (storedType: {}, providedType: {})"
          , debtPositionDTO.getDebtPositionId()
          , stored.getClass()
          , wfExecutionParameters.getWfExecutionConfig().getClass());
      }
      wfExecutionParameters.setWfExecutionConfig(stored);
    } else {
      Optional<WorkflowTypeOrg> workflowTypeOrg = workflowTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId());
      WfExecutionConfig defaultConfig = workflowTypeOrg.map(WorkflowTypeOrg::getDefaultExecutionConfig)
        .orElse(null);
      wfExecutionParameters.setWfExecutionConfig(mergeService.merge(defaultConfig, wfExecutionParameters.getWfExecutionConfig()));

      if (wfExecutionParameters.getWfExecutionConfig() != null) {
        log.info("Persisting WfExecutionConfig for DebtPosition {} ({})"
          , debtPositionDTO.getDebtPositionId()
          , wfExecutionParameters.getWfExecutionConfig().getClass());
        saveWfExecutionConfig(debtPositionDTO, workflowTypeOrg, wfExecutionParameters.getWfExecutionConfig());
      }
    }
  }

  private void saveWfExecutionConfig(DebtPositionDTO debtPositionDTO, Optional<WorkflowTypeOrg> workflowTypeOrg, WfExecutionConfig wfExecutionConfig) {
    DebtPositionWorkflowType entity = new DebtPositionWorkflowType();
    entity.setDebtPositionId(debtPositionDTO.getDebtPositionId());
    entity.setWorkflowTypeOrgId(workflowTypeOrg.map(WorkflowTypeOrg::getDebtPositionTypeOrgId).orElse(null));
    entity.setExecutionConfig(dataCipherService.encryptObj(wfExecutionConfig));

    debtPositionWorkflowTypeRepository.save(entity);
  }
}
