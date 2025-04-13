package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionFineReductionOptionExpirationActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config.DebtPositionFineWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper.FineWfExecutionConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION)
public class FineReductionOptionExpirationWFImpl implements FineReductionOptionExpirationWF, ApplicationContextAware {

  public static final String TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION = "FineReductionOptionExpirationWF";

  private DebtPositionFineReductionOptionExpirationActivity debtPositionFineReductionOptionExpirationActivity;
  private InvokeSyncDebtPositionActivity invokeSyncDebtPositionActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    DebtPositionFineWfConfig wfConfig = applicationContext.getBean(DebtPositionFineWfConfig.class);

    debtPositionFineReductionOptionExpirationActivity = wfConfig.buildDebtPositionFineReductionOptionExpirationActivityStub();
    invokeSyncDebtPositionActivity = wfConfig.buildInvokeSyncDebtPositionActivityStub();
  }

  @Override
  public String expireFineReduction(Long debtPositionId, FineWfExecutionConfig wfExecutionConfig) {
    log.info("Handling fine reduction expiration for debtPositionId: {}, wfExecutionConfig: {}", debtPositionId, wfExecutionConfig);
    DebtPositionDTO debtPositionDTO = debtPositionFineReductionOptionExpirationActivity.handleFineReductionExpiration(debtPositionId);

    if (debtPositionDTO == null){
      log.warn("DebtPositionDTO not found for debtPositionId: {}", debtPositionId);
      return null;
    }

    // TODO replace IO placeholders https://pagopa.atlassian.net/browse/P4ADEV-2599
    log.info("Mapped FineWfExecutionConfig: {} to GenericWfExecutionConfig", wfExecutionConfig);
    GenericWfExecutionConfig genericWfExecutionConfig = FineWfExecutionConfigMapper.mapReductionExpired(wfExecutionConfig);

    log.info("Synchronize debt position sync");
    return invokeSyncDebtPositionActivity.synchronizeDPSync(debtPositionDTO, null, false, genericWfExecutionConfig);
  }
}
