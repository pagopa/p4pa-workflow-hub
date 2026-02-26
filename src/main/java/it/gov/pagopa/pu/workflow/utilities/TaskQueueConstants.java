package it.gov.pagopa.pu.workflow.utilities;

/** @see <a href="https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1690697820/Gestione+esecuzione+concorrente+dei+WorkFlow+TaskQueue+e+numero+di+Poller#4.1.-Relazione-Workflow%2FTask-Queue">Confluence page</a>*/
public class TaskQueueConstants {
  private TaskQueueConstants(){}

//region generic taskQueue
  public static final String TASK_QUEUE_LOW_PRIORITY = "LowPriorityWF";
//endregion

//region DebtPosition domain
  public static final String TASK_QUEUE_DP_RESERVED_SYNC = "DebtPositionSyncWF";
  public static final String TASK_QUEUE_DP_RESERVED_SYNC_LOCAL = "DebtPositionSyncWF_LOCAL";

  public static final String TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC = "DebtPositionCustomSyncWF";
  public static final String TASK_QUEUE_DP_RESERVED_CUSTOM_SYNC_LOCAL = "DebtPositionCustomSyncWF_LOCAL";

  public static final String TASK_QUEUE_DP_LOW_PRIORITY = "DebtPositionWF";
//endregion

//region ImportData domain
  public static final String TASK_QUEUE_IMPORT_MEDIUM_PRIORITY = "IngestionFlowFileWF";
  public static final String TASK_QUEUE_IMPORT_MEDIUM_PRIORITY_LOCAL = "IngestionFlowFileWF_LOCAL";
//endregion

//region ExportData domain
  public static final String TASK_QUEUE_EXPORT_MEDIUM_PRIORITY = "ExportFileWF";
  public static final String TASK_QUEUE_EXPORT_MEDIUM_PRIORITY_LOCAL = "ExportFileWF_LOCAL";
//endregion

//region Classification domain
  public static final String TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY = "ClassificationWF";
  public static final String TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY_LOCAL = "ClassificationWF_LOCAL";
//endregion

//region SEND domain
  public static final String TASK_QUEUE_SEND_RESERVED_NOTIFICATION = "SendNotificationProcessWF";

  public static final String TASK_QUEUE_SEND_RESERVED_STREAM = "SendNotificationStreamConsumeWF";

  public static final String TASK_QUEUE_SEND_RESERVED_PUBLISH_PAYMENT_EVENT_LOCAL = "SendNotificationPublishPaymentEventSendWF_LOCAL";

//endregion

//region Assessments domain
  public static final String TASK_QUEUE_ASSESSMENTS_RESERVED_CREATION = "AssessmentCreationWF";
  public static final String TASK_QUEUE_ASSESSMENTS = "AssessmentsWF";
  public static final String TASK_QUEUE_ASSESSMENTS_CLASSIFICATION = "AssessmentClassificationWF";
//endregion
}
