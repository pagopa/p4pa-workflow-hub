package it.gov.pagopa.pu.workflow.controller.wf;

import io.micrometer.tracing.Tracer;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.ingestionflowfile.IngestionFlowFileStarterService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.opi.TreasuryOpiIngestionWFClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionFlowControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class IngestionFlowControllerTest {

  @Autowired
  private JsonMapper jsonMapper;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private IngestionFlowFileStarterService serviceMock;
  @MockitoBean
  private Tracer tracerMock;

  @MockitoBean
  private TreasuryOpiIngestionWFClient treasuryOpiIngestionWFClientMock;

  @Test
  void whenIngestThenInvokeService() throws Exception {
    long ingestionFileId = 1L;
    IngestionFlowFile.IngestionFlowFileTypeEnum flowFileType = IngestionFlowFile.IngestionFlowFileTypeEnum.PAYMENTS_REPORTING;
    String workflowId = "workflow123";
    String runId = "runId";

    WorkflowCreatedDTO workflowCreatedDTO = WorkflowCreatedDTO.builder().workflowId(workflowId).runId(runId).build();

    Mockito.when(serviceMock.ingest(ingestionFileId, flowFileType)).thenReturn(workflowCreatedDTO);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/ingestion-flow/{ingestionFileId}", ingestionFileId)
        .param("ingestionFlowFileType", flowFileType.name())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    WorkflowCreatedDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);

  }
}
