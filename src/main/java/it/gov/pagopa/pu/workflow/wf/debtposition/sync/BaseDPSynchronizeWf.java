package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import com.nimbusds.jose.util.Pair;
import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.payhub.activities.util.DebtPositionUtilities;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Common DebtPosition synchronization logic */
@Slf4j
public abstract class BaseDPSynchronizeWf implements ApplicationContextAware {

  protected FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivity;
  protected PublishPaymentEventActivity publishPaymentEventActivity;
  protected SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivity;
  protected ScheduleCheckDpExpirationActivity scheduleCheckDpExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SynchronizeDebtPositionWfConfig wfConfig = applicationContext.getBean(SynchronizeDebtPositionWfConfig.class);
    finalizeDebtPositionSyncStatusActivity = wfConfig.buildFinalizeDebtPositionSyncStatusActivityStub();
    publishPaymentEventActivity = wfConfig.buildPublishPaymentEventActivityStub();
    sendDebtPositionIONotificationActivity = wfConfig.buildSendDebtPositionIONotificationActivityStub();

    CheckDebtPositionExpirationWfConfig debtPositionExpirationWfConfig = applicationContext.getBean(CheckDebtPositionExpirationWfConfig.class);
    scheduleCheckDpExpirationActivity = debtPositionExpirationWfConfig.buildScheduleCheckDpExpirationActivityStub();

    buildActivities(wfConfig);
  }

  /** to be overridden by extended class in order to build further required activities */
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
  }

  /** For each TO_SYNC installment, it will call {@link #synchronizeInstallment(DebtPositionDTO, InstallmentDTO)} method, publishing an event in case of error.<BR />
   * Next it will call {@link #finalizeDebtPositionSyncStatusActivity}
   * */
  protected void synchronizeDebtPosition(DebtPositionDTO debtPositionDTO, PaymentEventType paymentEventType){
    log.info("Synchronizing DebtPosition {} using Activity class {}", debtPositionDTO.getDebtPositionId(), getClass().getSimpleName());
    Map<String, IupdSyncStatusUpdateDTO> finalizeStatusRequest = processToSyncInstallments(debtPositionDTO);
    finalizeNotifyAndSetExpiration(debtPositionDTO.getDebtPositionId(), finalizeStatusRequest, paymentEventType);
  }

  protected Map<String, IupdSyncStatusUpdateDTO> processToSyncInstallments(DebtPositionDTO debtPosition) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> InstallmentDTO.StatusEnum.TO_SYNC.equals(installment.getStatus()))
      .map(installment -> {
          try {
            return Pair.of(installment.getIud(), synchronizeInstallment(debtPosition, installment));
          } catch (Exception e) {
            log.error("Error occurred while synchronizing Installment with IUD: {} for DebtPosition ID: {}. Error: {}",
              installment.getIud(), debtPosition.getDebtPositionId(), e.getMessage());
            publishPaymentEventActivity.publish(debtPosition, PaymentEventType.SYNC_ERROR, e.getMessage());
            return null;
          }
        }
      )
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  /** It will build a ready to be used {@link IupdSyncStatusUpdateDTO} starting from the input installment */
  protected IupdSyncStatusUpdateDTO buildIupdSyncStatusUpdateDTO(InstallmentDTO installmentDTO){
    return IupdSyncStatusUpdateDTO.builder()
      .newStatus(IupdSyncStatusUpdateDTO.NewStatusEnum.valueOf(Objects.requireNonNull(installmentDTO.getSyncStatus()).getSyncStatusTo().name()))
      .build();
  }

  /** It will synchronize an Installment */
  protected abstract IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment);

  /** It will finalize DP status, publish the requested event, call notifyIO activity and set Expiration WF */
  protected void finalizeNotifyAndSetExpiration(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> iupdSyncStatusUpdateDTOMap, PaymentEventType paymentEventType) {
    log.info("Finalizing sync statuses of debtPosition {}: {}", debtPositionId, iupdSyncStatusUpdateDTOMap);
    DebtPositionDTO debtPositionDTO = finalizeDebtPositionSyncStatusActivity.finalizeDebtPositionSyncStatus(debtPositionId, iupdSyncStatusUpdateDTOMap);

    if(paymentEventType!=null){
      log.info("Publishing event {} on debtPosition {}", paymentEventType, debtPositionDTO.getDebtPositionId());
      publishPaymentEventActivity.publish(debtPositionDTO, paymentEventType, null);
    }

    log.info("Calling notifyIO activity on debtPosition {} (organizationId {}, debtPositionTypeOrgId {})", debtPositionId, debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId());
    sendDebtPositionIONotificationActivity.sendMessage(debtPositionDTO);

    OffsetDateTime nextDueDate = DebtPositionUtilities.calcDebtPositionNextDueDate(debtPositionDTO);
    if(nextDueDate!=null){
      scheduleCheckDpExpirationActivity.scheduleNextCheckDpExpiration(debtPositionId, nextDueDate.plusDays(1));
    }

    log.info("DebtPosition synchronized {}", debtPositionId);
  }
}
