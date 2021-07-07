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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ValidationMessageConverterImplTest {

    private ValidationMessageConverter validationMessageConverter = new ValidationMessageConverterImpl();

    @Test
    void shallReturnCodeWhenMessageIsMissingForTheCode() {
        final var code = "this.is.a.code";

        final var actualMessage = validationMessageConverter.codeToMessage(code);

        assertEquals(code, actualMessage, "If message is missing for a code, the code is expected to be returned");
    }

    static Stream<String> codes() {
        return Stream.of(
            "common.validation.date.year.not_selected",
            "common.validation.date.month.not_selected",
            "common.validation.date-period.invalid_format",
            "common.validation.date-period.invalid_order",
            "common.validation.date-period.period_overlap",
            "common.validation.date_out_of_range",
            "common.validation.date_out_of_range_no_future",
            "common.validation.date_invalid",
            "common.validation.future.datum",
            "common.validation.previous.datum",
            "common.validation.date.today.or.earlier",
            "common.validation.date.beforeLastYear",
            "common.validation.diagnos.missing",
            "common.validation.diagnos.codemissing",
            "common.validation.diagnos.invalid",
            "common.validation.diagnos.length-3",
            "common.validation.diagnos.psykisk.length-4",
            "common.validation.diagnos.description.missing",
            "common.validation.sjukskrivning_period.empty",
            "common.validation.sjukskrivning_period.invalid_format",
            "common.validation.sjukskrivning_period.incorrect_combination",
            "common.validation.sjukskrivning_period.period_overlap",
            "common.validation.sjukskrivning_period.en_fjardedel.invalid_format",
            "common.validation.sjukskrivning_period.halften.invalid_format",
            "common.validation.sjukskrivning_period.tre_fjardedel.invalid_format",
            "common.validation.sjukskrivning_period.helt_nedsatt.invalid_format",
            "common.validation.d-01",
            "common.validation.d-02",
            "common.validation.d-03",
            "common.validation.d-04",
            "common.validation.d-05",
            "common.validation.d-06",
            "common.validation.d-08",
            "common.validation.d-09",
            "common.validation.d-10",
            "common.validation.d-11",
            "common.validation.c-05",
            "common.validation.c-06",
            "common.validation.c-13a",
            "common.validation.b-04",
            "common.validation.b-02b",
            "common.validation.b-03a"
        );
    }

    @ParameterizedTest
    @MethodSource("codes")
    void shallReturnMessageForExistingCodes(String code) {
        final var actualMessage = validationMessageConverter.codeToMessage(code);

        assertNotEquals(code, actualMessage, () -> String.format("Expect '%s' to have a message specified for it", code));
    }
}