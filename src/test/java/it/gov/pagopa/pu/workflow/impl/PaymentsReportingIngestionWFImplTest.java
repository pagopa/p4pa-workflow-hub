// PaymentsReportingIngestionWFImplTest.java
package it.gov.pagopa.pu.workflow.impl;

import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.UpdateIngestionFlowStatusActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.PaymentsReportingIngestionFlowFileActivity;
import it.gov.pagopa.payhub.activities.activity.utility.SendEmailIngestionFlowActivity;
import it.gov.pagopa.payhub.activities.dto.paymentsreporting.PaymentsReportingIngestionFlowFileActivityResult;
import it.gov.pagopa.pu.workflow.PaymentsReportingIngestionWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class PaymentsReportingIngestionWFImplTest {

  private TestWorkflowEnvironment testEnv;
  private Worker worker;
  private PaymentsReportingIngestionWF workflow;
  private PaymentsReportingIngestionFlowFileActivity mockFileActivity;
  private SendEmailIngestionFlowActivity mockEmailActivity;
  private UpdateIngestionFlowStatusActivity mockStatusActivity;

  @Value("${workflow.queue:PaymentsReportingIngestionWF}")
  private String workflowQueue;

  @BeforeEach
  public void setUp() {
    /*
    testEnv = TestWorkflowEnvironment.newInstance();
    WorkerFactory factory = testEnv.getWorkerFactory();
    worker = factory.newWorker(workflowQueue);

    mockFileActivity = mock(PaymentsReportingIngestionFlowFileActivity.class);
    mockEmailActivity = mock(SendEmailIngestionFlowActivity.class);
    mockStatusActivity = mock(UpdateIngestionFlowStatusActivity.class);

    worker.registerWorkflowImplementationTypes(PaymentsReportingIngestionWFImpl.class);
    worker.registerActivitiesImplementations(mockFileActivity, mockEmailActivity, mockStatusActivity);

    factory.start();


    workflow = testEnv.getWorkflowClient().newWorkflowStub(
      PaymentsReportingIngestionWF.class,
      WorkflowOptions.newBuilder()
        .setTaskQueue(workflowQueue)
        .build()
    );

     */
  }

  @AfterEach
  public void tearDown() {
  //  testEnv.close();
  }

  @Test
  public void testIngest() {

    /*
    PaymentsReportingIngestionFlowFileActivityResult result = new PaymentsReportingIngestionFlowFileActivityResult();
    result.setSuccess(true);

    when(mockFileActivity.processFile(anyLong())).thenReturn(result);

    workflow.ingest(1L);

    verify(mockFileActivity, times(1)).processFile(1L);
    verify(mockEmailActivity, times(1)).sendEmail("1", true);
    verify(mockStatusActivity, times(1)).updateStatus(1L, "OK");

     */

  }
}
