package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowCompletionService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkflowControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WorkflowService serviceMock;

  @MockitoBean
  private WorkflowCompletionService workflowCompletionServiceMock;

  @Test
  void whenGetWorkflowStatusThenOk() throws Exception {
    String workflowId = "workflow-1";
    WorkflowStatusDTO workflowStatusDTO = WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .status(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED)
      .build();

    Mockito.when(serviceMock.getWorkflowStatus(workflowId))
      .thenReturn(workflowStatusDTO);

    MvcResult result = mockMvc.perform(
        get("/workflowhub/workflows/workflow-1/status")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowStatusDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowStatusDTO.class);
    assertEquals(workflowStatusDTO, resultResponse);
  }


  @Test
  void whenWaitWorkflowCompletionThenOk() throws Exception {
    String workflowId = "workflow-1";

    WorkflowStatusDTO expectedResult = WorkflowStatusDTO.builder().status(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED).build();
    Mockito.when(workflowCompletionServiceMock.waitTerminationStatus(workflowId, 2, 1))
      .thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        post("/workflowhub/workflows/workflow-1/wait-completion")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE)
          .param("maxAttempts", "2")
          .param("retryDelayMs", "1"))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowStatusDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowStatusDTO.class);
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenCancelWorkflowThenOk() throws Exception {
    String workflowId = "workflow-1";

    MvcResult result = mockMvc.perform(
        delete("/workflowhub/workflows/{workflowId}", workflowId)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    Mockito.verify(serviceMock, Mockito.times(1)).cancelWorkflow(workflowId);

    assertEquals("", result.getResponse().getContentAsString());
  }
}
