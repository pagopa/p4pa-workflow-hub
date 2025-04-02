package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION)
public class FineReductionOptionExpirationWFImpl implements FineReductionOptionExpirationWF, ApplicationContextAware {

  public static final String TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION = "FineReductionOptionExpirationWF";

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    // TODO https://pagopa.atlassian.net/browse/P4ADEV-2494
  }

  public String handleFineReductionExpiration(Long debtPositionId, PaymentEventRequestDTO paymentEventRequestDTO, FineWfExecutionConfig executionParams) {
    // TODO https://pagopa.atlassian.net/browse/P4ADEV-2494
    return "";
  }
}
