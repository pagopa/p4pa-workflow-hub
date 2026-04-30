package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper;

import it.gov.pagopa.payhub.activities.util.IoNotificationPlaceholderUtils;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO2;
import static it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper.IoNotificationFinePlaceholderResolver.applyFinePlaceholder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class IoNotificationFinePlaceholderResolverTest {

  @Test
  void whenApplyFinePlaceholderThenOk() {
    // Given
    LocalDate notificationDateRidotto = LocalDate.of(2024, 4, 1);
    LocalDate dueDateRidotto = notificationDateRidotto.plusDays(5);
    LocalDate dueDateIntero = notificationDateRidotto.plusDays(60);

    OffsetDateTime notificationOffset = notificationDateRidotto.atStartOfDay().atOffset(ZoneOffset.UTC);

    PaymentOptionDTO paymentOptionDTO1 = new PaymentOptionDTO();
    paymentOptionDTO1.setPaymentOptionType(PaymentOptionType.REDUCED_SINGLE_INSTALLMENT);
    InstallmentDTO installmentDTO1 = buildInstallmentDTO();
    installmentDTO1.setIuv("iuvRidotto");
    installmentDTO1.setNav("navRidotto");
    installmentDTO1.setAmountCents(100L);
    installmentDTO1.setNotificationDate(notificationOffset);
    installmentDTO1.setDueDate(dueDateRidotto);
    paymentOptionDTO1.setInstallments(List.of(installmentDTO1));

    PaymentOptionDTO paymentOptionDTO2 = new PaymentOptionDTO();
    paymentOptionDTO2.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    InstallmentDTO installmentDTO2 = buildInstallmentDTO2();
    installmentDTO2.setIuv("iuvIntero");
    installmentDTO2.setNav("navIntero");
    installmentDTO2.setAmountCents(200L);
    installmentDTO2.setNotificationDate(notificationOffset);
    installmentDTO2.setDueDate(dueDateIntero);
    paymentOptionDTO2.setInstallments(List.of(installmentDTO2));

    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setPaymentOptions(List.of(paymentOptionDTO1, paymentOptionDTO2));

    String markdown = " Avviso ridotto: IUV=%avvisoRidotto_IUV%, NAV=%avvisoRidotto_NAV%, importo=%avvisoRidotto_importo%, scadenza=%fineRiduzione%, notifica=%dataNotifica%. "+
      "Avviso intero: IUV=%avvisoIntero_IUV%, NAV=%avvisoIntero_NAV%, importo=%avvisoIntero_importo%, scadenza=%posizioneDebitoria_scadenza%.";

    String expectedMessage = "Avviso ridotto: IUV=iuvRidotto, NAV=navRidotto, importo=1,00, scadenza=06/04/2024, notifica=01/04/2024. " +
      "Avviso intero: IUV=iuvIntero, NAV=navIntero, importo=2,00, scadenza=31/05/2024.";

    try (MockedStatic<IoNotificationPlaceholderUtils> mocked = mockStatic(IoNotificationPlaceholderUtils.class)) {
      mocked.when(() -> IoNotificationPlaceholderUtils.applyPlaceholder(anyString(), anyMap()))
        .thenReturn(expectedMessage);

      // When
      String message = applyFinePlaceholder(markdown, debtPositionDTO);

      // Then
      assertEquals(expectedMessage, message);
    }
  }

  @Test
  void givenReducedInstallmentNullWhenThenOnlySingleDataIsFilled() {
    // Given
    InstallmentDTO single = buildInstallmentDTO();
    single.setIuv("iuvIntero");
    single.setNav("navIntero");
    single.setAmountCents(200L);
    single.setNotificationDate(OffsetDateTime.parse("2024-04-01T10:00:00Z"));
    single.setDueDate(LocalDate.of(2024, 5, 31));

    PaymentOptionDTO poSingle = new PaymentOptionDTO();
    poSingle.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    poSingle.setInstallments(List.of(single));

    DebtPositionDTO dto = new DebtPositionDTO();
    dto.setPaymentOptions(List.of(poSingle));

    String markdown = "Ridotto: %avvisoRidotto_IUV%, %avvisoRidotto_importo%, %fineRiduzione%, %dataNotifica%. " +
      "Intero: %avvisoIntero_IUV%, %avvisoIntero_importo%, %posizioneDebitoria_scadenza%.";

    String expected = "Ridotto: , , , . " +
      "Intero: iuvIntero, 2,00, 31/05/2024.";

    try (MockedStatic<IoNotificationPlaceholderUtils> mocked = mockStatic(IoNotificationPlaceholderUtils.class)) {
      mocked.when(() -> IoNotificationPlaceholderUtils.applyPlaceholder(anyString(), anyMap()))
        .thenReturn(expected);

      // When
      String result = applyFinePlaceholder(markdown, dto);

      // Then
      assertEquals(expected, result);
    }
  }

  @Test
  void givenSingleInstallmentNullWhenThenOnlyReducedDataIsFilled() {
    // Given
    InstallmentDTO reduced = buildInstallmentDTO();
    reduced.setIuv("iuvRidotto");
    reduced.setNav("navRidotto");
    reduced.setAmountCents(100L);
    reduced.setNotificationDate(OffsetDateTime.parse("2024-04-01T10:00:00Z"));
    reduced.setDueDate(LocalDate.of(2024, 4, 6));

    PaymentOptionDTO poReduced = new PaymentOptionDTO();
    poReduced.setPaymentOptionType(PaymentOptionType.REDUCED_SINGLE_INSTALLMENT);
    poReduced.setInstallments(List.of(reduced));

    DebtPositionDTO dto = new DebtPositionDTO();
    dto.setPaymentOptions(List.of(poReduced));

    String markdown = "Ridotto: %avvisoRidotto_IUV%, %avvisoRidotto_importo%, %fineRiduzione%, %dataNotifica%. " +
      "Intero: %avvisoIntero_IUV%, %avvisoIntero_importo%, %posizioneDebitoria_scadenza%.";

    String expected = "Ridotto: iuvRidotto, 1,00, 06/04/2024, 01/04/2024. " +
      "Intero: , , .";

    try (MockedStatic<IoNotificationPlaceholderUtils> mocked = mockStatic(IoNotificationPlaceholderUtils.class)) {
      mocked.when(() -> IoNotificationPlaceholderUtils.applyPlaceholder(anyString(), anyMap()))
        .thenReturn(expected);

      // When
      String result = applyFinePlaceholder(markdown, dto);

      // Then
      assertEquals(expected, result);
    }
  }
}
