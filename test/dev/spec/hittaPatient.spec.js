/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/*globals pages */
/*globals describe,it,helpers */
'use strict';

var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.testdata;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.sokSkrivIntygIndex;
var SokSkrivValjUtkastType = wcTestTools.pages.sokSkrivIntyg.sokSkrivValjUtkastType;
var IntygPage = wcTestTools.pages.intyg.fkIntyg;

describe('Create and Sign FK utkast', function() {

    var utkastId = null;

    describe('Login through the welcome page', function() {
        it('with user', function() {
            browser.ignoreSynchronization = false;
            specHelper.login();
            SokSkrivIntygPage.selectPersonnummer('191212121212');
            expect(SokSkrivValjUtkastType.isAt()).toBe(true);
        });
    });

});
