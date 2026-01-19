package it.gov.pagopa.pu.workflow.controller.wf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.config.json.JsonConfig;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.SyncDebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.DebtPositionService;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
  private DebtPositionService serviceMock;
  @MockitoBean
  private Tracer tracerMock;

  @Test
  void whenSyncDebtPositionThenOk() throws Exception {
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionRequestDTO = buildDebtPositionDTO();
    GenericWfExecutionConfig executionConfig = GenericWfExecutionConfig.builder()
      .ioMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(new IONotificationMessage("CREATED_SUBJECT", "CREATED_MESSAGE"))
        .updated(new IONotificationMessage("UPDATED_SUBJECT", "UPDATED_MESSAGE"))
        .build())
      .build();
    SyncDebtPositionRequestDTO request = new SyncDebtPositionRequestDTO(debtPositionRequestDTO, executionConfig);
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(serviceMock.syncDebtPosition(debtPositionRequestDTO, paymentEventRequest, new WfExecutionParameters(true, false, executionConfig), accessToken))
      .thenReturn(expected);

    try(MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          post("/workflowhub/workflow/debt-position/sync")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("massive", "true")
            .param("partialChange", "false")
            .param("paymentEventType", paymentEventRequest.getPaymentEventType().getValue())
            .param("paymentEventDescription", paymentEventRequest.getEventDescription())
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

  @Test
  void givenNoOptionalParametersWhenSyncDebtPositionThenOk() throws Exception {
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionRequestDTO = buildDebtPositionDTO();
    SyncDebtPositionRequestDTO request = new SyncDebtPositionRequestDTO(debtPositionRequestDTO, null);
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(serviceMock.syncDebtPosition(debtPositionRequestDTO, null, new WfExecutionParameters(false, false, null), accessToken))
      .thenReturn(expected);

    try(MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          post("/workflowhub/workflow/debt-position/sync")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(request)))
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

    Mockito.when(serviceMock.checkDpExpiration(debtPositionId)).thenReturn(expected);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/debt-position/{debtPositionId}/check-expiration", debtPositionId)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    WorkflowCreatedDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(expected, resultResponse);
  }
}
