package it.gov.pagopa.pu.workflow;

import it.gov.pagopa.payhub.activities.aspect.NotRetryableActivityExceptionHandlerAspect;
import it.gov.pagopa.payhub.activities.performancelogger.ApiRequestPerformanceLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(
  exclude = {ErrorMvcAutoConfiguration.class},
  scanBasePackages = {
    "it.gov.pagopa.pu.workflow",
    "it.gov.pagopa.payhub.activities.connector.auth"
  })
@Import({
  NotRetryableActivityExceptionHandlerAspect.class,
  ApiRequestPerformanceLogger.class
})
public class WorkflowApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorkflowApplication.class, args);
  }

}
