package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.send.SendNotificationService;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SendNotificationControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class SendNotificationControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private SendNotificationService serviceMock;

  @Test
  void givenSendNotificationIdWhenSendNotificationProcessThenOk() throws Exception {
    String workflowId = "workflow-1";
    String runId = "runId";
    String sendNotificationId = "sendNotificationId";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .runId(runId)
      .build();

    Mockito.when(serviceMock.sendNotificationProcess(sendNotificationId))
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          get("/workflowhub/workflow/send-notification/{sendNotificationId}/start", sendNotificationId))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

  @Test
  void givenSendNotificationIdWhenRetrieveNotificationDateThenOk() throws Exception {
    String workflowId = "workflow-1";
    String sendNotificationId = "sendNotificationId";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(serviceMock.sendNotificationDateRetrieve(sendNotificationId))
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          get("/workflowhub/workflow/send-notification/{sendNotificationId}/retrieve-notification-date", sendNotificationId))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

}
