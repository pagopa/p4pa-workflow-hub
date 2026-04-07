package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdate;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.massive.MassiveIbanUpdateActivity;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.massive.config.MassiveDebtPositionWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_RESERVED_SYNC)
public class MassiveIbanUpdateWFImpl implements MassiveIbanUpdateWF, ApplicationContextAware {

  private MassiveIbanUpdateActivity massiveIbanUpdateActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    MassiveDebtPositionWFConfig wfConfig = applicationContext.getBean(MassiveDebtPositionWFConfig.class);

    massiveIbanUpdateActivity = wfConfig.buildMassiveIbanUpdateActivityStub();
  }

  @Override
  public void massiveIbanUpdate(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban) {
    log.info("Start MassiveIbanUpdate Workflow for debtPositionTypeOrgId {} or organizationId {}", dptoId, orgId);

    massiveIbanUpdateActivity.massiveIbanUpdateRetrieveAndUpdateDp(orgId, dptoId, newIban, oldIban, oldPostalIban, newPostalIban);
    //TODO - reschedule with P4ADEV-4535
  }

}
