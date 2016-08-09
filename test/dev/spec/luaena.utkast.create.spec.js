/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

/*globals beforeAll,describe,it,browser */
'use strict';
var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.restTestdata;
var UtkastPage = wcTestTools.pages.intyg.luae_na.utkast;
var IntygPage = wcTestTools.pages.intyg.luae_na.intyg;

describe('Create and Sign luae_na utkast', function() {

    var utkastId = null, data = null;

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
        specHelper.createUtkastForPatient('191212121212', 'Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga');
        browser.ignoreSynchronization = false;
    });

    describe('Skapa luae_na', function(){

        describe('Fyll i intyget', function() {

            it('Spara undan intygsId från URL', function() {
                UtkastPage.disableAutosave();

                browser.getCurrentUrl().then(function(url) {
                    utkastId = url.split('/').pop();
                });
                data = wcTestTools.testdata.fk.LUAE_NA.getRandom(utkastId);
            });


            it('angeBaseratPa', function() {
                UtkastPage.angeBaseratPa(data.baseratPa);
            });
            it('angeAndraMedicinskaUtredningar', function() {
                UtkastPage.angeAndraMedicinskaUtredningar(data.andraMedicinskaUtredningar);
            });
            it('angeDiagnos', function() {
                browser.ignoreSynchronization = false;
                UtkastPage.angeDiagnos(data.diagnos);
            });
            it('angeSjukdomsforlopp', function() {
                browser.ignoreSynchronization = true;
                UtkastPage.angeSjukdomsforlopp(data.sjukdomsForlopp);
            });
            it('angeFunktionsnedsattning', function() {
                UtkastPage.angeFunktionsnedsattning(data.funktionsnedsattning);
            });
            it('angeAktivitetsbegransning', function() {
                UtkastPage.angeAktivitetsbegransning(data.aktivitetsbegransning);
            });
            it('angeMedicinskBehandling', function() {
                UtkastPage.angeMedicinskBehandling(data.medicinskbehandling);
            });
            it('angeMedicinskaForutsattningar', function() {
                UtkastPage.enableAutosave();
                UtkastPage.angeMedicinskaForutsattningar(data.medicinskaForutsattningar);
            });
            it('angeOvrigaUpplysningar', function() {
                UtkastPage.angeOvrigaUpplysningar(data.ovrigt);
            });
            it('angeKontaktMedFK', function() {
                UtkastPage.angeKontaktMedFK(data.kontaktMedFk);
            });
        });

        it('Signera intyget', function() {
            UtkastPage.whenSigneraButtonIsEnabled().then(function() {
                browser.sleep(1000).then(function() {
                    UtkastPage.signeraButtonClick();

                    browser.sleep(1000).then(function() {
                        expect(IntygPage.isAt()).toBeTruthy();
                    });
                });
            });
        });

        it('Verifiera intyg', function() {
            IntygPage.whenCertificateLoaded().then(function() {
                IntygPage.verify(data);
            });
        });
    });

    afterAll(function() {
        testdataHelper.deleteIntyg(utkastId);
        testdataHelper.deleteUtkast(utkastId);
    });

});
