package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.PaymentsReportingIngestionWFClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowHubApiImpl.class)
public class WorkflowHubApiImplTest {


  @Value("${openapi.p4paWorkflowHub.base-path:/workflowhub}")
  private String basePath;


  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PaymentsReportingIngestionWFClient paymentsReportingIngestionWFClient;

  @BeforeEach
  public void setUp() {
    Mockito.reset(paymentsReportingIngestionWFClient);
  }

  @Test
  public void testCreatePaymentIngestionWF_Success() throws Exception {
    Long ingestionFileId = 1L;
    String workflowId = "workflow123";

    Mockito.when(paymentsReportingIngestionWFClient.ingest(ingestionFileId)).thenReturn(workflowId);

    String jsonContent = """
      {
        "workflowId": "%s"
      }
      """.formatted(workflowId);

    mockMvc.perform(post(basePath + "/PaymentIngestionWF/{ingestionFileId}", ingestionFileId)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(jsonContent));
  }

  @Test
  public void testCreatePaymentIngestionWF_Exception() throws Exception {
    Long ingestionFileId = 1L;
    String errorMessage = "Internal Server Error";

    Mockito.when(paymentsReportingIngestionWFClient.ingest(ingestionFileId)).thenThrow(new RuntimeException(errorMessage));

    String jsonContent = """
      {
        "error": "Internal Server Error",
        "errorDescription": "%s"
      }
      """.formatted(errorMessage);

    mockMvc.perform(post(basePath + "/PaymentIngestionWF/{ingestionFileId}", ingestionFileId)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isInternalServerError())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(jsonContent));
  }
}
