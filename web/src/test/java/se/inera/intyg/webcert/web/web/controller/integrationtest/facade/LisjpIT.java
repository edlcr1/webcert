/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonCertificateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonDraftIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonLockedCertificateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.complement.ComplementIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.fmb.UpdateFmbIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.send.SendIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.template.TemplateIT;

public class LisjpIT {

    private static final String CURRENT_VERSION = "1.3";
    private static final String AG7804_CURRENT_VERSION = "1.2";

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeCommonCertificateIT extends CommonCertificateIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected Boolean shouldReturnLatestVersion() {
            return true;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2", CURRENT_VERSION);
        }
    }

    @Nested
    class IncludeCommonDraftIT extends CommonDraftIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeCommonLockedCertificateIT extends CommonLockedCertificateIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2", CURRENT_VERSION);
        }

        @Override
        protected Boolean shouldReturnLatestVersion() {
            return true;
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeComplementIT extends ComplementIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2", CURRENT_VERSION);
        }

        @Override
        protected Boolean shouldReturnLastestVersion() {
            return true;
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeTemplateIT extends TemplateIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String expectedVersion() {
            return AG7804_CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2");
        }
    }

    @Nested
    class IncludeSendIT extends SendIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }
    }

    @Nested
    class IncludeUpdateFmbIT extends UpdateFmbIT {

        @Override
        protected String moduleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }
    }
}
