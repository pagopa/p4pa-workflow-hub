package it.gov.pagopa.pu.workflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
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

@WebMvcTest(ExportFileControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class ExportFileControllerImplTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private ExportFileService serviceMock;

  @Test
  void givenExportFileIdWhenExpireExportFileThenOk() throws Exception {
    Long exportFileId = 1L;
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(serviceMock.expireExportFile(exportFileId))
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          post("/workflowhub/export-file/{exportFileId}/expire", exportFileId))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }

  @Test
  void whenCreateExportFileThenOk() throws Exception {
    Long exportFileId = 1L;
    ExportFile.ExportFileTypeEnum exportFileType = ExportFileTypeEnum.CLASSIFICATIONS;
    String workflowId = "workflow-1";
    String accessToken = "ACCESSTOKEN";
    WorkflowCreatedDTO expected = WorkflowCreatedDTO.builder()
      .workflowId(workflowId)
      .build();

    Mockito.when(serviceMock.exportFile(exportFileId, exportFileType))
      .thenReturn(expected);

    try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtilsMockedStatic.when(SecurityUtils::getAccessToken)
        .thenReturn(accessToken);

      MvcResult result = mockMvc.perform(
          post("/workflowhub/export-file/{exportFileId}", exportFileId)
            .queryParam("exportFileType", ExportFileTypeEnum.CLASSIFICATIONS.toString()))
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

      WorkflowCreatedDTO resultResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
      assertEquals(expected, resultResponse);
    }
  }
}
