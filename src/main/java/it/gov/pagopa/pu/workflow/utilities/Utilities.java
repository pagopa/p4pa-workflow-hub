package it.gov.pagopa.pu.workflow.utilities;

import com.google.protobuf.Timestamp;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import org.mapstruct.Named;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utilities {

  private Utilities() {
  }

  private static final Pattern IUD_MATCH_PATTERN = Pattern.compile("IUD:\\s*([^;]*)\\s*(?:;|$)");

  public static String generateWorkflowId(Long id, Class<?> workflowInterface) {
    return generateWorkflowId(id != null ? id.toString() : null, workflowInterface);
  }

  public static String generateWorkflowId(String id, Class<?> workflowInterface) {
    if (id == null || workflowInterface == null) {
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_INVALID_WORKFLOW_ID, "The ID or the workflow must not be null");
    }
    return String.format("%s-%s", workflowInterface.getSimpleName(), id);
  }

  public static String getWorkflowExceptionMessage(Throwable e) {
    if (e instanceof ActivityFailure activityFailure) {
      if (activityFailure.getCause() instanceof ApplicationFailure applicationFailure) {
        return applicationFailure.getOriginalMessage();
      }
      return activityFailure.getMessage();
    }
    return e.getMessage();
  }

  @Named("offsetDateTimeToInstant")
  public static Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toInstant() : null;
  }

  @Named("instantToOffsetDateTime")
  public static OffsetDateTime instantToOffsetDateTime(Instant instant) {
    return instant != null ? instant.atZone(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID).toOffsetDateTime() : null;
  }

  @Named("offsetDateTimeToLocalDateTime")
  public static LocalDateTime offsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
  }

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static OffsetDateTime protobufTimestamp2OffsetDateTime(Timestamp ts) {
    if (ts.getSeconds() > 0) {
      return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).atZone(it.gov.pagopa.payhub.activities.util.Utilities.ZONEID).toOffsetDateTime();
    } else {
      return null;
    }
  }

  public static Duration protobufDuration2Duration(com.google.protobuf.Duration d) {
    return Duration.ofSeconds(d.getSeconds(), d.getNanos());
  }

  public static Set<String> extractIudsFromDescription(String description) {
    if(!StringUtils.hasText(description)){
      return Set.of();
    }

    Set<String> iuds = new HashSet<>();
    Matcher matcher = IUD_MATCH_PATTERN.matcher(description);

    if (matcher.find()) {
      String ids = matcher.group(1);
      String[] splitIds = ids.split(",");
      for (String id : splitIds) {
        String trimmedId = id.trim();
        if (!trimmedId.isEmpty()) {
          iuds.add(trimmedId);
        }
      }
    }

    return iuds;
  }
}
