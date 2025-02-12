package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.config.JsonConfig;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.DebtPositionService;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JsonConfig.class)
class DebtPositionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionService service;

  @Test
  void whenSyncDebtPositionThenOk() throws Exception {
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionRequestDTO = buildDebtPositionDTO();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(service.syncDebtPosition(debtPositionRequestDTO, paymentEventType, true, accessToken))
      .thenReturn(expected);

    try(MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          post("/workflowhub/workflow/debt-position/sync")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("massive", "true")
            .param("paymentEventType", paymentEventType.name())
            .content(objectMapper.writeValueAsString(debtPositionRequestDTO)))
        .andExpect(status().isOk())
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

  @Test
  void whenCheckDpExpirationThenOk() throws Exception {
    String workflowId = "workflow-1";
    Long debtPositionId = 1L;

    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(service.checkDpExpiration(debtPositionId)).thenReturn(expected);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/debt-position/{debtPositionId}/check-expiration", debtPositionId)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    WorkflowCreatedDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(expected, resultResponse);
  }
}
