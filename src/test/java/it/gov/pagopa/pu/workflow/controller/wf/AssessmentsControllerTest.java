package it.gov.pagopa.pu.workflow.controller.wf;

import io.micrometer.tracing.Tracer;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
import it.gov.pagopa.pu.workflow.wf.assessments.CreateAssessmentsWFClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentsControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class AssessmentsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;
  @MockitoBean
  private Tracer tracerMock;

  @MockitoBean
  private CreateAssessmentsWFClient createAssessmentsWFClientMock;

  @Test
  void whenCreateAssessmentsProcessThenOk() throws Exception {
    String workflowId = "workflow-1";
    String runId = "runId";
    Long receiptId = 1L;
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .runId(runId)
      .build();

    Mockito.when(createAssessmentsWFClientMock.createAssessments(receiptId))
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          post("/workflowhub/workflow/assessments/receipt/{receiptId}", receiptId))
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }
}
