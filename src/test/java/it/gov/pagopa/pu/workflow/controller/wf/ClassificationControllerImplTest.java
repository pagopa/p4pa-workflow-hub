package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.ClassifyAssessmentsWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.dto.ClassifyAssessmentStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClassificationControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class ClassificationControllerImplTest {
  private static final Long ORGANIZATION = 123L;
  private static final String IUV = "01011112222333345";
  private static final String IUR = "IUR";
  private static final String IUD = "IUD";
  private static final int INDEX = 1;

  @Autowired
  private JsonMapper jsonMapper;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ClassifyAssessmentsWFClient classifyAssessmentsWFClientMock;

  @MockitoBean
  private TransferClassificationWFClient transferClassificationWFClientMock;

  @MockitoBean
  private IudClassificationWFClient iudClassificationWFClientMock;

  @Test
  void givenIdWhenClassifyThenCreateTransferClassificationWFSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s-%s-%d", "TransferClassificationWF", ORGANIZATION, IUV, IUR, INDEX);
    String runId = "runId";

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId, runId);

    when(transferClassificationWFClientMock.startTransferClassification(new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX)))
      .thenReturn(workflowCreatedDTO);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/classification/transfer/{orgId}", ORGANIZATION)
      .param("iuv", IUV)
      .param("iur", IUR)
      .param("transferIndex", String.valueOf(INDEX))
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }

  @Test
  void givenIdWhenClassifyThenCreateTransferClassificationWFSkipped() throws Exception {
    when(transferClassificationWFClientMock.startTransferClassification(new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX)))
      .thenReturn(null);

    mockMvc.perform(post("/workflowhub/workflow/classification/transfer/{orgId}", ORGANIZATION)
        .param("iuv", IUV)
        .param("iur", IUR)
        .param("transferIndex", "1"))
      .andExpect(status().isNoContent());
  }

  @Test
  void givenIdWhenClassifyThenCreateIudClassificationByPaymentNotificationSignalSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s", "IudClassificationWF", ORGANIZATION, IUD);
    String runId = "runId";

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId, runId);

    when(iudClassificationWFClientMock.notifyPaymentNotification(new IudClassificationNotifyPaymentNotificationSignalDTO(IUD, ORGANIZATION)))
      .thenReturn(workflowCreatedDTO);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/classification/iud/{orgId}/notify-payment-notification", ORGANIZATION)
      .param("iud", IUD)
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }

  @Test
  void givenIdWhenClassifyThenCreateIudClassificationByPaymentNotificationSkipped() throws Exception {
    when(iudClassificationWFClientMock.notifyPaymentNotification(new IudClassificationNotifyPaymentNotificationSignalDTO(IUD, ORGANIZATION)))
      .thenReturn(null);

    mockMvc.perform(post("/workflowhub/workflow/classification/iud/{orgId}/notify-payment-notification", ORGANIZATION)
      .param("iud", IUD)
    ).andExpect(status().isNoContent());
  }

  @Test
  void givenIdWhenClassifyThenCreateIudClassificationByReceiptSignalSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s", "IudClassificationWF", ORGANIZATION, IUD);
    String runId = "runId";

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId, runId);
    IudClassificationNotifyReceiptSignalDTO signalDTO = new IudClassificationNotifyReceiptSignalDTO(ORGANIZATION, IUD, IUV, IUR, Collections.singletonList(INDEX));
    when(iudClassificationWFClientMock.notifyReceipt(signalDTO))
      .thenReturn(workflowCreatedDTO);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/classification/iud/{orgId}/notify-receipt", ORGANIZATION)
      .param("iuv", IUV)
      .param("iud", IUD)
      .param("iur", IUR)
      .param("transferIndexes", String.valueOf(INDEX))
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }

  @Test
  void givenIdWhenClassifyThenCreateIudClassificationByReceiptSkipped() throws Exception {
    IudClassificationNotifyReceiptSignalDTO signalDTO = new IudClassificationNotifyReceiptSignalDTO(ORGANIZATION, IUD, IUV, IUR, Collections.singletonList(INDEX));
    when(iudClassificationWFClientMock.notifyReceipt(signalDTO))
      .thenReturn(null);

    mockMvc.perform(post("/workflowhub/workflow/classification/iud/{orgId}/notify-receipt", ORGANIZATION)
      .param("iuv", IUV)
      .param("iud", IUD)
      .param("iur", IUR)
      .param("transferIndexes", String.valueOf(INDEX))
    ).andExpect(status().isNoContent());
  }

  @Test
  void givenIdWhenClassifyThenCreateAssessmentsClassificationWFSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s-%s-%d", "ClassifyAssessmentsWF", ORGANIZATION, IUV, IUR, INDEX);
    String runId = "runId";

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId, runId);

    when(classifyAssessmentsWFClientMock.startAssessmentsClassification(new ClassifyAssessmentStartSignalDTO(ORGANIZATION, IUV, IUD)))
      .thenReturn(workflowCreatedDTO);

    MvcResult result = mockMvc.perform(post("/workflowhub/workflow/classification/assessments/{orgId}", ORGANIZATION)
      .param("iuv", IUV)
      .param("iud", IUD)
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }

  @Test
  void givenIdWhenClassifyThenCreateAssessmentsClassificationWFSkipped() throws Exception {
    when(classifyAssessmentsWFClientMock.startAssessmentsClassification(new ClassifyAssessmentStartSignalDTO(ORGANIZATION, IUV, IUD)))
      .thenReturn(null);

    mockMvc.perform(post("/workflowhub/workflow/classification/assessments/{orgId}", ORGANIZATION)
        .param("iuv", IUV)
        .param("iud", IUD))
      .andExpect(status().isNoContent())
      .andExpect(content().string(""));
  }

}
