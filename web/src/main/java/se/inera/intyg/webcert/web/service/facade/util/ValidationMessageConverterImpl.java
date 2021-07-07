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

package se.inera.intyg.webcert.web.service.facade.util;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.config.CertificateDataConfigTypes;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;

@Component
public class ValidationMessageConverterImpl implements ValidationMessageConverter {

    private static final Map<String, String> MESSAGES;

    static {
        MESSAGES = new HashMap<>();
        MESSAGES.put("common.validation.date.year.not_selected", "Ange år och månad.");
        MESSAGES.put("common.validation.date.month.not_selected", "Ange månad.");
        MESSAGES.put("common.validation.date-period.invalid_format", "Felaktigt datumformat.");
        MESSAGES.put("common.validation.date-period.invalid_order", "Ange ett slutdatum som infaller efter startdatumet.");
        MESSAGES.put("common.validation.date-period.period_overlap", "Datumintervall överlappar.");
        MESSAGES.put("common.validation.date_out_of_range", "Ange ett datum som inte ligger för långt fram eller tillbaka i tiden.");
        MESSAGES.put("common.validation.date_out_of_range_no_future", "Ange ett datum som inte ligger för långt tillbaka i tiden.");
        MESSAGES.put("common.validation.date_invalid", "Ange ett giltigt datum.");
        MESSAGES.put("common.validation.future.datum", "Ange ett giltigt datum. Framtida datum ger inga resultat.");
        MESSAGES.put("common.validation.previous.datum", "Ange ett giltigt datum. Datumet är för långt bak i tiden.");
        MESSAGES.put("common.validation.date.today.or.earlier", "Ange dagens eller ett tidigare datum.");
        MESSAGES.put("common.validation.date.beforeLastYear", "Ange ett datum, samma som eller senare än 1 januari föregående året.");
        MESSAGES.put("common.validation.diagnos.missing", "Ange minst en diagnos.");
        MESSAGES.put("common.validation.diagnos.codemissing", "Ange diagnoskod.");
        MESSAGES.put("common.validation.diagnos.invalid", "Diagnoskod är ej giltig.");
        MESSAGES
            .put("common.validation.diagnos.length-3", "Ange diagnoskod med så många positioner som möjligt, men minst tre positioner.");
        MESSAGES.put("common.validation.diagnos.psykisk.length-4", "Ange diagnosrad med minst fyra positioner på en psykisk diagnos.");
        MESSAGES.put("common.validation.diagnos.description.missing", "Ange diagnostext.");
        MESSAGES.put("common.validation.sjukskrivning_period.empty", "Ange ett datum.");
        MESSAGES.put("common.validation.sjukskrivning_period.invalid_format", "Ange datum i formatet åååå-mm-dd.");
        MESSAGES.put("common.validation.sjukskrivning_period.incorrect_combination", "Ange ett slutdatum som infaller efter startdatumet.");
        MESSAGES.put("common.validation.sjukskrivning_period.period_overlap", "Ange sjukskrivningsperioder som inte överlappar varandra.");
        MESSAGES.put(
            "common.validation.sjukskrivning_period.en_fjardedel.invalid_format",
            "Datum för nedsatt arbetsförmåga med 25% har angetts på felaktigt format.");
        MESSAGES.put(
            "common.validation.sjukskrivning_period.halften.invalid_format",
            "Datum för nedsatt arbetsförmåga med 50% har angetts på felaktigt format.");
        MESSAGES.put(
            "common.validation.sjukskrivning_period.tre_fjardedel.invalid_format",
            "Datum för nedsatt arbetsförmåga med 75% har angetts på felaktigt format.");
        MESSAGES.put(
            "common.validation.sjukskrivning_period.helt_nedsatt.invalid_format",
            "Datum för nedsatt arbetsförmåga med 100% har angetts på felaktigt format.");
        MESSAGES.put("common.validation.d-01", "Ange vilket år behandling med insulin påbörjades.");
        MESSAGES.put("common.validation.d-02", "Ange ett årtal mellan patientens födelsedatum och dagens datum.");
        MESSAGES.put("common.validation.d-03", "Ange synskärpa i intervallet 0,0 - 2,0.");
        MESSAGES.put("common.validation.d-04", "Ange ett datum inom det senaste året.");
        MESSAGES.put("common.validation.d-05", "Ange ett år, senare än patientens födelseår.");
        MESSAGES.put("common.validation.d-06", "Ange ett år, årets eller tidigare.");
        MESSAGES.put("common.validation.d-08", "Ange dagens eller ett tidigare datum.");
        MESSAGES.put("common.validation.d-09", "Ange ett datum som infaller under de senaste tolv månaderna");
        MESSAGES.put("common.validation.d-10", "Ange ett datum som infaller under de senaste tre månaderna");
        MESSAGES.put("common.validation.d-11", "Ange ett datum som infaller efter patientens födelsedatum.");
        MESSAGES.put("common.validation.c-05", "Fyll i den översta raden först.");
        MESSAGES.put("common.validation.c-06", "Ange ett giltigt datum. Framtida datum får inte anges.");
        MESSAGES.put("common.validation.c-13a", "Ange diagnos på översta raden först.");
        MESSAGES.put("common.validation.b-04", "Välj minst ett alternativ.");
        MESSAGES.put("common.validation.b-02b", "Ange ett giltigt årtal.");
        MESSAGES.put("common.validation.b-03a", "Ange ett svar.");
    }

    public String codeToMessage(String code) {
        return MESSAGES.getOrDefault(code, code);
    }

    @Override
    public String typeToMessage(ValidationMessageType validationType, CertificateDataConfigTypes configType) {
        // TODO: Implement logic to translate validationType and configType to a message.
        return null;
    }
}
