package it.gov.pagopa.pu.workflow.wf.exportfile.export.config;

import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.activity.ScheduleExportFileExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.activity.ScheduleExportFileExpirationActivityImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class ExportFileWFConfigTest {

  private final ExportFileWFConfig config = new ExportFileWFConfig();

  private final Map<Class<?>, Class<?>> localActivityInterface2Impl = Map.of(
    ScheduleExportFileExpirationActivity.class, ScheduleExportFileExpirationActivityImpl.class
  );

  @Test
  void testTaskQueueAlignment() throws InvocationTargetException, IllegalAccessException {
    TemporalTestUtils.verifyActivityStubConfiguration(config, localActivityInterface2Impl);
  }
}
