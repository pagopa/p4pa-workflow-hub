package it.gov.pagopa.pu.workflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.schedules.ScheduleClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.PaymentsReportingPagoPaBrokersFetchScheduler;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.SynchronizeTaxonomyPagoPaFetchScheduler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonAssert;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE, addFilters = false)
@TestPropertySource(properties = {
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
  "spring.datasource.username=sa",
  "spring.datasource.password=sa",

  "springwolf.enabled=true",
  "springwolf.use-fqn=false",
  "spring.cloud.function.definition=",
  "spring.temporal.enabled=false",
  "spring.temporal.connection.target="
})
@Slf4j
class AsyncApiGeneratorTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorkflowClient workflowClientMock;
  @MockitoBean
  private ScheduleClient scheduleClientMock;
  @MockitoBean
  private PaymentsReportingPagoPaBrokersFetchScheduler paymentsReportingPagoPaBrokersFetchSchedulerMock;
  @MockitoBean
  private SynchronizeTaxonomyPagoPaFetchScheduler synchronizeTaxonomyPagoPaFetchSchedulerMock;

  @Test
  void generateAndVerifyCommit() throws Exception {
    MvcResult result = mockMvc.perform(
      get("/springwolf/docs")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk())
      .andReturn();

    String asyncApiResult = result.getResponse().getContentAsString()
      .replace("\r", "");

    Assertions.assertTrue(asyncApiResult.startsWith("{\n  \"asyncapi\": \"3"));

    Path asyncApiGeneratedPath = Path.of("asyncapi/generated.asyncapi.json");
    boolean toStore=true;
    if(Files.exists(asyncApiGeneratedPath)){
      String storedOpenApi = Files.readString(asyncApiGeneratedPath);
      try {
        JsonAssert.comparator(JsonCompareMode.STRICT).assertIsMatch(storedOpenApi, asyncApiResult);
        toStore=false;
      } catch (Throwable e){
        log.info("Observed the following changes: {}", e.getMessage());
      }
    }
    if(toStore){
      Files.writeString(asyncApiGeneratedPath, asyncApiResult, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    String gitStatus = execCmd("git", "status");
    Assertions.assertFalse(gitStatus.contains("asyncapi/generated.asyncapi.json"), "Generated AsyncApi not committed");
  }

  public static String execCmd(String... cmd) throws java.io.IOException {
    java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
