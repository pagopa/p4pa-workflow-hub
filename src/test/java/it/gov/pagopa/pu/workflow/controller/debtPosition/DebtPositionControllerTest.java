package it.gov.pagopa.pu.workflow.controller.debtPosition;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.controller.debtposition.DebtPositionControllerImpl;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.DebtPositionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.function.Function;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionService service;

  @Test
  void whenCreateDpSyncThenOk() throws Exception {
    testWorkflowCreationDP("/workflowhub/workflow/debt-position",
      debtPositionRequestDTO -> service.createDPSync(debtPositionRequestDTO));
  }

  @Test
  void whenCreateDpSyncAcaThenOk() throws Exception {
    testWorkflowCreationDP("/workflowhub/workflow/debt-position/aca",
      debtPositionRequestDTO -> service.alignDpSyncAca(debtPositionRequestDTO));
  }

  private void testWorkflowCreationDP(String endpoint, Function<DebtPositionRequestDTO, WorkflowCreatedDTO> service) throws Exception {
    String workflowId = "workflow-1";
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(service.apply(Mockito.any(DebtPositionRequestDTO.class))).thenReturn(expected);

    MvcResult result = mockMvc.perform(
        post(endpoint)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(debtPositionRequestDTO)))
      .andExpect(status().isOk())
      .andReturn();

    WorkflowCreatedDTO resultResponse =
      objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(expected, resultResponse);
  }
}
