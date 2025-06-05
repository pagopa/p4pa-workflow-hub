package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowCompletionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    Mockito.when(workflowCompletionServiceMock.waitTerminationStatus(workflowId, 2, 1))
      .thenReturn(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED);

    MvcResult result = mockMvc.perform(
        post("/workflowhub/workflows/workflow-1/wait-completion")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE)
          .param("maxAttempts", "2")
          .param("retryDelayMs", "1"))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowExecutionStatus resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowExecutionStatus.class);
    assertEquals(WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED, resultResponse);
  }
}
