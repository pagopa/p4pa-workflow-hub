package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.DeleteMassiveNoticesFileWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.MassiveNoticesGenerationWFClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class NoticeControllerImplTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MassiveNoticesGenerationWFClient massiveNoticesGenerationWFClientMock;
  @MockitoBean
  private DeleteMassiveNoticesFileWFClient deleteMassiveNoticesFileWFClientMock;

  @Test
  void whenGenerateMassiveThenOk() throws Exception {
    String workflowId = "workflow-1";
    String runId = "runId";
    Long ingestionFlowFileId = 1L;

    Mockito.when(massiveNoticesGenerationWFClientMock.generate(ingestionFlowFileId))
      .thenReturn(new WorkflowCreatedDTO(workflowId, runId));

    mockMvc.perform(
        post("/workflowhub/workflow/notice/massive/generate/{ingestionFlowFileId}", ingestionFlowFileId)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().is2xxSuccessful())
      .andExpect(content().json("{\"workflowId\":\"workflow-1\",\"runId\":\"runId\"}"));
  }

  @Test
  void whenDeleteMassiveThenOk() throws Exception {
    Long ingestionFlowFileId = 1L;

    MvcResult result = mockMvc.perform(
        delete("/workflowhub/workflow/notice/massive/delete/{ingestionFlowFileId}", ingestionFlowFileId)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    Mockito.verify(deleteMassiveNoticesFileWFClientMock, Mockito.times(1)).delete(ingestionFlowFileId);

    assertEquals("", result.getResponse().getContentAsString());
  }
}
