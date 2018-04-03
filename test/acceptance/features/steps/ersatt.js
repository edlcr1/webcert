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

/* globals protractor, intyg, browser, logger, Promise, wcTestTools */

'use strict';
/*jshint newcap:false */
//TODO Uppgradera Jshint p.g.a. newcap kommer bli depricated. (klarade inte att ignorera i grunt-task)


/*
 *	Stödlib och ramverk
 *
 */

const {
    Given, // jshint ignore:line
    When, // jshint ignore:line
    Then // jshint ignore:line
} = require('cucumber');



var helpers = require('./helpers');
var intygURL = helpers.intygURL;
var baseIntygPage = wcTestTools.pages.intyg.base.intyg;

/*
 *	Stödfunktioner
 *
 */


/*
 *	Test steg
 *
 */

Given(/^ska jag se en knapp med texten "([^"]*)"$/, function(btnTxt) {
    return expect(element(by.id('ersattBtn')).getText()).to.eventually.equal(btnTxt);
});

Given(/^jag klickar på ersätta knappen$/, function() {
    return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE);
});

Given(/^om jag klickar på ersätta knappen så ska det finnas en avbryt\-knapp med texten "([^"]*)"$/, function(btnText) {
    return element(by.id('ersattBtn')).sendKeys(protractor.Key.SPACE).then(function() {
        return element(by.css('.modal-dialog')).getText().then(function(modalText) {
            return expect(modalText).to.contain(btnText);
        });
    });
});

When(/^jag klickar på ersätt\-knappen i dialogen$/, function() {
    global.ersattintyg = JSON.parse(JSON.stringify(intyg));


    return element(by.id('button1ersatt-dialog')).sendKeys(protractor.Key.SPACE).then(function() {
        logger.info('Clicked ersätt button');
        return helpers.pageReloadDelay().then(function() {
            return browser.getCurrentUrl().then(function(text) {
                intyg.id = text.split('/').slice(-2)[0];
                intyg.id = intyg.id.split('?')[0];
                logger.info('intyg.id:' + intyg.id);
            });
        });
    });
});

Given(/^jag går tillbaka till det ersatta intyget$/, function() {
    return helpers.pageReloadDelay().then(function() {
        var url = intygURL(global.ersattintyg.typ, global.ersattintyg.id);
        return helpers.getUrl(url);
    });
});

Given(/^ska jag se texten "([^"]*)" som innehåller en länk till det ersatta intyget$/, function(replacedMessage) {
    var replaceMsg = element(by.id('wc-intyg-replaced-message'));
    return replaceMsg.isPresent().then(function(isPresent) {
        if (isPresent) {
            return expect(replaceMsg.getText()).to.eventually.contain(replacedMessage);
        } else {
            return expect(element(by.id('intyg-already-replaced-warning')).getText()).to.eventually.contain(replacedMessage);
        }

    });
});

Given(/^ska meddelandet som visas innehålla texten "([^"]*)"$/, function(modalMsg) {
    return expect(element(by.css('.modal-body')).getText()).to.eventually.contain(modalMsg);
});

Given(/^ska det( inte)? finnas knappar för "([^"]*)"( om intygstyp är "([^"]*)")?$/, function(inte, buttons, typ) {

    if (typ && intyg.typ !== typ) {
        logger.silly('Intygstyp är inte ' + typ);
        return Promise.resolve();
    }

    buttons = buttons.split(',');
    var shouldBeDisplayed = typeof(inte) === 'undefined';
    var promiseArr = [];
    buttons.forEach(function(button) {
        switch (button) {
            case 'skicka':
                promiseArr.push(expect(helpers.elementIsUsable(baseIntygPage.skicka.knapp)).to.become(shouldBeDisplayed));
                break;
            case 'ersätta':
                promiseArr.push(expect(helpers.elementIsUsable(baseIntygPage.replace.button)).to.become(shouldBeDisplayed));
                break;
            case 'förnya':
                promiseArr.push(expect(helpers.elementIsUsable(baseIntygPage.fornya.button)).to.become(shouldBeDisplayed));
                break;
            case 'makulera':
                promiseArr.push(expect(helpers.elementIsUsable(baseIntygPage.makulera.btn)).to.become(shouldBeDisplayed));
                break;
            case 'fråga/svar':
                promiseArr.push(expect(helpers.elementIsUsable(baseIntygPage.fragaSvar.administrativFraga.menyVal)).to.become(shouldBeDisplayed));
                promiseArr.push(expect(helpers.elementIsUsable(baseIntygPage.fragaSvar.komplettering.menyVal)).to.become(shouldBeDisplayed));
                break;
            default:
                throw ('Felaktig check. Hantering av knapp: ' + button + ' finns inte');
        }
    });
    return Promise.all(promiseArr);
});
