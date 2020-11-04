/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/TS_intyg/tsDiabetesIntyg'
import * as overhopp from '../../../support/overhopp_helpers'

// TSTRK1031 = Transportstyrelsens läkarintyg, diabetes version 3.0

describe('TS-DIABETES-intyg', function () {
    
    before(function() {
        cy.fixture('TS_intyg/minTsDiabetes').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })

    beforeEach(function() {
        cy.skapaTsDiabetesUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("TS-DIABETES-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar ett TS-DIABETES intyg', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const normalUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad';
        const avlidenUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + '&avliden=true';
        const ändratNamnUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&fornamn=Gunilla&efternamn=Karlsson&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad';
        const originalPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad&alternatePatientSSn=' + this.vårdtagare.personnummerKompakt;
        const ändratPnrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad&alternatePatientSSn=191212121212';
        const reservNrUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id + 
        '&postadress=Bryggaregatan%2011&postnummer=65340&postort=Karlstad&alternatePatientSSn=19270926308A';
        //reservnummer = 19270926308A
        //samordningsnummer = 196812732391

        // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intygsutkast --- //

        // Verifiera att inga observandum visas när inga Overhoppsparametrar skickas med
        intyg.besökÖnskadUrl(normalUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);
        cy.contains("Intyget avser").should('exist');
        cy.get('#ta-bort-utkast').should('exist');
        cy.log('Overhoppsparametrar: Ingen');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att endast observandum med information om att patienten är avliden visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: avliden');
        cy.contains("Intyget avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: fornamn & efternamn');
        cy.contains("Intyget avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("patientNamn");

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: alternatePatientSSn (reservnummer)');
        cy.contains("Intyget avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga Overhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: Ingen');
        cy.contains("Intyget avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        cy.contains("Intyget avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga Overhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(originalPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat tillbaka personnummer)');
        cy.contains("Intyget avser");
        cy.get('#ta-bort-utkast').should('exist');
        overhopp.verifyPatStatus("ändratPnr");

        //Fyll i och signera intyget
        intyg.sektionIntygetAvser(this.intygsdata.intygetAvser);
        intyg.sektionIdentitet(this.intygsdata.identitet);
        intyg.sektionAllmänt(this.intygsdata.Allmänt, this.vårdtagare.personnummer);
        intyg.sektionHypoglykemier(this.intygsdata.hypoglykemier);
        intyg.sektionSynfunktioner(this.intygsdata.synfunktioner);
        intyg.sektionÖvrigt(this.intygsdata.övrigt);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.signera();

        // --- Verifera observandum triggade utifrån olika uthoppsparametrar i intyg --- //

        // Verifiera att endast observandum med information om att patienten är avliden visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(avlidenUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: avliden');
        cy.contains("Intyget avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("avliden");

        // Verifiera att endast observandum om att patientens namn skiljer sig från Journalsystemet visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratNamnUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: fornamn & efternamn');
        cy.contains("Intyget avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("patientNamn"); // För TS Diabetes 3.0 visas detta även för signerat intyg.

        // Verifiera att endast observandum om att personen har ett sammordningsnummer kopplat till ett reservnummer
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(reservNrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: alternatePatientSSn (reservnummer)');
        cy.contains("Intyget avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("reservnummer");

        // Verifiera att inga observandum visas när inga Overhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: Ingen');
        cy.contains("Intyget avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("utanParameter");

        // Verifiera att observandum om att patientens personnummer har ändrats visas
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(ändratPnrUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: alternatePatientSSn (ändrat personnummer)');
        cy.contains("Intyget avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("ändratPnr");

        // Verifiera att inga observandum visas när inga Overhoppsparametrar skickas med
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(normalUrl);
        cy.url().should('include', this.utkastId);
        cy.log('Overhoppsparametrar: Inga');
        cy.contains("Intyget avser");
        cy.get('#makuleraBtn').should('exist');
        overhopp.verifyPatStatus("utanParameter");

        // // Skickar intyget till FK samt populerar pdl-arrayen med förväntad logpost "Utskrift" med argument att det är skickat till FK
        // intyg.skickaTillFk();
    });
});
