package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowStatusDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowControllerImpl.class)
class WorkflowControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WorkflowService service;

  @Test
  void whenGetWorkflowStatusThenOk() throws Exception {
    String workflowId = "workflow-1";
    WorkflowStatusDTO workflowStatusDTO = WorkflowStatusDTO.builder()
      .workflowId(workflowId)
      .status("ok")
      .build();

    Mockito.when(service.getWorkflowStatus(workflowId))
      .thenReturn(workflowStatusDTO);

    MvcResult result = mockMvc.perform(
        get("/workflowhub/workflow-1/status")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andReturn();

    WorkflowStatusDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowStatusDTO.class);
    assertEquals(workflowStatusDTO, resultResponse);
  }
}
