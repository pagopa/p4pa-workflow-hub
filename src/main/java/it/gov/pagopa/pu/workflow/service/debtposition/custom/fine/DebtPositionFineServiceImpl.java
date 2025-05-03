package it.gov.pagopa.pu.workflow.service.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.config.WfExecutionConfigHandlerService;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionFineServiceImpl implements DebtPositionFineService {

  private final DebtPositionFineClient debtPositionFineClient;
  private final WfExecutionConfigHandlerService wfExecutionConfigHandlerService;

  public DebtPositionFineServiceImpl(DebtPositionFineClient debtPositionFineClient, WfExecutionConfigHandlerService wfExecutionConfigHandlerService) {
    this.debtPositionFineClient = debtPositionFineClient;
    this.wfExecutionConfigHandlerService = wfExecutionConfigHandlerService;
  }

  @Override
  public WorkflowCreatedDTO expireFineReduction(Long debtPositionId) {
    log.debug("Fetching fine execution config for debtPositionId: {}", debtPositionId);
    FineWfExecutionConfig executionParams = wfExecutionConfigHandlerService.findStoredExecutionConfig(debtPositionId, FineWfExecutionConfig.class);

    log.debug("Starting workflow to handle fine reduction expiration: {})", debtPositionId);
    return debtPositionFineClient.expireFineReduction(debtPositionId, executionParams);
  }
}
