package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowErrorDTO;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(WorkflowHubApiImpl.class)
public class WorkflowHubApiImplTest {


  @Value("${openapi.p4paWorkflowHub.base-path:/workflowhub}")
  private String basePath;

  @Autowired
  private ObjectMapper objectMapper;

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

    WorkflowCreatedDTO workflowCreatedDTO = WorkflowCreatedDTO.builder().workflowId(workflowId).build();

    Mockito.when(paymentsReportingIngestionWFClient.ingest(ingestionFileId)).thenReturn(workflowId);

    MvcResult result = mockMvc.perform(post(basePath + "/PaymentIngestionWF/{ingestionFileId}", ingestionFileId)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    WorkflowCreatedDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);

  }

  @Test
  public void testCreatePaymentIngestionWF_Exception() throws Exception {
    Long ingestionFileId = 1L;
    String errorDescription = "Error";
    String errorCode = WorkflowErrorDTO.CodeEnum.GENERIC_ERROR.getValue();

    Mockito.when(paymentsReportingIngestionWFClient.ingest(ingestionFileId)).thenThrow(new RuntimeException(errorDescription));

    mockMvc.perform(post(basePath + "/PaymentIngestionWF/{ingestionFileId}", ingestionFileId)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isInternalServerError())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(errorCode))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(errorDescription));
  }
}
