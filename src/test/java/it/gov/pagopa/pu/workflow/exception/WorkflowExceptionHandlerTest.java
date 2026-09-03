package it.gov.pagopa.pu.workflow.exception;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import it.gov.pagopa.payhub.activities.exception.ingestionflow.IngestionFlowTypeNotSupportedException;
import it.gov.pagopa.pu.workflow.exception.common.CommonExceptionHandlerTest;
import it.gov.pagopa.pu.workflow.exception.custom.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class WorkflowExceptionHandlerTest extends CommonExceptionHandlerTest {

  @Test
  void handleWorkflowExecutionAlreadyStarted() throws Exception {
    doThrow(new WorkflowExecutionAlreadyStarted(mock(WorkflowExecution.class), null, null)).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("workflowId='null', runId='null'"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleWorkflowConflict() throws Exception {
    doThrow(new WorkflowConflictException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WF_ALREADY_EXISTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleWorkflowNotFoundException() throws Exception {
    doThrow(new WorkflowNotFoundException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleWorkflowTypeNotFoundException() throws Exception {
    doThrow(new WorkflowTypeNotFoundException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_TYPE_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleTemporalWorkflowNotFoundException() throws Exception {
    WorkflowExecution workflowExecution = mock(WorkflowExecution.class);
    doThrow(new io.temporal.client.WorkflowNotFoundException(workflowExecution, null, null)).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("workflowId='null', runId='null'"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleIngestionFlowTypeNotSupportedException() throws Exception {
    doThrow(new IngestionFlowTypeNotSupportedException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_INGESTION_FLOW_FILE_NOT_SUPPORTED"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INGESTION_FLOW_FILE_TYPE_NOT_SUPPORTED"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidWfExecutionConfigException() throws Exception {
    doThrow(new InvalidWfExecutionConfigException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_INVALID_SYNC_DP_WF_EXECUTION_CONFIG"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInternalError() throws Exception {
    doThrow(new WorkflowInternalErrorException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("WORKFLOW_INTERNAL_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleTooManyAttemptsException() throws Exception {
    doThrow(new TooManyAttemptsException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isRequestTimeout())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("WORKFLOW_REQUEST_TIMEOUT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("TOO_MANY_ATTEMPTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

}
