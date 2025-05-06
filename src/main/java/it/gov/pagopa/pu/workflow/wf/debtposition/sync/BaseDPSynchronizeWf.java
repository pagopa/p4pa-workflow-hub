package it.gov.pagopa.pu.workflow.wf.debtposition.sync;

import com.nimbusds.jose.util.Pair;
import it.gov.pagopa.payhub.activities.activity.debtposition.synchronize.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.IONotificationDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.util.DebtPositionUtilities;
import it.gov.pagopa.pu.debtposition.dto.generated.*;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.CancelCheckDpExpirationScheduleActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.HashMap;
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
  protected IONotificationDebtPositionActivity ioNotificationDebtPositionActivity;
  protected CancelCheckDpExpirationScheduleActivity cancelCheckDpExpirationScheduleActivity;
  protected ScheduleCheckDpExpirationActivity scheduleCheckDpExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SynchronizeDebtPositionWfConfig wfConfig = applicationContext.getBean(SynchronizeDebtPositionWfConfig.class);
    finalizeDebtPositionSyncStatusActivity = wfConfig.buildFinalizeDebtPositionSyncStatusActivityStub();
    publishPaymentEventActivity = wfConfig.buildPublishPaymentEventActivityStub();
    ioNotificationDebtPositionActivity = wfConfig.buildIONotificationDebtPositionActivityStub();
    cancelCheckDpExpirationScheduleActivity = wfConfig.buildCancelCheckDpExpirationScheduleActivityStub();
    scheduleCheckDpExpirationActivity = wfConfig.buildScheduleCheckDpExpirationActivityStub();

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
  protected void synchronizeDebtPosition(DebtPositionDTO requestedDebtPosition, PaymentEventRequestDTO paymentEventRequest, GenericWfExecutionConfig wfExecutionConfig) {
    Long debtPositionId = requestedDebtPosition.getDebtPositionId();
    log.info("Synchronizing DebtPosition {} using Activity class {}", debtPositionId, getClass().getSimpleName());

    SyncStatusUpdateRequestDTO finalizeStatusRequest = processToSyncInstallments(requestedDebtPosition);
    DebtPositionDTO finalizedDebtPositionDTO = finalizeSyncStatus(requestedDebtPosition, finalizeStatusRequest);
    publishEvent(paymentEventRequest, finalizedDebtPositionDTO);
    callIONotificationActivity(requestedDebtPosition, finalizeStatusRequest.getIupd2finalize(), wfExecutionConfig!=null? wfExecutionConfig.getIoMessages() : null);
    scheduleExpirationWF(finalizedDebtPositionDTO, debtPositionId);

    log.info("DebtPosition synchronized {}", debtPositionId);
  }

  protected SyncStatusUpdateRequestDTO processToSyncInstallments(DebtPositionDTO debtPosition) {
    SyncStatusUpdateRequestDTO out = new SyncStatusUpdateRequestDTO();
    out.setIupdSyncError(new HashMap<>());

    out.setIupd2finalize(debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(installment -> InstallmentStatus.TO_SYNC.equals(installment.getStatus()))
      .map(installment -> {
        String iud = installment.getIud();
        try {
            return Pair.of(iud, synchronizeInstallment(debtPosition, installment));
          } catch (Exception e) {
            String errorMessage = "Error occurred while synchronizing Installment with IUD: " + iud + " for DebtPosition ID: " + debtPosition.getDebtPositionId() + ". Error: " + e.getMessage();
            log.error(errorMessage, e);
            publishPaymentEventActivity.publishDebtPositionErrorEvent(debtPosition, new PaymentEventRequestDTO(PaymentEventType.SYNC_ERROR, errorMessage));
            out.getIupdSyncError().put(iud, new SyncErrorDTO(errorMessage));
            return null;
          }
        }
      )
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

    return out;
  }

  /**
   * It will build a ready to be used {@link SyncCompleteDTO} starting from the input installment
   */
  protected SyncCompleteDTO buildIupdSyncStatusUpdateDTO(InstallmentDTO installmentDTO) {
    return SyncCompleteDTO.builder()
      .newStatus(Objects.requireNonNull(installmentDTO.getSyncStatus()).getSyncStatusTo())
      .build();
  }

  /**
   * It will synchronize an Installment
   */
  protected abstract SyncCompleteDTO synchronizeInstallment(DebtPositionDTO debtPosition, InstallmentDTO installment);

  protected DebtPositionDTO finalizeSyncStatus(DebtPositionDTO requestedDebtPosition, SyncStatusUpdateRequestDTO syncStatusUpdateRequestDTO) {
    Long debtPositionId = requestedDebtPosition.getDebtPositionId();
    if (!CollectionUtils.isEmpty(syncStatusUpdateRequestDTO.getIupdSyncError())
      || !CollectionUtils.isEmpty(syncStatusUpdateRequestDTO.getIupd2finalize())) {
      log.info("Finalizing sync statuses of debtPosition {}: {}", debtPositionId, syncStatusUpdateRequestDTO);
      return finalizeDebtPositionSyncStatusActivity.finalizeDebtPositionSyncStatus(debtPositionId, syncStatusUpdateRequestDTO);
    } else {
      log.info("No sync statuses to finalize for debtPosition {}", debtPositionId);
      return requestedDebtPosition;
    }
  }

  protected void publishEvent(PaymentEventRequestDTO paymentEventRequest, DebtPositionDTO finalizedDebtPositionDTO) {
    if (paymentEventRequest != null) {
      log.info("Publishing event {} on debtPosition {}", paymentEventRequest.getPaymentEventType(), finalizedDebtPositionDTO.getDebtPositionId());
      publishPaymentEventActivity.publishDebtPositionEvent(finalizedDebtPositionDTO, paymentEventRequest);
    }
  }

  protected void callIONotificationActivity(DebtPositionDTO requestedDebtPosition, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
    Long debtPositionId = requestedDebtPosition.getDebtPositionId();
    if (!CollectionUtils.isEmpty(iudSyncCompleteDTOMap)) {
      log.info("Calling notifyIO activity on debtPosition {} (organizationId {}, debtPositionTypeOrgId {})", debtPositionId, requestedDebtPosition.getOrganizationId(), requestedDebtPosition.getDebtPositionTypeOrgId());
      DebtPositionIoNotificationDTO ioNotifications = ioNotificationDebtPositionActivity.sendIoNotification(requestedDebtPosition, iudSyncCompleteDTOMap, ioMessages);
      if(ioNotifications != null){
        publishPaymentEventActivity.publishDebtPositionIoNotificationEvent(ioNotifications, new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, null));
      }
    } else {
      log.info("Nothing to notifyIO on debtPosition {}", debtPositionId);
    }
  }

  protected void scheduleExpirationWF(DebtPositionDTO finalizedDebtPositionDTO, Long debtPositionId) {
    cancelCheckDpExpirationScheduleActivity.cancelExpirationSchedule(debtPositionId);
    LocalDate nextDueDate = DebtPositionUtilities.calcDebtPositionNextDueDate(finalizedDebtPositionDTO);
    if (nextDueDate != null) {
      scheduleCheckDpExpirationActivity.scheduleNextCheckDpExpiration(debtPositionId, nextDueDate.plusDays(1));
    }
  }
}
