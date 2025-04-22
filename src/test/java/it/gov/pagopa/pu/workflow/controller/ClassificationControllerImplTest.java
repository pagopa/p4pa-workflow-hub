package it.gov.pagopa.pu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.IudClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TransferClassificationWFClient transferClassificationWFClientMock;

  @MockitoBean
  private IudClassificationWFClient iudClassificationWFClientMock;

  @Test
  void givenIdWhenClassifyThenCreateTransferClassificationWFSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s-%s-%d", "TransferClassificationWF", ORGANIZATION, IUV, IUR, INDEX);

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId);

    when(transferClassificationWFClientMock.startTransferClassification(new TransferClassificationStartSignalDTO(ORGANIZATION, IUV, IUR, INDEX)))
      .thenReturn(expectedWorkflowId);

    MvcResult result = mockMvc.perform(post("/workflowhub/classification/transfer/{orgId}", ORGANIZATION)
      .param("iuv", IUV)
      .param("iur", IUR)
      .param("transferIndex", String.valueOf(INDEX))
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }

  @Test
  void givenIdWhenClassifyThenCreateIudClassificationByPaymentNotificationSignalSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s", "IudClassificationWF", ORGANIZATION, IUD);

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId);

    when(iudClassificationWFClientMock.notifyPaymentNotification(new IudClassificationNotifyPaymentNotificationSignalDTO(IUD, ORGANIZATION)))
      .thenReturn(expectedWorkflowId);

    MvcResult result = mockMvc.perform(post("/workflowhub/classification/iud/{orgId}/notify-payment-notification", ORGANIZATION)
      .param("iud", IUD)
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }

  @Test
  void givenIdWhenClassifyThenCreateIudClassificationByReceiptSignalSuccessfully() throws Exception {
    String expectedWorkflowId = String.format("%s-%d-%s", "IudClassificationWF", ORGANIZATION, IUD);

    WorkflowCreatedDTO workflowCreatedDTO = new WorkflowCreatedDTO(expectedWorkflowId);
    IudClassificationNotifyReceiptSignalDTO signalDTO = new IudClassificationNotifyReceiptSignalDTO(ORGANIZATION, IUD, IUV, IUR, Collections.singletonList(INDEX));
    when(iudClassificationWFClientMock.notifyReceipt(signalDTO))
      .thenReturn(expectedWorkflowId);

    MvcResult result = mockMvc.perform(post("/workflowhub/classification/iud/{orgId}/notify-receipt", ORGANIZATION)
      .param("iuv", IUV)
      .param("iud", IUD)
      .param("iur", IUR)
      .param("transferIndexes", String.valueOf(INDEX))
    ).andExpect(status().isCreated()).andReturn();

    WorkflowCreatedDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), WorkflowCreatedDTO.class);
    assertEquals(workflowCreatedDTO, resultResponse);
  }
}
