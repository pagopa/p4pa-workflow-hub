package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.IONotificationDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.config.SyncDpIONotificationWFConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_DP_LOW_PRIORITY)
public class SyncDpIONotificationWFImpl implements SyncDpIONotificationWF, ApplicationContextAware {

    private IONotificationDebtPositionActivity ioNotificationDebtPositionActivity;
    private PublishPaymentEventActivity publishPaymentEventActivity;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SyncDpIONotificationWFConfig wfConfig = applicationContext.getBean(SyncDpIONotificationWFConfig.class);
        ioNotificationDebtPositionActivity = wfConfig.buildIoNotificationDebtPositionActivityStub();
        publishPaymentEventActivity = wfConfig.buildPublishPaymentEventActivityStub();
    }

    @Override
    public void sendIoNotification(DebtPositionDTO debtPositionDTO, Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap, GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessages) {
        log.info("Calling notifyIO activity on debtPosition {} (organizationId {}, debtPositionTypeOrgId {})",
                debtPositionDTO.getDebtPositionId(), debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId());
        DebtPositionIoNotificationDTO ioNotifications = ioNotificationDebtPositionActivity.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessages);
        if (ioNotifications != null) {
            publishPaymentEventActivity.publishDebtPositionIoNotificationEvent(ioNotifications, new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, null));
        }
    }
}

