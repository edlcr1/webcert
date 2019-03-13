﻿/* globals context cy */
/// <reference types="Cypress" />

describe('(normalinloggat) skicka maximalt ifyllt LISJP till FK', function () {

	before(function () {
		cy.fixture('FK_intyg/lisjpData').as('lisjpData');
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
	});

	it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
		cy.loggaInVårdgivareNormal(this);
		cy.visit('/#/create/choose-intyg-type/' + this.vårdtagare.personnummer + '/index');
		cy.get("#intygTypeFortsatt-lisjp").click().fyllIMaxLisjp(this);
	});
});