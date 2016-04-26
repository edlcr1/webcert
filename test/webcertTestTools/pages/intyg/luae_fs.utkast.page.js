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

/*globals element,by, Promise*/
'use strict';

var BaseUtkast = require('./base.utkast.page.js');

function sendKeysWithBackspaceFix(el, text) {
    return el.sendKeys(text)
        .then(function() {
            return el.sendKeys(protractor.Key.BACK_SPACE);
        })
        .then(function() {
            return el.sendKeys(text.substr(text.length - 1));
        });
}

var LuaefsUtkast = BaseUtkast._extend({
    init: function init() {
        init._super.call(this);

        this.at = element(by.css('.edit-form'));

        this.andraMedicinskaUtredningar = {
            finns: {
                JA: element(by.id('underlagFinnsYes')),
                NEJ: element(by.id('underlagFinnsNo'))
            },
            underlagRow: function(index) {
                index = index + 1; //skip header-row
                var row = element.all(by.css('tr.underlagRow')).get(index);
                return {
                    underlag: row.element(by.css('[name="andraUnderlag"]')),
                    datum: row.element(by.css('[name="-Date"]')),
                    information: row.element(by.css('.input-full'))
                };

            },
            laggTillUnderlagKnapp: element(by.cssContainingText('button', 'ytterligare underlag'))

        };

        this.diagnosKod = element(by.id('diagnoseCode'));

        this.funktionsnedsattningDebut = element(by.id('funktionsnedsattningDebut'));
        this.funktionsnedsattningPaverkan = element(by.id('funktionsnedsattningPaverkan'));

        this.ovrigt = element(by.id('ovrigt'));


        this.kontaktMedFkNo = element(by.id('formly_1_checkbox-inline_kontaktMedFk_0'));
        this.anledningTillKontakt = element(by.id('anledningTillKontakt'));

        this.tillaggsfragor0svar = element(by.id('tillaggsfragor[0].svar'));
        this.tillaggsfragor1svar = element(by.id('tillaggsfragor[1].svar'));

        this.baseratPa = {
            minUndersokningAvPatienten: {
                checkbox: element(by.id('formly_1_date_undersokningAvPatienten_3')),
                datum: element(by.id('form_undersokningAvPatienten')).element(by.css('input[type=text]'))
            },
            journaluppgifter: {
                checkbox: element(by.id('formly_1_date_journaluppgifter_4')),
                datum: element(by.id('form_journaluppgifter')).element(by.css('input[type=text]'))
            },
            anhorigBeskrivning: {
                checkbox: element(by.id('form_anhorigsBeskrivningAvPatienten')),
                datum: element(by.id('form_anhorigsBeskrivningAvPatienten')).element(by.css('input[type=text]'))
            },
            annat: {
                beskrivning: element(by.id('formly_1_single-text_annatGrundForMUBeskrivning_7')),
                checkbox: element(by.id('formly_1_date_annatGrundForMU_6')),
                datum: element(by.id('form_annatGrundForMU')).all(by.css('input[type=text]')).first()
            },
            kannedomOmPatient: {
                datum: element(by.id('form_kannedomOmPatient')).element(by.css('input[type=text]')),
                checkbox: element(by.id('formly_1_date_kannedomOmPatient_8'))
            }
        };
    },

    angeAndraMedicinskaUtredningar: function(utredningar) {
        var utredningarElement = this.andraMedicinskaUtredningar;

        var fillIn = function fillInUtr(val, index) {

            var laggTillUnderlag;
            if (index !== 0) {
                laggTillUnderlag = utredningarElement.laggTillUnderlagKnapp.sendKeys(protractor.Key.SPACE);
            }

            var row = utredningarElement.underlagRow(index);

            return Promise.all([
                laggTillUnderlag,
                sendKeysWithBackspaceFix(row.datum, val.datum),
                row.underlag.element(by.cssContainingText('option', val.underlag)).click(),
                row.information.sendKeys(val.infoOmUtredningen)
            ]);
        };

        if (utredningar) {
            return utredningarElement.finns.JA.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    browser.sleep(2000);
                    var actions = utredningar.map(fillIn);
                    return actions;
                });
        } else {
            return utredningarElement.finns.NEJ.sendKeys(protractor.Key.SPACE);
        }
    },

    angeUnderlagFinns: function(underlag) {
        if (!underlag) {
            return Promise.resolve('Success');
        }

        promisesArr = [];
        promisesArr.push(this.underlagDatePicker1.sendKeys(underlag.datum));
        Promise.all(promisesArr);

        var promisesArr = [];
        promisesArr.push(this.underlagSelect1.sendKeys(underlag.typ));
        Promise.all(promisesArr);



        promisesArr = [];
        promisesArr.push(this.underlagTextField1.sendKeys(underlag.hamtasFran));
        Promise.all(promisesArr);
    },

    angeIntygetBaserasPa: function(intygetBaserasPa) {
        if (!intygetBaserasPa) {
            return Promise.resolve('Success');
        }

        var promisesArr = [];

        if (intygetBaserasPa.minUndersokning) {
            // this.baserasPa.minUndersokning.checkbox.sendKeys(protractor.Key.SPACE);
            promisesArr.push(this.baseratPa.minUndersokningAvPatienten.datum.sendKeys(intygetBaserasPa.minUndersokning.datum));
        }
        if (intygetBaserasPa.journaluppgifter) {
            // this.baserasPa.journaluppgifter.checkbox.sendKeys(protractor.Key.SPACE);
            promisesArr.push(this.baseratPa.journaluppgifter.datum.sendKeys(intygetBaserasPa.journaluppgifter.datum));
        }
        if (intygetBaserasPa.anhorigBeskrivning) {
            // this.baserasPa.anhorigBeskrivning.checkbox.sendKeys(protractor.Key.SPACE);
            promisesArr.push(this.baseratPa.anhorigBeskrivning.datum.sendKeys(intygetBaserasPa.anhorigBeskrivning.datum));
        }
        if (intygetBaserasPa.annat) {
            // this.baserasPa.annat.checkbox.sendKeys(protractor.Key.SPACE);
            promisesArr.push(this.baseratPa.annat.datum.sendKeys(intygetBaserasPa.annat.datum));
            promisesArr.push(this.baseratPa.annat.text.sendKeys(intygetBaserasPa.annat.text));
        }
        return Promise.all(promisesArr);
    },


    get: function get(intygId) {
        get._super.call(this, 'luae_fs', intygId);
    },
    isAt: function isAt() {
        return isAt._super.call(this);
    }
});

module.exports = new LuaefsUtkast();
