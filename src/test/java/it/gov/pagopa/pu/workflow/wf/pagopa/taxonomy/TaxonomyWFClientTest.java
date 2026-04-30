package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxonomyWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private SynchronizeTaxonomyPagoPaFetchWF wfMock;

  private TaxonomyWFClient client;

  @BeforeEach
  void init() {
    client = new TaxonomyWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenSynchronizeTaxonomyThenOk() {
    // Given
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("SynchronizeTaxonomyPagoPaFetchWF-ON-DEMAND", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(SynchronizeTaxonomyPagoPaFetchWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult);

    // When
    WorkflowCreatedDTO result = client.synchronizeTaxonomy();

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).synchronize();

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, SynchronizeTaxonomyPagoPaFetchWFImpl.class);
  }
}
