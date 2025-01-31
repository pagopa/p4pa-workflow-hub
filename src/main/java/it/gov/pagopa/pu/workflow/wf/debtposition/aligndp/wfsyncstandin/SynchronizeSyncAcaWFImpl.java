package it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.producer.PaymentsProducerService;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin.SynchronizeSyncAcaWFImpl.TASK_QUEUE_SYNCHRONIZE_SYNC_ACA_WF;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_SYNCHRONIZE_SYNC_ACA_WF)
public class SynchronizeSyncAcaWFImpl implements SynchronizeSyncAcaWF, ApplicationContextAware {

  public static final String TASK_QUEUE_SYNCHRONIZE_SYNC_ACA_WF = "SynchronizeSyncAcaWF";

  private SynchronizeInstallmentAcaActivity synchronizeInstallmentAcaActivity;
  private FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivity;
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivity;

  private final PaymentsProducerService paymentsProducerService;

  public SynchronizeSyncAcaWFImpl(PaymentsProducerService paymentsProducerService) {
    this.paymentsProducerService = paymentsProducerService;
  }

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SynchronizeDebtPositionWfConfig wfConfig = applicationContext.getBean(SynchronizeDebtPositionWfConfig.class);
    synchronizeInstallmentAcaActivity = wfConfig.buildSynchronizeInstallmentAcaActivity();
    finalizeDebtPositionSyncStatusActivity = wfConfig.buildFinalizeDebtPositionSyncStatusActivityStub();
    sendDebtPositionIONotificationActivity = wfConfig.buildSendDebtPositionIONotificationActivityStub();
  }

  @Override
  public void synchronizeDPSyncAca(DebtPositionDTO debtPosition) {
    Map<String, IupdSyncStatusUpdateDTO> iupdSyncStatusUpdateDTOMap = invokeAcaStandInCreateDebtPositionActivity(debtPosition);

    DebtPositionDTO debtPositionDTO = finalizeDebtPositionSyncStatusActivity.finalizeDebtPositionSyncStatus(debtPosition.getDebtPositionId(), iupdSyncStatusUpdateDTOMap);
    log.info("Sync status updated for IUD/IupdPagoPa and new statuses: {}", iupdSyncStatusUpdateDTOMap);

    sendDebtPositionIONotificationActivity.sendMessage(debtPositionDTO);
    log.info("Message sent to IO for organizationId {} and debtPositionTypeOrgId {}", debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId());
  }

  private Map<String, IupdSyncStatusUpdateDTO> invokeAcaStandInCreateDebtPositionActivity(DebtPositionDTO debtPosition) {
    Map<String, IupdSyncStatusUpdateDTO> syncStatusMap = new HashMap<>();

    log.info("Starting workflow for synchronizing DebtPosition on ACA with ID: {}", debtPosition.getDebtPositionId());

    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().stream()
        .filter(installment -> InstallmentDTO.StatusEnum.TO_SYNC.equals(installment.getStatus()))
        .forEach(installment ->
          synchronizeInstallmentAca(debtPosition, installment.getIud(), syncStatusMap, Objects.requireNonNull(installment.getSyncStatus()).getSyncStatusTo()))
    );

    return syncStatusMap;
  }

  private void synchronizeInstallmentAca(DebtPositionDTO debtPosition, String iud, Map<String, IupdSyncStatusUpdateDTO> syncStatusMap, InstallmentSyncStatus.SyncStatusToEnum newStatus) {
    try {
      log.info("Synchronizing Installment with IUD: {} for DebtPosition ID: {}", iud, debtPosition.getDebtPositionId());

      synchronizeInstallmentAcaActivity.synchronizeInstallmentAca(debtPosition, iud);

      updateSyncStatusMap(IupdSyncStatusUpdateDTO.NewStatusEnum.valueOf(newStatus.name()), syncStatusMap, iud);
    } catch (Exception e) {
      log.error("Error occurred while synchronizing Installment with IUD: {} for DebtPosition ID: {}. Error: {}",
        iud, debtPosition.getDebtPositionId(), e.getMessage());

      paymentsProducerService.notifyPaymentsEvent(
        debtPosition,
        PaymentEventType.SYNC_ERROR,
        e.getMessage()
      );
    }
  }

  private void updateSyncStatusMap(IupdSyncStatusUpdateDTO.NewStatusEnum status, Map<String, IupdSyncStatusUpdateDTO> syncStatusMap, String iud) {
    syncStatusMap.put(iud, IupdSyncStatusUpdateDTO.builder()
      .newStatus(status)
      .iupdPagopa(null)
      .build());
  }
}
