package it.gov.pagopa.pu.workflow.controller.wf;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.config.json.JsonConfig;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.custom.fine.DebtPositionFineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionFineControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JsonConfig.class)
class DebtPositionFineControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionFineService serviceMock;

  @Test
  void whenHandleFineReductionExpirationThenOk() throws Exception {
    Long debtPositionId = 1L;
    String expectedWorkflowId = "FineReductionOptionExpirationWF-1";
    String runId = "runId";
    WorkflowCreatedDTO expected = new WorkflowCreatedDTO(expectedWorkflowId, runId);

    Mockito.when(serviceMock.expireFineReduction(debtPositionId))
      .thenReturn(expected);

    MvcResult result = mockMvc.perform(
        post("/workflowhub/workflow/debt-position/fine/1/expire-reduction")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    WorkflowCreatedDTO resultResponse =
      objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(expected, resultResponse);
  }
}
