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

'use strict';

var BaseSkvIntygPage = require('../skv.base.intyg.page.js');
//var pageHelpers = require('../../../../pageHelper.util.js');

var DbIntyg = BaseSkvIntygPage._extend({
    init: function init() {
        init._super.call(this);

        this.identitetStyrkt = element(by.id('identitetStyrkt'));
        this.dodsdatumSakert = element(by.id('dodsdatumSakert'));
        this.dodsplats = {
            kommun: element(by.id('dodsplatsKommun')),
            boende: element(by.id('dodsplatsBoende'))
        };

        this.explosivImplantat = {
            value: element(by.id('explosivImplantat')),
            avlagsnat: element(by.id('explosivAvlagsnat'))
        };

        this.yttreUndersokning = {
            value: element(by.id('undersokningYttre')),
            datum: element(by.id('undersokningDatum'))
        };

        this.polisanmalan = element(by.id('polisanmalan'));
    },

    get: function get(intygId) {
        get._super.call(this, intygId);
    },

    verifyDodsdatum: function(dodsdatum) {

    },

    verifyDodsPlats: function(dodsPlats) {
        expect(this.dodsplats.kommun.getText()).toBe(dodsPlats.kommun);
        //expect(this.dodsplats.boende.getText()).toBe(dodsPlats.boende);
    },

    verifyExplosivImplantat: function(explosivImplantat) {
        if (explosivImplantat) {
            expect(this.explosivImplantat.value.getText()).toBe('Ja');
            expect(this.explosivImplantat.avlagsnat.getText()).toBe(explosivImplantat.avlagsnat ? 'Ja' : 'Nej');
        } else {
            expect(this.explosivImplantat.value.getText()).toBe('Nej');
            expect(this.explosivImplantat.avlagsnat.getText()).toBe('Ej angivet');
        }
    },

    verifyYttreUndersokning: function(yttreUndersokning) {
        if (yttreUndersokning.datum) {
            expect(this.yttreUndersokning.datum.getText()).toBe(yttreUndersokning.datum);
        } else {
            expect(this.yttreUndersokning.datum.getText()).toBe('Ej angivet');
        }
    },

    verifyPolisanmalan: function(polisanmalan) {
        expect(this.polisanmalan.getText()).toBe(polisanmalan ? 'Ja' : 'Nej');
    },

    verify: function(data) {
        expect(this.identitetStyrkt.getText()).toBe(data.identitetStyrktGenom);
        this.verifyDodsdatum(data.dodsdatum);
        this.verifyDodsPlats(data.dodsPlats);
        this.verifyExplosivImplantat(data.explosivImplantat);
        this.verifyYttreUndersokning(data.yttreUndersokning);
        this.verifyPolisanmalan(data.polisanmalan);
    }
});
module.exports = new DbIntyg();
