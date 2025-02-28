package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import com.nimbusds.jose.util.Pair;
import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.payhub.activities.util.DebtPositionUtilities;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.CancelCheckDpExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Common DebtPosition synchronization logic
 */
@Slf4j
public abstract class BaseDPSynchronizeWf implements ApplicationContextAware {

  protected FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivity;
  protected PublishPaymentEventActivity publishPaymentEventActivity;
  protected SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivity;
  protected CancelCheckDpExpirationScheduleActivity cancelCheckDpExpirationScheduleActivity;
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
    cancelCheckDpExpirationScheduleActivity = wfConfig.buildCancelCheckDpExpirationScheduleActivityStub();

    CheckDebtPositionExpirationWfConfig debtPositionExpirationWfConfig = applicationContext.getBean(CheckDebtPositionExpirationWfConfig.class);
    scheduleCheckDpExpirationActivity = debtPositionExpirationWfConfig.buildScheduleCheckDpExpirationActivityStub();

    buildActivities(wfConfig);
  }

  /**
   * to be overridden by extended class in order to build further required activities
   */
  protected void buildActivities(SynchronizeDebtPositionWfConfig wfConfig) {
  }

  /**
   * For each TO_SYNC installment, it will call {@link #synchronizeInstallment(DebtPositionDTO, InstallmentDTO)} method, publishing an event in case of error.<BR />
   * Next it will call {@link #finalizeDebtPositionSyncStatusActivity}
   */
  protected void synchronizeDebtPosition(DebtPositionDTO requestedDebtPosition, PaymentEventType paymentEventType) {
    Long debtPositionId = requestedDebtPosition.getDebtPositionId();
    log.info("Synchronizing DebtPosition {} using Activity class {}", debtPositionId, getClass().getSimpleName());

    Map<String, IupdSyncStatusUpdateDTO> finalizeStatusRequest = processToSyncInstallments(requestedDebtPosition);
    DebtPositionDTO finalizedDebtPositionDTO = finalizeSyncStatus(requestedDebtPosition, finalizeStatusRequest);
    publishEvent(paymentEventType, finalizedDebtPositionDTO);
    callIONotificationActivity(requestedDebtPosition, finalizeStatusRequest);
    scheduleExpirationWF(finalizedDebtPositionDTO, debtPositionId);

    log.info("DebtPosition synchronized {}", debtPositionId);
  }

  protected Map<String, IupdSyncStatusUpdateDTO> processToSyncInstallments(DebtPositionDTO debtPosition) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> InstallmentDTO.StatusEnum.TO_SYNC.equals(installment.getStatus()))
      .map(installment -> {
          try {
            return Pair.of(installment.getIud(), synchronizeInstallment(debtPosition, installment));
          } catch (Exception e) {
            String errorMessage = "Error occurred while synchronizing Installment with IUD: " + installment.getIud() + " for DebtPosition ID: " + debtPosition.getDebtPositionId() + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            publishPaymentEventActivity.publish(debtPosition, PaymentEventType.SYNC_ERROR, errorMessage);
            return null;
          }
        }
      )
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  /**
   * It will build a ready to be used {@link IupdSyncStatusUpdateDTO} starting from the input installment
   */
  protected IupdSyncStatusUpdateDTO buildIupdSyncStatusUpdateDTO(InstallmentDTO installmentDTO) {
    return IupdSyncStatusUpdateDTO.builder()
      .newStatus(IupdSyncStatusUpdateDTO.NewStatusEnum.valueOf(Objects.requireNonNull(installmentDTO.getSyncStatus()).getSyncStatusTo().name()))
      .build();
  }

  /**
   * It will synchronize an Installment
   */
  protected abstract IupdSyncStatusUpdateDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment);

  protected DebtPositionDTO finalizeSyncStatus(DebtPositionDTO requestedDebtPosition, Map<String, IupdSyncStatusUpdateDTO> iupdSyncStatusUpdateDTOMap) {
    Long debtPositionId = requestedDebtPosition.getDebtPositionId();
    if (!CollectionUtils.isEmpty(iupdSyncStatusUpdateDTOMap)) {
      log.info("Finalizing sync statuses of debtPosition {}: {}", debtPositionId, iupdSyncStatusUpdateDTOMap);
      return finalizeDebtPositionSyncStatusActivity.finalizeDebtPositionSyncStatus(debtPositionId, iupdSyncStatusUpdateDTOMap);
    } else {
      log.info("No sync statuses to finalized for debtPosition {}", debtPositionId);
      return requestedDebtPosition;
    }
  }

  protected void publishEvent(PaymentEventType paymentEventType, DebtPositionDTO finalizedDebtPositionDTO) {
    if (paymentEventType != null) {
      log.info("Publishing event {} on debtPosition {}", paymentEventType, finalizedDebtPositionDTO.getDebtPositionId());
      publishPaymentEventActivity.publish(finalizedDebtPositionDTO, paymentEventType, null);
    }
  }

  protected void callIONotificationActivity(DebtPositionDTO requestedDebtPosition, Map<String, IupdSyncStatusUpdateDTO> iupdSyncStatusUpdateDTOMap) {
    Long debtPositionId = requestedDebtPosition.getDebtPositionId();
    if (!CollectionUtils.isEmpty(iupdSyncStatusUpdateDTOMap)) {
      log.info("Calling notifyIO activity on debtPosition {} (organizationId {}, debtPositionTypeOrgId {})", debtPositionId, requestedDebtPosition.getOrganizationId(), requestedDebtPosition.getDebtPositionTypeOrgId());
      sendDebtPositionIONotificationActivity.sendMessage(requestedDebtPosition, iupdSyncStatusUpdateDTOMap);
    } else {
      log.info("Nothing to notifyIO on debtPosition {}", debtPositionId);
    }
  }

  protected void scheduleExpirationWF(DebtPositionDTO finalizedDebtPositionDTO, Long debtPositionId) {
    cancelCheckDpExpirationScheduleActivity.cancel(debtPositionId);
    LocalDate nextDueDate = DebtPositionUtilities.calcDebtPositionNextDueDate(finalizedDebtPositionDTO);
    if (nextDueDate != null) {
      scheduleCheckDpExpirationActivity.scheduleNextCheckDpExpiration(debtPositionId, nextDueDate.plusDays(1));
    }
  }
}
