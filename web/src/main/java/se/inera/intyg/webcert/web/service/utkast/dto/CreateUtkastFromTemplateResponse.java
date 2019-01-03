/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.utkast.dto;

public class CreateUtkastFromTemplateResponse {
    private String newDraftIntygType;

    private String newDraftIntygTypeVersion;

    private String newDraftIntygId;

    private String originalIntygId;

    public CreateUtkastFromTemplateResponse(String newDraftIntygType, String newDraftIntygTypeVersion, String newDraftIntygId,
            String originalIntygId) {
        this.newDraftIntygId = newDraftIntygId;
        this.newDraftIntygType = newDraftIntygType;
        this.newDraftIntygTypeVersion = newDraftIntygTypeVersion;
        this.originalIntygId = originalIntygId;
    }

    public String getNewDraftIntygType() {
        return newDraftIntygType;
    }

    public String getNewDraftIntygTypeVersion() {
        return newDraftIntygTypeVersion;
    }

    public String getNewDraftIntygId() {
        return newDraftIntygId;
    }

    public String getOriginalIntygId() {
        return originalIntygId;
    }

}
