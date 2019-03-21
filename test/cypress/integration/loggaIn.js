/* globals context cy */
/// <reference types="Cypress" />

describe('Testinloggningssidan', function () {

    const destUrlLyckadInloggning = "/#/create/choose-patient/index";

    beforeEach(function() {
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.visit('/welcome.html');
    });

    it('har korrekt titel', function () {
		cy.title().should('contain', "Webcert test inloggning");
    });

    it('är mojligt att logga in vårdgivare genom inloggningsknappen', function() {
        cy.contains(this.vårdgivare.namnSträngInloggning).click().then(() => {
			cy.contains("Logga in").click();
        });

        cy.url().should('include', destUrlLyckadInloggning);
    });

    it('är möjligt att logga in vårdgivare genom dubbelklick i namnlistan', function() {
        cy.contains(this.vårdgivare.namnSträngInloggning).dblclick().then(() => {
			cy.url().should('include', destUrlLyckadInloggning);
        });
    });
});
