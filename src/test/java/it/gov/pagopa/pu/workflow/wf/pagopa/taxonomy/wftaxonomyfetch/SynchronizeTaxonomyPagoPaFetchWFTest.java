package it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.wftaxonomyfetch;

import it.gov.pagopa.payhub.activities.activity.taxonomy.SynchronizeTaxonomyActivity;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.config.SynchronizeTaxonomyPagoPaFetchWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SynchronizeTaxonomyPagoPaFetchWFTest {

  @Mock
  private SynchronizeTaxonomyActivity synchronizeTaxonomyActivityMock;

  private SynchronizeTaxonomyPagoPaFetchWFImpl wf;

  @BeforeEach
  void setUp() {
    SynchronizeTaxonomyPagoPaFetchWfConfig synchronizeTaxonomyPagoPaFetchWfConfigMock = mock(SynchronizeTaxonomyPagoPaFetchWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(synchronizeTaxonomyPagoPaFetchWfConfigMock.buildSynchronizeTaxonomyActivityStub()).thenReturn(synchronizeTaxonomyActivityMock);

    when(applicationContextMock.getBean(SynchronizeTaxonomyPagoPaFetchWfConfig.class)).thenReturn(synchronizeTaxonomyPagoPaFetchWfConfigMock);

    wf = new SynchronizeTaxonomyPagoPaFetchWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronizeTaxonomyActivityMock);
  }
  @Test
  void givenSuccessfulSyncWhenSynchronizeThenLogSynchronizedTaxonomies() {
    // Given
    Integer synchronizedTaxonomies = 5;
    when(synchronizeTaxonomyActivityMock.syncTaxonomy()).thenReturn(synchronizedTaxonomies);

    // When
    wf.synchronize();

    // Then
    verify(synchronizeTaxonomyActivityMock).syncTaxonomy();
  }
}
