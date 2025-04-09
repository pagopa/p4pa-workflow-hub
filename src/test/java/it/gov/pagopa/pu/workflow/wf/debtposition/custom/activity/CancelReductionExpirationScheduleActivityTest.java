package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancelReductionExpirationScheduleActivityTest {

  @Mock
  private WorkflowService workflowServiceMock;

  private CancelReductionExpirationScheduleActivity activity;

  @BeforeEach
  void init() {
    activity = new CancelReductionExpirationScheduleActivityImpl(workflowServiceMock);
  }

  @Test
  void whenCancelSchedulingThenOk(){
    String workflowId = "workflowId";

    activity.cancelScheduling(workflowId);

    verify(workflowServiceMock).cancelWorkflow(workflowId);
  }
}
