package it.gov.pagopa.pu.workflow.wf.exportfile.wfexportfile;

import io.micrometer.common.util.StringUtils;
import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.exportflow.UpdateExportFileStatusActivity;
import it.gov.pagopa.payhub.activities.activity.exportflow.debtposition.ExportFileActivity;
import it.gov.pagopa.payhub.activities.dto.exportflow.ExportFileResult;
import it.gov.pagopa.payhub.activities.dto.exportflow.UpdateStatusRequest;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFileStatus;
import it.gov.pagopa.pu.workflow.wf.exportfile.activity.ScheduleExportFileExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.exportfile.config.ExportFileWFConfig;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = ExportFileWFImpl.TASK_QUEUE_EXPORT_FILE_WF)
public class ExportFileWFImpl implements ExportFileWF, ApplicationContextAware {
  public static final String TASK_QUEUE_EXPORT_FILE_WF = "ExportFileWF";
  public static final String TASK_QUEUE_EXPORT_FILE_LOCAL_ACTIVITY = "ExportFileWF_LOCAL";

  @Value("${schedule.export-file-expiration.days}")
  private int expirationDays;

  private ExportFileActivity exportFileActivity;
  private UpdateExportFileStatusActivity updateExportFileStatusActivity;
  private ScheduleExportFileExpirationActivity scheduleExportFileExpirationActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ExportFileWFConfig wfConfig = applicationContext.getBean(ExportFileWFConfig.class);
    exportFileActivity = wfConfig.buildExportFileActivityStub();
    updateExportFileStatusActivity = wfConfig.buildUpdateExportFileStatusActivityStub();
    scheduleExportFileExpirationActivity = wfConfig.buildScheduleExportFileExpirationActivityStub();
  }

  @Override
  public void exportFile(Long exportFileId, ExportFileTypeEnum exportFileType) {
    log.info("Starting export file workflow for exportFileId: {}", exportFileId);
    boolean success = processExportFile(exportFileId, exportFileType);
    if(success){
      scheduleExportFileExpiration(exportFileId);
    }
    //TODO sendEmailActivity will be added with the task P4ADEV-2597
    log.info("Completed export file workflow for exportFileId: {}", exportFileId);
  }

  private boolean processExportFile(Long exportFileId, ExportFileTypeEnum exportFileType) {
    updateExportFileStatus(UpdateStatusRequest.builder()
      .exportFileId(exportFileId)
      .oldStatus(ExportFileStatus.REQUESTED)
      .newStatus(ExportFileStatus.PROCESSING)
      .build());
    Pair<ExportFileResult,String> exportResultPair = executeExport(exportFileId, exportFileType);
    ExportFileResult exportFileResult = exportResultPair.getLeft();
    String errorDescription = exportResultPair.getRight();
    updateExportFileWithProcessingResult(exportFileId, errorDescription, exportFileResult);
    return StringUtils.isBlank(errorDescription);
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
        //TODO field fileSize depends on task P4ADEV-2598
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
    updateExportFileStatusActivity.updateStatus(updateStatusRequest);
  }

  private Pair<ExportFileResult,String> executeExport(Long exportFileId, ExportFileTypeEnum exportFileType){
    try {
      return Pair.of(exportFileActivity.executeExport(
          exportFileId, exportFileType),null);
    }catch(Exception e){
      return Pair.of(null,e.getMessage());
    }
  }

  private void scheduleExportFileExpiration(Long exportFileId) {
    scheduleExportFileExpirationActivity.scheduleExportFileExpiration(
      exportFileId,
      LocalDate.now().plusDays(expirationDays)
    );
  }
}
