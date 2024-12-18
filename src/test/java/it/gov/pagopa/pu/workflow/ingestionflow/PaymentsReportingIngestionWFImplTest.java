package it.gov.pagopa.pu.workflow.ingestionflow;

import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivityImpl;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivityImpl;
import it.gov.pagopa.payhub.activities.activity.utility.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.WorkflowApplication;
import it.gov.pagopa.pu.workflow.local.SpringValuesLocalActivity;
import it.gov.pagopa.pu.workflow.local.SpringValuesLocalActivityImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WorkflowApplication.class)
public class PaymentsReportingIngestionWFImplTest {

  private TestWorkflowEnvironment testEnv;
  private Worker worker;
  private PaymentsReportingIngestionWF workflow;
  private PaymentsReportingIngestionFlowFileActivity mockFileActivity;
  private SendEmailIngestionFlowActivity mockEmailActivity;
  private UpdateIngestionFlowStatusActivity mockStatusActivity;
  private SpringValuesLocalActivity mockSpringValuesLocalActivity;

  @Value("${workflow.queue:PaymentsReportingIngestionWF}")
  private String workflowQueue;

  @BeforeEach
  public void setUp() {
    if (workflowQueue == null || workflowQueue.isEmpty()) {
      throw new IllegalArgumentException("Task queue should not be an empty string");
    }

    testEnv = TestWorkflowEnvironment.newInstance();
    WorkerFactory factory = testEnv.getWorkerFactory();
    worker = factory.newWorker(workflowQueue);

    mockFileActivity = mock(PaymentsReportingIngestionFlowFileActivityImpl.class);
    //  mockEmailActivity = mock(SendEmailIngestionFlowActivity.class);
    mockStatusActivity = mock(UpdateIngestionFlowStatusActivityImpl.class);
    mockSpringValuesLocalActivity = mock(SpringValuesLocalActivityImpl.class);

    worker.registerWorkflowImplementationTypes(PaymentsReportingIngestionWFImpl.class);
    worker.registerActivitiesImplementations(mockFileActivity,
      //mockEmailActivity,
      mockStatusActivity,
      mockSpringValuesLocalActivity);

    factory.start();

    workflow = testEnv.getWorkflowClient().newWorkflowStub(
      PaymentsReportingIngestionWF.class,
      WorkflowOptions.newBuilder()
        .setTaskQueue(workflowQueue)
        .build()
    );
  }

  @AfterEach
  public void tearDown() {
    testEnv.close();
  }


  /**
   * Test the successful ingestion of a file
   */
  @Test
  public void testIngestSuccess() {
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(true);

    when(mockFileActivity.processFile(anyLong())).thenReturn(result);

    HashMap<String, String> props = buildProperties();


    when(mockSpringValuesLocalActivity.getProperties()).thenReturn(props);

    workflow.ingest(1L);

    verify(mockFileActivity, times(1)).processFile(1L);
    //  verify(mockEmailActivity, times(1)).sendEmail(1L, true);
    verify(mockStatusActivity, times(1)).updateStatus(1L, "OK");
  }


  /**
   * Test the ingest method when the file processing fails
   */

  @Test
  public void testIngestFailure() {
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(false);

    when(mockFileActivity.processFile(anyLong())).thenReturn(result);

    HashMap<String, String> props = buildProperties();
    when(mockSpringValuesLocalActivity.getProperties()).thenReturn(props);

    workflow.ingest(1L);

    verify(mockFileActivity, times(1)).processFile(1L);
    //  verify(mockEmailActivity, times(1)).sendEmail(1L, false);
    verify(mockStatusActivity, times(1)).updateStatus(1L, "KO");
  }


  /**
   * Helper method to build the properties map
   *
   * @return the properties map
   */
  private HashMap<String, String> buildProperties() {
    HashMap<String, String> props = new HashMap<String, String>();
    props.put("startToCloseTimeoutInSeconds", "1");
    props.put("retryInitialIntervalInMillis", "1000");
    props.put("retryBackoffCoefficient", "1.1");
    props.put("retryMaximumAttempts", "5");
    props.put("workflowQueue", "PaymentsReportingIngestionWF");
    return props;
  }
}

