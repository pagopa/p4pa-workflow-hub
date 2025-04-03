package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.exportfile.ExportFileService;
import it.gov.pagopa.pu.workflow.utilities.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportFileControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class ExportFileControllerImplTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private ExportFileService service;

  @Test
  void givenExportFileIdWhenExportFileExpirationHandlerThenOk() throws Exception {
    Long exportFileId = 1L;
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(service.exportFileExpirationHandler(exportFileId))
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          get("/workflowhub/workflow/export-file/{exportFileId}", exportFileId))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }
}
