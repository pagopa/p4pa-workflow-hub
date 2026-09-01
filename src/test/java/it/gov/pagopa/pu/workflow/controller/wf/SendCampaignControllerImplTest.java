package it.gov.pagopa.pu.workflow.controller.wf;

import io.micrometer.tracing.Tracer;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.send.campaign.SendCampaignService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SendCampaignControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class SendCampaignControllerImplTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private SendCampaignService serviceMock;
  @MockitoBean
  private Tracer tracerMock;

  @Test
  void whenAlignActiveSendCampaignCountersThenOk() throws Exception {
    //GIVEN
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    when(serviceMock.alignActiveSendCampaignCounters())
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      //WHEN
      MvcResult result = mockMvc.perform(get("/workflowhub/workflow/send-campaign/active/align-counters"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();
      //THEN
      WorkflowCreatedDTO resultResponse =
        jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

  @Test
  void whenAlignUpdatedSendCampaignCountersThenOk() throws Exception {
    //GIVEN
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    when(serviceMock.alignUpdatedSendCampaignCounters())
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      //WHEN
      MvcResult result = mockMvc.perform(get("/workflowhub/workflow/send-campaign/updated/align-counters"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();
      //THEN
      WorkflowCreatedDTO resultResponse =
        jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

}
