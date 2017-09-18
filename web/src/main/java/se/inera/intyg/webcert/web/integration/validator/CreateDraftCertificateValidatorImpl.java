/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;

@Component
public class CreateDraftCertificateValidatorImpl implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private WebcertUserDetailsService webcertUserDetailsService;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.integration.validator.CreateDraftCertificateValidator#validate(se.inera.certificate.
     * clinicalprocess
     * .healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType)
     */
    @Override
    public ResultValidator validate(Utlatande utlatande) {
        ResultValidator errors = ResultValidator.newInstance();

        validateTypAvUtlatande(utlatande.getTypAvUtlatande(), errors);
        validatePatient(utlatande.getPatient(), errors);
        validateSkapadAv(utlatande.getSkapadAv(), errors);

        return errors;
    }

    @Override
    public ResultValidator validateApplicationErrors(Utlatande utlatande) {
        ResultValidator errors = ResultValidator.newInstance();
        validateSekretessmarkeringOchIntygsTyp(utlatande.getSkapadAv(), utlatande.getTypAvUtlatande(), utlatande.getPatient().getPersonId(),
                errors);
        return errors;
    }

    private void validateSekretessmarkeringOchIntygsTyp(HosPersonal skapadAv, TypAvUtlatande typAvUtlatande, PersonId personId,
            ResultValidator errors) {

        // If intygstyp is NOT allowed to issue for sekretessmarkerad patient we check sekr state through the PU-service.
        String intygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(typAvUtlatande.getCode());
        if (!commonAuthoritiesResolver.getSekretessmarkeringAllowed().contains(intygsTyp)) {

            Personnummer pnr = Personnummer.createValidatedPersonnummerWithDash(personId.getExtension()).orElse(null);

            if (pnr != null) {
                final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(pnr);
                switch (sekretessStatus) {
                case TRUE:
                    errors.addError("Cannot issue intyg type {0} for patient having "
                            + "sekretessmarkering.", intygsTyp);
                    break;
                case UNDEFINED:
                    errors.addError("Cannot issue intyg type {0} for unknown patient. Might be due "
                            + "to a problem in the PU service.", intygsTyp);
                    break;
                case FALSE:
                    break; // Do nothing
                }
            }
        } else {
            // Check if user has PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT or return error
            IntygUser user = webcertUserDetailsService.loadUserByHsaId(skapadAv.getPersonalId().getExtension());
            AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
            if (!authoritiesValidator.given(user)
                    .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT)
                    .isVerified()) {
                errors.addError(
                        "Du saknar behörighet. För att hantera intyg för patienter med sekretessmarkering krävs "
                        + "att du har befattningen läkare eller tandläkare");
            }
        }
    }

    private void validateTypAvUtlatande(TypAvUtlatande typAvUtlatandeType, ResultValidator errors) {
        String intygsTyp = typAvUtlatandeType.getCode();

        if (!moduleRegistry.moduleExists(intygsTyp)) {
            errors.addError("Intyg {0} is not supported", intygsTyp);
        }
    }

    private void validatePatient(Patient patient, ResultValidator errors) {
        if (Strings.nullToEmpty(patient.getEfternamn()).trim().isEmpty()) {
            errors.addError("efternamn is required");
        }

        if (patient.getFornamn() == null || patient.getFornamn().isEmpty()) {
            errors.addError("At least one fornamn is required");
        }

        if (patient.getPersonId() == null || Strings.nullToEmpty(patient.getPersonId().getExtension()).trim().isEmpty()) {
            errors.addError("personId is required");
        } else {
            PersonnummerChecksumValidator.validate(new Personnummer(patient.getPersonId().getExtension()), errors);
        }
    }

    private void validateSkapadAv(HosPersonal skapadAv, ResultValidator errors) {
        if (Strings.nullToEmpty(skapadAv.getFullstandigtNamn()).trim().isEmpty()) {
            errors.addError("Physicians full name is required");
        }

        if (skapadAv.getPersonalId() == null || Strings.nullToEmpty(skapadAv.getPersonalId().getExtension()).trim().isEmpty()) {
            errors.addError("Physicians hsaId is required");
        }

        validateEnhet(skapadAv.getEnhet(), errors);
    }

    private void validateEnhet(Enhet enhet, ResultValidator errors) {
        if (enhet == null) {
            errors.addError("Enhet is missing");
        } else {
            if (Strings.nullToEmpty(enhet.getEnhetsnamn()).trim().isEmpty()) {
                errors.addError("enhetsnamn is required");
            }

            if (enhet.getEnhetsId() == null || Strings.nullToEmpty(enhet.getEnhetsId().getExtension()).trim().isEmpty()) {
                errors.addError("enhetsId is required");
            }
        }
    }

}
