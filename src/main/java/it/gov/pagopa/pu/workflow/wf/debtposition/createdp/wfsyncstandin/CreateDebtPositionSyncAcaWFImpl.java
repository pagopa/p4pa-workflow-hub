package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsyncstandin;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.AcaStandInCreateDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.config.CreateDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsync.CreateDebtPositionSyncWFImpl.TASK_QUEUE;


@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class CreateDebtPositionSyncAcaWFImpl implements CreateDebtPositionSyncAcaWF, ApplicationContextAware {

  public static final String TASK_QUEUE = "CreateDebtPositionSyncAcaWF";

  private AcaStandInCreateDebtPositionActivity acaStandInCreateDebtPositionActivity;
  private FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivity;
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CreateDebtPositionWfConfig wfConfig = applicationContext.getBean(CreateDebtPositionWfConfig.class);
    acaStandInCreateDebtPositionActivity = wfConfig.buildAcaStandInCreateDebtPositionActivityStub();
    finalizeDebtPositionSyncStatusActivity = wfConfig.buildFinalizeDebtPositionSyncStatusActivityStub();
    sendDebtPositionIONotificationActivity = wfConfig.buildSendDebtPositionIONotificationActivityStub();
  }

  @Override
  public void createDPSyncAca(DebtPositionDTO debtPosition) {
    log.info("Starting workflow for creating an Aca DebtPosition with ID: {}", debtPosition.getDebtPositionId());
    Map<String, IupdSyncStatusUpdateDTO> iupdSyncStatusUpdateDTOMap = acaStandInCreateDebtPositionActivity.createAcaDebtPosition(debtPosition);

    DebtPositionDTO debtPositionDTO = finalizeDebtPositionSyncStatusActivity.finalizeDebtPositionSyncStatus(debtPosition.getDebtPositionId(), iupdSyncStatusUpdateDTOMap);
    log.info("Sync status updated for IUD/IupdPagoPa and new statuses: {}", iupdSyncStatusUpdateDTOMap);

    sendDebtPositionIONotificationActivity.sendMessage(debtPositionDTO);
    log.info("Message sent to IO for organizationId {} and debtPositionTypeOrgId {}", debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId());
  }
}
