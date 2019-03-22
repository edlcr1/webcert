import {implementeradeIntyg} from './../commands'

const implementeradeIntygArray = Object.values(implementeradeIntyg);

export function sektion_grund_för_medicinskt_underlag(intygsdata, intygstyp) {
    expect(intygsdata).to.exist;
    expect(implementeradeIntygArray).to.include.members([intygstyp]);

    // -------------------- 'Intyget/utlåtandet är baserat på' --------------------
    /*
        Antagande: Det är inte viktigt med exakt samma datum relativt "idag"
        som används i mallarna.
    */
    const idagMinus5 =  Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD');
    const idagMinus6 =  Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD');
    const idagMinus14 = Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD');
    const idagMinus15 = Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD');
    const idagMinus2Mån = Cypress.moment().subtract(2, 'months').format('YYYY-MM-DD');

    // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
    // sidan ibland inte har hunnit ladda innan den får timeout.
    // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
    // man kör lokalt.
    cy.get('#checkbox_undersokningAvPatienten', {timeout: 60000}).check();
    cy.get("#datepicker_undersokningAvPatienten").clear().type(idagMinus5);

    if (intygstyp === implementeradeIntyg.LISJP) {
        cy.get('#checkbox_telefonkontaktMedPatienten').check();
        cy.get("#datepicker_telefonkontaktMedPatienten").clear().type(idagMinus6);
    }

    cy.get('#checkbox_journaluppgifter').check();
    cy.get("#datepicker_journaluppgifter").clear().type(idagMinus15);

    if (intygstyp !== implementeradeIntyg.LISJP) {
        cy.get('#checkbox_anhorigsBeskrivningAvPatienten').check();
        cy.get("#datepicker_anhorigsBeskrivningAvPatienten").clear().type(idagMinus6);
    }

    cy.get('#checkbox_annatGrundForMU').check();
    cy.get("#datepicker_annatGrundForMU").clear().type(idagMinus14);

    // Denna textruta dyker upp efter att "Annat" har klickats i
    cy.get("#annatGrundForMUBeskrivning").type(intygsdata.annat);

    if (intygstyp !== implementeradeIntyg.LISJP) {
        cy.get("#datepicker_kannedomOmPatient").clear().type(idagMinus14);

        // Klicka i att utlåtandet även baseras på andra medicinska
        // utredningar eller underlag. Detta gör att nya fält visualiseras
        cy.get("#underlagFinnsYes").click();

        /* Fyll i fält för utredning/underlag:
           - Klicka på raden för att fälla ut dropdown
           - Baserat på  id:t för dropdownen, verifiera att specifik text finns och välj den raden
           - Klicka på raden
           - Lokalisera datumfältet och fyll i datum
           - Lokalisera textfältet där vårdgivare ska anges, fyll i text
        */

        // Fyll i alla fält för utredning/underlag 1:
        cy.get("#underlag-0--typ-selected-item-label").click();
        cy.get("#wcdropdown-underlag-0--typ")
        .contains(intygsdata.underlag1)
        .then(option => {
            cy.wrap(option).contains(intygsdata.underlag1); // Säkerställ att rätt alternativ valts
            option[0].click(); // jquery "click()", inte Cypress "click()"
        });

        cy.get("#datepicker_underlag\\[0\\]\\.datum").clear().type(idagMinus2Mån);
        cy.get("#underlag-0--hamtasFran").type(intygsdata.underlag1HämtasFrån);

        // Fyll i alla fält för utredning/underlag 2
        cy.get("#underlag-1--typ-selected-item-label").click();
        cy.get("#wcdropdown-underlag-1--typ")
        .contains(intygsdata.underlag2)
        .then(option => {
            cy.wrap(option).contains(intygsdata.underlag2); // Säkerställ att rätt alternativ valts
            option[0].click(); // jquery "click()", inte Cypress "click()"
        });

        cy.get("#datepicker_underlag\\[1\\]\\.datum").clear().type(idagMinus2Mån);
        cy.get("#underlag-1--hamtasFran").type(intygsdata.underlag2HämtasFrån);

        // Fyll i alla fält för utredning/underlag 3
        cy.get("#underlag-2--typ-selected-item-label").click();
        cy.get("#wcdropdown-underlag-2--typ")
        .contains(intygsdata.underlag3)
        .then(option => {
            cy.wrap(option).contains(intygsdata.underlag3); // Säkerställ att rätt alternativ valts
            option[0].click(); // jquery "click()", inte Cypress "click()"
        });

        cy.get("#datepicker_underlag\\[2\\]\\.datum").clear().type(idagMinus2Mån);
        cy.get("#underlag-2--hamtasFran").type(intygsdata.underlag3HämtasFrån);
    }
}

