/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.facade.impl;

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.REPLACED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.SIGNED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.UNSIGNED;
import static se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO.FMB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificatesAvailableFunctions;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetCertificatesAvailableFunctionsImpl implements GetCertificatesAvailableFunctions {

    private static final String EDIT_NAME = "Ändra";
    private static final String EDIT_DESCRIPTION = "Ändrar intygsutkast";

    private static final String REMOVE_NAME = "Radera";
    private static final String REMOVE_DESCRIPTION = "Raderar intygsutkast";

    private static final String SIGN_AND_SEND_NAME = "Signera och skicka";
    private static final String SIGN_AND_SEND_DESCRIPTION = "Intyget skickas direkt till intygsmottagare";

    private static final String SIGN_NAME = "Signera intyget";
    private static final String SIGN_DESCRIPTION = "Signerar intygsutkast";

    private static final String FORWARD_NAME = "Vidarebefodra utkast";
    private static final String FORWARD_DESCRIPTION = "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.";

    private static final String FMB_NAME = "FMB";
    private static final String FMB_DESCRIPTION = "Läs FMB - ett stöd för ifyllnad och bedömning";

    private static final String REPLACE_NAME = "Ersätt";
    private static final String REPLACE_DESCRIPTION = "Skapar en kopia av detta intyg som du kan redigera.";

    private static final String RENEW_NAME = "Förnya";
    private static final String RENEW_DESCRIPTION = "Skapar en redigerbar kopia av intyget på den enhet som du är inloggad på.";
    private static final String RENEW_BODY =
        "Förnya intyg kan användas vid förlängning av en sjukskrivning. När ett intyg förnyas skapas ett nytt intygsutkast"
            + " med viss information från det ursprungliga intyget.<br><br>\n"
            + "Uppgifterna i det nya intygsutkastet går att ändra innan det signeras.<br><br>\n"
            + "De uppgifter som inte kommer med till det nya utkastet är:<br><br>\n"
            + "<ul>\n"
            + "<li>Sjukskrivningsperiod och grad.</li>\n"
            + "<li>Valet om man vill ha kontakt med Försäkringskassan.</li>\n"
            + "<li>Referenser som intyget baseras på.</li>\n"
            + "</ul>\n"
            + "<br>Eventuell kompletteringsbegäran kommer att klarmarkeras.<br><br>\n"
            + "Det nya utkastet skapas på den enhet du är inloggad på.";

    private static final String PRINT_CERTIFICATE_DESCRIPTION = "Laddar ned intyget för utskrift.";
    private static final String REVOKE_CERTIFICATE_DESCRIPTION = "Öppnar ett fönster där du kan välja att makulera intyget.";

    private static final String PRINT_NAME = "Skriv ut";
    private static final String PRINT_DRAFT_DESCRIPTION = "Laddar ned intygsutkastet för utskrift.";

    private static final String COPY_NAME = "Kopiera";
    private static final String COPY_DESCRIPTION = "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.";

    private static final String REVOKE_NAME = "Makulera";
    private static final String REVOKE_LOCKED_DRAFT_DESCRIPTION = "Öppnar ett fönster där du kan välja att makulera det låsta utkastet.";

    private static final String QUESTIONS_NAME = "Ärendekommunikation";
    private static final String QUESTIONS_DESCRIPTION = "Hantera kompletteringsbegäran, frågor och svar";

    private static final String NEW_QUESTION_NAME = "Ny fråga";
    private static final String NEW_QUESTION_DESCRIPTION = "Här kan du ställa en ny fråga till Försäkringskassan.";

    private static final String SEND_NAME = "Skicka till Försäkringskassan";
    private static final String SEND_DESCRIPTION = "Öppnar ett fönster där du kan välja att skicka intyget till Försäkringskassan";
    private static final String SEND_BODY = "<p>Om du går vidare kommer intyget skickas direkt till "
        + "Försäkringskassans system vilket ska göras i samråd med patienten.</p>"
        + "<p>Upplys patienten om att även göra en ansökan om sjukpenning hos Försäkringskassan.</p>";

    private static final String CREATE_AG7804_NAME = "Skapa Ag7804";
    private static final String CREATE_AG7804_DESCRIPTION = "Skapar ett intyg till arbetsgivaren utifrån Försäkringskassans intyg.";
    private static final String CREATE_AG7804_BODY = "<div><div class=\"ic-alert ic-alert--status ic-alert--info\">\n"
        + "<i class=\"ic-alert__icon ic-info-icon\"></i>\n"
        + "Kom ihåg att stämma av med patienten om hen vill att du skickar Läkarintyget för sjukpenning till Försäkringskassan. "
        + "Gör detta i så fall först.</div>"
        + "<p class='iu-pt-400'>Skapa ett Läkarintyg om arbetsförmåga - arbetsgivaren (AG7804)"
        + " utifrån ett Läkarintyg för sjukpenning innebär att "
        + "informationsmängder som är gemensamma för båda intygen automatiskt förifylls.\n"
        + "</p></div>";

    private static final String CREATE_FROM_CANDIDATE_NAME = "Hjälp med ifyllnad?";
    private String createFromCandidateBody = "";

    private final AuthoritiesHelper authoritiesHelper;
    private final WebCertUserService webCertUserService;
    private final CandidateDataHelper candidateDataHelper;

    @Autowired
    public GetCertificatesAvailableFunctionsImpl(AuthoritiesHelper authoritiesHelper, WebCertUserService webCertUserService,
        CandidateDataHelper candidateDataHelper) {
        this.authoritiesHelper = authoritiesHelper;
        this.webCertUserService = webCertUserService;
        this.candidateDataHelper = candidateDataHelper;
    }

    @Override
    public List<ResourceLinkDTO> get(Certificate certificate) {
        final var availableFunctions = new ArrayList<ResourceLinkDTO>();
        switch (certificate.getMetadata().getStatus()) {
            case UNSIGNED:
                availableFunctions.addAll(
                    getAvailableFunctionsForDraft(certificate)
                );
                break;
            case SIGNED:
                availableFunctions.addAll(
                    getAvailableFunctionsForCertificate(certificate)
                );
                break;
            case LOCKED:
                availableFunctions.addAll(
                    getAvailableFunctionsForLockedDraft()
                );
                break;
            default:
        }
        return availableFunctions;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForDraft(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.EDIT_CERTIFICATE,
                EDIT_NAME,
                EDIT_DESCRIPTION,
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                PRINT_NAME,
                PRINT_DRAFT_DESCRIPTION,
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REMOVE_CERTIFICATE,
                REMOVE_NAME,
                REMOVE_DESCRIPTION,
                true
            )
        );

        if (isSignedAndSendDirectly(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                    SIGN_AND_SEND_NAME,
                    SIGN_AND_SEND_DESCRIPTION,
                    true
                )
            );
        } else {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                    SIGN_NAME,
                    SIGN_DESCRIPTION,
                    true
                )
            );
        }

        if (isMessagingAvailable(certificate) && isComplementingCertificate(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.QUESTIONS,
                    QUESTIONS_NAME,
                    QUESTIONS_DESCRIPTION,
                    true
                )
            );
        }

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                FORWARD_NAME,
                FORWARD_DESCRIPTION,
                true
            )
        );

        if (isLisjp(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    FMB,
                    FMB_NAME,
                    FMB_DESCRIPTION,
                    true
                )
            );
        }

        if (isCreateCertificateFromCandidateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE,
                    CREATE_FROM_CANDIDATE_NAME,
                    "",
                    createFromCandidateBody,
                    true
                )
            );
        }

        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForCertificate(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                PRINT_NAME,
                PRINT_CERTIFICATE_DESCRIPTION,
                true
            )
        );

        if (isReplaceCertificateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
                    REPLACE_NAME,
                    REPLACE_DESCRIPTION,
                    true
                )
            );
        }

        if (isReplaceCertificateContinueAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE,
                    REPLACE_NAME,
                    REPLACE_DESCRIPTION,
                    true
                )
            );
        }

        if (isRenewCertificateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.RENEW_CERTIFICATE,
                    RENEW_NAME,
                    RENEW_DESCRIPTION,
                    RENEW_BODY,
                    true
                )
            );
        }

        if (isCreateCertificateFromTemplateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE,
                    CREATE_AG7804_NAME,
                    CREATE_AG7804_DESCRIPTION,
                    CREATE_AG7804_BODY,
                    true
                )
            );
        }

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                REVOKE_NAME,
                REVOKE_CERTIFICATE_DESCRIPTION,
                true
            )
        );

        if (isSendCertificateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    SEND_NAME,
                    SEND_DESCRIPTION,
                    SEND_BODY,
                    true
                )
            );
        }

        if (isMessagingAvailable(certificate)) {
            if (isSent(certificate)) {
                resourceLinks.add(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.QUESTIONS,
                        QUESTIONS_NAME,
                        QUESTIONS_DESCRIPTION,
                        true
                    )
                );
            } else {
                resourceLinks.add(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE,
                        QUESTIONS_NAME,
                        QUESTIONS_DESCRIPTION,
                        true
                    )
                );
            }
        }

        if (isMessagingAvailable(certificate) && isSent(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_QUESTIONS,
                    NEW_QUESTION_NAME,
                    NEW_QUESTION_DESCRIPTION,
                    true
                )
            );
        }

        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForLockedDraft() {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                PRINT_NAME,
                PRINT_DRAFT_DESCRIPTION,
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                COPY_NAME,
                COPY_DESCRIPTION,
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                REVOKE_NAME,
                REVOKE_LOCKED_DRAFT_DESCRIPTION,
                true
            )
        );

        return resourceLinks;
    }

    private boolean isSent(Certificate certificate) {
        return certificate.getMetadata().isSent();
    }

    private boolean isSignedAndSendDirectly(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, certificate.getMetadata().getType())
            || isComplementingCertificate(certificate);
    }

    private boolean isComplementingCertificate(Certificate certificate) {
        return certificate.getMetadata().getRelations() != null && certificate.getMetadata().getRelations().getParent() != null
            && certificate.getMetadata().getRelations().getParent().getType() == COMPLEMENTED;
    }

    private boolean isRevoked(Certificate certificate) {
        return certificate.getMetadata().getStatus() == CertificateStatus.REVOKED;

    }

    private boolean isSendCertificateAvailable(Certificate certificate) {
        if (isSent(certificate)) {
            return false;
        }

        if (isReplacementSigned(certificate)) {
            return false;
        }

        return isLisjp(certificate);
    }

    private boolean isCreateCertificateFromTemplateAvailable(Certificate certificate) {
        if (isReplacementSigned(certificate) || isDjupintegration() || isComplementingCertificate(certificate) || isRevoked(certificate)) {
            return false;
        }

        return isLisjp(certificate);
    }

    private boolean isCreateCertificateFromCandidateAvailable(Certificate certificate) {
        if (certificate.getMetadata().getVersion() == 0 && isRelationsEmpty(certificate) && isAg7804(certificate)) {
            final var metadata = candidateDataHelper
                .getCandidateMetadata(certificate.getMetadata().getType(), certificate.getMetadata().getTypeVersion(),
                    Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).get());
            if (metadata.isPresent()) {
                setCreateFromCandidateBody(metadata.get().getIntygCreated().toString());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isRelationsEmpty(Certificate certificate) {
        return certificate.getMetadata().getRelations() == null || (certificate.getMetadata().getRelations().getParent() == null
            && certificate.getMetadata().getRelations().getChildren().length == 0);
    }

    private void setCreateFromCandidateBody(String candidateDate) {
        createFromCandidateBody = "<p>Det finns ett Läkarintyg för sjukpenning för denna patient som är utfärdat "
            + "<span class='iu-fw-bold'>"
            + candidateDate.split("T")[0]
            + "</span> på en enhet som du har åtkomst till. Vill du kopiera de svar som givits i det intyget till detta intygsutkast?</p>";
    }

    private boolean isLisjp(Certificate certificate) {
        return certificate.getMetadata().getType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID);
    }

    private boolean isAg7804(Certificate certificate) {
        return certificate.getMetadata().getType().equalsIgnoreCase(Ag7804EntryPoint.MODULE_ID);
    }

    private boolean isDjupintegration() {
        final var user = webCertUserService.getUser();
        return user != null && user.getOrigin().contains("DJUPINTEGRATION");
    }

    private boolean isReplaceCertificateAvailable(Certificate certificate) {
        return !(isReplacementUnsigned(certificate) || isReplacementSigned(certificate));
    }

    private boolean isReplaceCertificateContinueAvailable(Certificate certificate) {
        return isReplacementUnsigned(certificate);
    }

    private boolean isRenewCertificateAvailable(Certificate certificate) {
        if (isReplacementSigned(certificate)) {
            return false;
        }

        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, certificate.getMetadata().getType());
    }

    private boolean isReplacementUnsigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, UNSIGNED);
    }

    private boolean isReplacementSigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, SIGNED);
    }

    private boolean includesChildRelation(CertificateRelations relations, CertificateRelationType type, CertificateStatus status) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(type) && relation.getStatus().equals(status)
        );
    }

    private boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }

    private boolean isMessagingAvailable(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, certificate.getMetadata().getType());
    }
}
