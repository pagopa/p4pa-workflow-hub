package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import io.temporal.client.WorkflowClient;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWFImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.workflow.utilities.Utilities.generateWorkflowId;

@Slf4j
@Service
public class DebtPositionFineClientImpl implements DebtPositionFineClient {

  private final WorkflowService workflowService;

  public DebtPositionFineClientImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public String expireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig) {
    log.info("Starting check debt position reduction expiration WF: {}", debtPositionId);
    String taskQueue = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION;
    String workflowId = generateExpireFineReductionWorkflowId(debtPositionId);

    FineReductionOptionExpirationWF workflow = workflowService.buildWorkflowStub(
      FineReductionOptionExpirationWF.class,
      taskQueue,
      workflowId);
    WorkflowClient.start(workflow::expireFineReduction, debtPositionId, wfExecutionConfig);
    return workflowId;
  }

  @Override
  public String scheduleExpireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig, OffsetDateTime fineReductionExpirationDateTime) {
    log.info("Starting schedule to check debt position reduction expiration WF: {}", debtPositionId);
    String taskQueue = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION;
    String workflowId = generateExpireFineReductionWorkflowId(debtPositionId);

    FineReductionOptionExpirationWF workflow = workflowService.buildWorkflowStubScheduled(
      FineReductionOptionExpirationWF.class,
      taskQueue,
      workflowId,
      fineReductionExpirationDateTime);
    WorkflowClient.start(workflow::expireFineReduction, debtPositionId, wfExecutionConfig);
    return workflowId;
  }

  public static String generateExpireFineReductionWorkflowId(Long debtPositionId) {
    return generateWorkflowId(debtPositionId, FineReductionOptionExpirationWF.class);
  }
}
