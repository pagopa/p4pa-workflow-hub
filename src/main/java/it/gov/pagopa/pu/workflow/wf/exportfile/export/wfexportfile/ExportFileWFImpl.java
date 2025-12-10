package it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile;

import io.micrometer.common.util.StringUtils;
import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileActivity;
import it.gov.pagopa.payhub.activities.activity.exportflow.UpdateExportFileStatusActivity;
import it.gov.pagopa.payhub.activities.activity.exportflow.email.SendEmailExportFileActivity;
import it.gov.pagopa.payhub.activities.dto.exportflow.ExportFileResult;
import it.gov.pagopa.payhub.activities.dto.exportflow.UpdateStatusRequest;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFileStatus;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.dto.ExportDataDTO;
import it.gov.pagopa.pu.workflow.enums.DataEventType;
import it.gov.pagopa.pu.workflow.event.dataevents.dto.DataEventRequestDTO;
import it.gov.pagopa.pu.workflow.event.dataevents.producer.DataEventsProducerService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.activity.ScheduleExportFileExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.config.ExportFileWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.LocalDate;

import static it.gov.pagopa.payhub.activities.util.Utilities.toOffsetDateTimeEndOfTheDay;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_EXPORT_MEDIUM_PRIORITY)
public class ExportFileWFImpl implements ExportFileWF, ApplicationContextAware {

  @Value("${schedule.export-file-expiration.days}")
  private int expirationDays;

  private ExportFileActivity exportFileActivity;
  private UpdateExportFileStatusActivity updateExportFileStatusActivity;
  private SendEmailExportFileActivity sendEmailExportFileActivity;
  private ScheduleExportFileExpirationActivity scheduleExportFileExpirationActivity;
  private DataEventsProducerService dataEventsProducerService;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ExportFileWFConfig wfConfig = applicationContext.getBean(ExportFileWFConfig.class);
    exportFileActivity = wfConfig.buildExportFileActivityStub();
    updateExportFileStatusActivity = wfConfig.buildUpdateExportFileStatusActivityStub();
    sendEmailExportFileActivity = wfConfig.buildSendEmailExportFileActivityStub();
    scheduleExportFileExpirationActivity = wfConfig.buildScheduleExportFileExpirationActivityStub();
    dataEventsProducerService = applicationContext.getBean(DataEventsProducerService.class);
  }

  @Override
  public void exportFile(Long exportFileId, ExportFileTypeEnum exportFileType) {
    log.info("Starting export file workflow for exportFileId: {}", exportFileId);
    updateExportFileStatus(UpdateStatusRequest.builder()
      .exportFileId(exportFileId)
      .oldStatus(ExportFileStatus.REQUESTED)
      .newStatus(ExportFileStatus.PROCESSING)
      .build());

    ExportFileResult exportFileResult = null;
    String errorDescription = null;
    try {
      exportFileResult = exportFileActivity.executeExport(
        exportFileId, exportFileType);
    } catch(Exception e){
      errorDescription = Utilities.getWorkflowExceptionMessage(e);
    }

    updateExportFileWithProcessingResult(exportFileId, errorDescription, exportFileResult);

    if(StringUtils.isBlank(errorDescription) && exportFileResult!=null && exportFileResult.getExportDate()!=null){
      scheduleExportFileExpiration(exportFileId, exportFileResult.getExportDate().plusDays(expirationDays));
      publishDataEvent(exportFileId, exportFileResult, exportFileType);
      sendEmailExportFileActivity.sendExportCompletedEmail(exportFileId, true);
    }else {
      sendEmailExportFileActivity.sendExportCompletedEmail(exportFileId, false);
    }

    log.info("Completed export file workflow for exportFileId: {}", exportFileId);
  }

  private void updateExportFileWithProcessingResult(Long exportFileId, String errorDescription,
    ExportFileResult exportFileResult) {
    UpdateStatusRequest updateStatusRequest;
    if(StringUtils.isBlank(errorDescription)){
      updateStatusRequest = UpdateStatusRequest.builder()
        .exportFileId(exportFileId)
        .oldStatus(ExportFileStatus.PROCESSING)
        .newStatus(ExportFileStatus.COMPLETED)
        .filePathName(exportFileResult.getFilePath())
        .fileName(exportFileResult.getFileName())
        .fileSize(exportFileResult.getFileSize())
        .expirationDate(toOffsetDateTimeEndOfTheDay(exportFileResult.getExportDate().plusDays(expirationDays)))
        .exportedRows(exportFileResult.getExportedRows())
        .build();
    }else{
      updateStatusRequest = UpdateStatusRequest.builder()
        .exportFileId(exportFileId)
        .oldStatus(ExportFileStatus.PROCESSING)
        .newStatus(ExportFileStatus.ERROR)
        .errorDescription(errorDescription)
        .build();
    }
    updateExportFileStatus(updateStatusRequest);
  }

  private void updateExportFileStatus(UpdateStatusRequest updateStatusRequest) {
    updateExportFileStatusActivity.updateExportStatus(updateStatusRequest);
  }

  private void scheduleExportFileExpiration(Long exportFileId, LocalDate expirationDate) {
    scheduleExportFileExpirationActivity.scheduleExportFileExpiration(
      exportFileId,
      expirationDate
    );
  }

  private void publishDataEvent(Long exportFileId, ExportFileResult result, ExportFileTypeEnum exportFileType) {
    dataEventsProducerService.notifyExportEvent(
      ExportDataDTO.builder()
        .exportFileId(exportFileId)
        .organizationId(result.getOrganizationId())
        .exportedRows(result.getExportedRows())
        .fileSize(result.getFileSize())
        .exportFileType(exportFileType)
        .operatorExternalUserId(result.getOperatorExternalId())
        .build(),
      DataEventRequestDTO.builder()
        .dataEventType(DataEventType.EXPORT_FILE)
        .eventDescription("Export file having type "+exportFileType.name())
        .build());
  }
}
