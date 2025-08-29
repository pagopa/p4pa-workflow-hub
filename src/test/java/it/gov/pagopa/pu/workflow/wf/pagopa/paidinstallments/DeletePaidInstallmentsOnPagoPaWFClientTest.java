package it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.wf.DeletePaidInstallmentsOnPagoPaWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paidinstallments.wf.DeletePaidInstallmentsOnPagoPaWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DeletePaidInstallmentsOnPagoPaWFClientTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private DeletePaidInstallmentsOnPagoPaWF wfMock;

  private DeletePaidInstallmentsOnPagoPaWFClient client;

  @BeforeEach
  void setUp() {
    client = new DeletePaidInstallmentsOnPagoPaWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void testDeletePaidInstallmentsOnPagoPa() {
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    Long receiptId = 2L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("DeletePaidInstallmentsOnPagoPaWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(DeletePaidInstallmentsOnPagoPaWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, debtPositionDTO,  receiptId);

    // When
    WorkflowCreatedDTO result = client.deletePaidInstallments(debtPositionDTO, receiptId);

    // Then
    assertEquals(expectedResult, result);
    verify(wfMock).deletePaidInstallments(debtPositionDTO, receiptId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, DeletePaidInstallmentsOnPagoPaWFImpl.class);
  }
}
