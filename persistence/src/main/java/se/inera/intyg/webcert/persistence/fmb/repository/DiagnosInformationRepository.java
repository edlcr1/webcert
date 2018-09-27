package se.inera.intyg.webcert.persistence.fmb.repository;

/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import se.inera.intyg.webcert.persistence.fmb.model.icf.DiagnosInformation;

public interface DiagnosInformationRepository extends JpaRepository<DiagnosInformation, Long> {

    // CHECKSTYLE:OFF MethodName
    Optional<DiagnosInformation> findByIcd10KodList_kod(final String icd10Kod);
    // CHECKSTYLE:ON MethodName

}
