package it.gov.pagopa.pu.workflow;

import it.gov.pagopa.payhub.activities.aspect.NotRetryableActivityExceptionHandlerAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(NotRetryableActivityExceptionHandlerAspect.class)
public class WorkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowApplication.class, args);
	}

}
