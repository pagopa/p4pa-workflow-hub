package it.gov.pagopa.pu.workflow.utilities.faker;

import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UtilitiesTest {

  @Test
  void whenGenerateWorkflowIdThenOk(){
    String workflowId = Utilities.generateWorkflowId(1L, "workflow");

    assertEquals("workflow-1", workflowId);
  }

  @Test
  void givenGenerateWorkflowIdWhenIdNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(null, "workflow");
  }

  @Test
  void givenGenerateWorkflowIdWhenWorkflowNullThenThrowWorkflowInternalErrorException(){
    testGenerateWorkflowIdWhenNullErrors(1L, null);
  }

  private static void testGenerateWorkflowIdWhenNullErrors(Long id, String workflow) {
    WorkflowInternalErrorException exception = assertThrows(
      WorkflowInternalErrorException.class,
      () -> Utilities.generateWorkflowId(id, workflow)
    );

    assertEquals("The ID or the workflow must not be null", exception.getMessage());
  }
}
