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


/*globals describe,it,browser */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.soc.doi.utkast;
var IntygPage = wcTestTools.pages.intyg.soc.doi.intyg;

describe('Create and Sign doi utkast', function() {

    var utkastId = null,
        data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        testdataHelper.deleteAllUtkast();
        testdataHelper.deleteAllIntyg();
        specHelper.createUtkastForPatient('191212121212', 'doi');
    });

    it('Spara undan intygsId från URL', function() {
        UtkastPage.disableAutosave();

        specHelper.getUtkastIdFromUrl().then(function(id) {
            utkastId = id;
        });
        data = wcTestTools.testdata.soc.doi.getRandom(utkastId);

    });


    describe('Fyll i intyget', function() {
        it('angeIdentitetStyrktGenom', function() {
            UtkastPage.angeIdentitetStyrktGenom(data.identitetStyrktGenom);
        });
        it('angeDodsdatum', function() {
            UtkastPage.angeDodsdatum(data.dodsdatum);
        });
        it('angeDodsPlats', function() {
            UtkastPage.angeDodsPlats(data.dodsPlats);
        });
        it('angeBarn', function() {
            UtkastPage.angeBarn(data.barn);
        });
        it('angeUtlatandeOmDodsorsak', function () {
           UtkastPage.angeUtlatandeOmDodsorsak(data.dodsorsak);
        });
        it('angeOperation', function () {
           UtkastPage.angeOperation(data.operation);
        });
        it('angeSkadaForgiftning', function () {
           UtkastPage.angeSkadaForgiftning(data.skadaForgiftning);
        });
        it('angeDodsorsaksuppgifterna', function () {
            UtkastPage.angeDodsorsaksuppgifterna(data.dodsorsaksuppgifter);
            UtkastPage.enableAutosave();
        });
    });

    it('Signera intyget', function() {
        UtkastPage.whenSigneraButtonIsEnabled();
        UtkastPage.signeraButtonClick();

        expect(IntygPage.isAt()).toBeTruthy();
    });

    it('Verifiera intyg', function() {
        IntygPage.verify(data);
    });


    afterAll(function() {
        testdataHelper.deleteIntyg(utkastId);
        testdataHelper.deleteUtkast(utkastId);
    });
});