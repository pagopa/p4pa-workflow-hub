package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper;

import it.gov.pagopa.payhub.activities.util.IoNotificationPlaceholderUtils;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionTypeEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static it.gov.pagopa.payhub.activities.util.IoNotificationPlaceholderUtils.ITALIAN_DATE_FORMAT;
import static it.gov.pagopa.payhub.activities.util.Utilities.centsAmountToEuroString;

public class IoNotificationFinePlaceholderResolver {

  private IoNotificationFinePlaceholderResolver(){}

  public static String applyFinePlaceholder(String markdown, DebtPositionDTO debtPositionDTO) {
        Map<String, String> placeholders = new HashMap<>();

        InstallmentDTO reducedInstallment = debtPositionDTO.getPaymentOptions().stream()
                .filter(po -> PaymentOptionTypeEnum.REDUCED_SINGLE_INSTALLMENT.equals(po.getPaymentOptionType()))
                .map(po -> po.getInstallments().getFirst())
                .findFirst()
                .orElse(null);

        // data from Reduced Installment
        if(reducedInstallment != null) {
            placeholders.put("%dataNotifica%", reducedInstallment.getNotificationDate() != null ? reducedInstallment.getNotificationDate().format(ITALIAN_DATE_FORMAT) : "");
            placeholders.put("%fineRiduzione%", reducedInstallment.getDueDate() != null ? reducedInstallment.getDueDate().format(ITALIAN_DATE_FORMAT) : "");
            placeholders.put("%avvisoRidotto_IUV%", Objects.toString(reducedInstallment.getIuv(), ""));
            placeholders.put("%avvisoRidotto_NAV%", Objects.toString(reducedInstallment.getNav(), ""));
            placeholders.put("%avvisoRidotto_importo%", Objects.toString(centsAmountToEuroString(reducedInstallment.getAmountCents()), ""));
        }

        InstallmentDTO singleInstallment = debtPositionDTO.getPaymentOptions().stream()
                .filter(po -> PaymentOptionTypeEnum.SINGLE_INSTALLMENT.equals(po.getPaymentOptionType()))
                .map(po -> po.getInstallments().getFirst())
                .findFirst()
                .orElse(null);

        // data from Single Installment
        if(singleInstallment != null) {
            placeholders.put("%dataNotifica%", singleInstallment.getNotificationDate() != null ? singleInstallment.getNotificationDate().format(ITALIAN_DATE_FORMAT) : "");
            placeholders.put("%posizioneDebitoria_scadenza%", singleInstallment.getDueDate() != null ? singleInstallment.getDueDate().format(ITALIAN_DATE_FORMAT) : "");
            placeholders.put("%avvisoIntero_IUV%", Objects.toString(singleInstallment.getIuv(), ""));
            placeholders.put("%avvisoIntero_NAV%", Objects.toString(singleInstallment.getNav(), ""));
            placeholders.put("%avvisoIntero_importo%", Objects.toString(centsAmountToEuroString(singleInstallment.getAmountCents()), ""));
        }

        return IoNotificationPlaceholderUtils.applyPlaceholder(markdown, placeholders);
    }
}
