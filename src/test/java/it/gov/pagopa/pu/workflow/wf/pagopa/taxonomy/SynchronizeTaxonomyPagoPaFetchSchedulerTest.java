package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy;

import io.temporal.client.schedules.ScheduleHandle;
import it.gov.pagopa.pu.workflow.service.WorkflowScheduleService;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch.SynchronizeTaxonomyPagoPaFetchWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SynchronizeTaxonomyPagoPaFetchSchedulerTest {

  @Mock
  private WorkflowScheduleService workflowScheduleServiceMock;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowScheduleServiceMock);
  }

  @Test
  void givenServiceCreationThenInvokeSchedule(){
    // Given
    String cronExpression = "cron";

    ScheduleHandle expectedResult = Mockito.mock(ScheduleHandle.class);
    Mockito.when(workflowScheduleServiceMock.schedule(
        SynchronizeTaxonomyPagoPaFetchScheduler.SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH_SCHEDULEID,
        SynchronizeTaxonomyPagoPaFetchWF.class,
        SynchronizeTaxonomyPagoPaFetchWFImpl.TASK_QUEUE_SYNCHRONIZE_TAXONOMY_PAGOPA_FETCH,
        cronExpression
      ))
      .thenReturn(expectedResult);

    // When
    SynchronizeTaxonomyPagoPaFetchScheduler scheduler = new SynchronizeTaxonomyPagoPaFetchScheduler(workflowScheduleServiceMock, cronExpression);
    ScheduleHandle result = scheduler.getSchedule();

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