export function sektion_diagnoser_för_sjukdom(intygsdata, intygstyp) {
    // Antag att ICD-10-SE är förvalt
    // Fyll i diagnoskoderna och verifiera att motsvarande beskriving kan läsas i textfältet
    cy.get('#diagnoseCode-0').parent().within(($diagnoskodrad) => {
        cy.get('#diagnoseCode-0').type(intygsdata.diagnosKod1);
        cy.wrap($diagnoskodrad).contains(intygsdata.diagnosText1).click();
    });
    cy.get('#diagnoseDescription-0').invoke('val').should('contain', intygsdata.diagnosText1);

    cy.get('#diagnoseCode-1').parent().within(($diagnoskodrad) => {
        cy.get('#diagnoseCode-1').type(intygsdata.diagnosKod2);
        cy.wrap($diagnoskodrad).contains(intygsdata.diagnosText2).click();
    });
    cy.get('#diagnoseDescription-1').invoke('val').should('contain', intygsdata.diagnosText2);

    cy.get('#diagnoseCode-2').parent().within(($diagnoskodrad) => {
        cy.get('#diagnoseCode-2').type(intygsdata.diagnosKod3);
        cy.wrap($diagnoskodrad).contains(intygsdata.diagnosText3).click();
    });
    cy.get('#diagnoseDescription-2').invoke('val').should('contain', intygsdata.diagnosText3);

    if (intygstyp === implementeradeIntyg.LUSE ||
        intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#diagnosgrund").type(intygsdata.diagnosgrund);

        // Finns skäl att revidera tidigare diagnos?
        cy.get("#nyBedomningDiagnosgrundYes").click();
        cy.get("#diagnosForNyBedomning").type(intygsdata.revideraTidigareDiagnos);
    }
}

export function sektion_aktivitetsbegränsningar(intygsdata) {
    cy.get("#aktivitetsbegransning").type(intygsdata.aktivitetsbegränsning);
}

export function sektion_medicinsk_behandling(intygsdata, intygstyp) {
    if (intygstyp === implementeradeIntyg.LUSE ||
        intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#avslutadBehandling").type(intygsdata.avslutadBehandling);
    }

    cy.get("#pagaendeBehandling").type(intygsdata.pågåendeBehandlingar);
    cy.get("#planeradBehandling").type(intygsdata.planeradeBehandlingar);

    if (intygstyp === implementeradeIntyg.LUSE ||
        intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#substansintag").type(intygsdata.substansintag);
    }
}

export function sektion_medicinska_förutsättningar_för_arbete(intygsdata, intygstyp) {
    cy.get("#medicinskaForutsattningarForArbete").type(intygsdata.förutsättningarFörArbete);
    cy.get("#formagaTrotsBegransning").type(intygsdata.förmågaTrotsBegränsning);
    if (intygstyp === implementeradeIntyg.LUAE_NA) {
        cy.get("#forslagTillAtgard").type(intygsdata.förslagTillÅtgärd);
    }
}

export function sektion_övriga_upplysningar(intygsdata) {
    cy.get("#ovrigt").type(intygsdata.övrigaUpplysningar);
}

export function sektion_kontakta_mig(intygsdata) {
    cy.get("#kontaktMedFk").check();
    cy.get("#anledningTillKontakt").type(intygsdata.kontaktMedFK);
}

export function sektion_signera_intyg(intygsdata) {
    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');

    // cy.click() fungerar inte alltid. Det finns issues rapporterade
    // (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // Har provat att vänta tills "Intyget är sparat" har försvunnit, och även provat
    // att inte kolla alls på den. I båda fallen misslyckas testfallet ofta ("Intyget
    // är sparat" försvinner inte ens efter lång tid och om man inte väntar alls så
    // klickas knappen "Signera intyget" men inget händer. Enda sättet att alltid
    // komma förbi detta steg i nuläget är med en wait()

    // Kan detta vara något? https://www.cypress.io/blog/2019/01/22/when-can-the-test-click/ :
    // "If a tree falls in the forest and no one has attached a “fall” event listener, did it really fall?"
    // cy.wait(5000);

    let count = 0
    const click = $el => {
        count += 1
        return $el.click()
    }

    // Parent() p.g.a. att ett element täcker knappen
    cy.get('#signera-utkast-button').parent().should('be.visible')

    cy.get('#signera-utkast-button')
    .pipe(click)
    .should($el => {
        expect($el.parent()).to.not.be.visible;
    })
    // TODO: Ta bort "then()" och "count" när/om det visar sig att pipe är ett stabilt alternativ till problemet.
    .then(() => {
        cy.log(count + ' ggr klickades Signera Intyg')
    })
}

export function skicka_till_FK(intygsdata) {
    cy.get("#sendBtn", { timeout: 60000 }).click();
    cy.get("#button1send-dialog").click(); // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.contains("Intyget är skickat till Försäkringskassan");
}

export function sektion_funktionsnedsättning(intygsdata) {
    cy.get('#toggle-funktionsnedsattningIntellektuell').click();
    cy.get('#funktionsnedsattningIntellektuell').type(intygsdata.funknedsättningIntellektuell);

    cy.get('#toggle-funktionsnedsattningKommunikation').click();
    cy.get('#funktionsnedsattningKommunikation').type(intygsdata.funknedsättningKommunikation);

    cy.get('#toggle-funktionsnedsattningKoncentration').click();
    cy.get('#funktionsnedsattningKoncentration').type(intygsdata.funknedsättningUppmärksamhet);

    cy.get('#toggle-funktionsnedsattningPsykisk').click();
    cy.get('#funktionsnedsattningPsykisk').type(intygsdata.funknedsättningPsykisk);

    cy.get('#toggle-funktionsnedsattningSynHorselTal').click();
    cy.get('#funktionsnedsattningSynHorselTal').type(intygsdata.funknedsättningSinne);

    cy.get('#toggle-funktionsnedsattningBalansKoordination').click();
    cy.get('#funktionsnedsattningBalansKoordination').type(intygsdata.funknedsättningBalans);

    cy.get('#toggle-funktionsnedsattningAnnan').click();
    cy.get('#funktionsnedsattningAnnan').type(intygsdata.funknedsättningAnnan);
}