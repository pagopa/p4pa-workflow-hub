package it.gov.pagopa.pu.workflow.service;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.workflow.exception.custom.InvalidWfExecutionConfigException;
import it.gov.pagopa.pu.workflow.exception.custom.WorkflowTypeNotFoundException;
import it.gov.pagopa.pu.workflow.model.WorkflowType;
import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeOrgRepository;
import it.gov.pagopa.pu.workflow.repository.WorkflowTypeRepository;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.config.WfExecutionConfigMergeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTypeOrgSaveServiceImpl implements WorkflowTypeOrgSaveService {

  private final WorkflowTypeRepository workflowTypeRepository;
  private final WfExecutionConfigMergeService configMergeService;
  private final WorkflowTypeOrgRepository repository;

  public WorkflowTypeOrgSaveServiceImpl(WorkflowTypeRepository workflowTypeRepository, WfExecutionConfigMergeService configMergeService, WorkflowTypeOrgRepository repository) {
    this.workflowTypeRepository = workflowTypeRepository;
    this.configMergeService = configMergeService;
    this.repository = repository;
  }

  @Override
  public WorkflowTypeOrg save(WorkflowTypeOrg entity) {
    Long workflowTypeId = entity.getWorkflowTypeId();
    WorkflowType workflowType = workflowTypeRepository.findById(workflowTypeId)
      .orElseThrow(() -> new WorkflowTypeNotFoundException("Cannot find WorkflowType having id " + workflowTypeId));

    validateWfExecutionConfigType(entity, workflowType, workflowTypeId);

    WfExecutionConfig merged = configMergeService.merge(workflowType.getDefaultExecutionConfig(), entity.getDefaultExecutionConfig());
    entity.setDefaultExecutionConfig(merged);
    return repository.save(entity);
  }

  private static void validateWfExecutionConfigType(WorkflowTypeOrg entity, WorkflowType workflowType, Long workflowTypeId) {
    if (!entity.getDefaultExecutionConfig().getClass().equals(workflowType.getDefaultExecutionConfig().getClass())) {
      throw new InvalidWfExecutionConfigException(
        "Invalid execution config type for workflowTypeId: %d. Expected: %s, Found: %s".formatted(
          workflowTypeId,
          workflowType.getDefaultExecutionConfig().getClass().getSimpleName(),
          entity.getDefaultExecutionConfig().getClass().getSimpleName()));
    }
  }
}
