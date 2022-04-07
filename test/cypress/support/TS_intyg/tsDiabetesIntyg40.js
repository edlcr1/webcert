// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert
//TSTRK1031 3.0 TS Diabetes
// Datumen är inte specifika för något testfall
export {besökÖnskadUrl, loggaUtLoggaIn} from '../FK_intyg/fk_helpers';

// -------------------- 'Intyget avser' --------------------
export function sektionIntygetAvser(intygetAvser) {

    if (intygetAvser) {
        // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
        // sidan ibland inte har hunnit ladda innan den får timeout.
        // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
        // man kör lokalt

        var körkortsTyper = {
            "AM": '#intygAvser\\.kategorier-IAV11',
            "A1": '#intygAvser\\.kategorier-IAV12',
            "A2": '#intygAvser\\.kategorier-IAV13',
            "A": '#intygAvser\\.kategorier-IAV14',
            "B": '#intygAvser\\.kategorier-IAV15',
            "BE": '#intygAvser\\.kategorier-IAV16',
            "Traktor": '#intygAvser\\.kategorier-IAV17',
            "C1": '#intygAvser\\.kategorier-IAV18',
            "C1E": '#intygAvser\\.kategorier-IAV19',
            "C": '#intygAvser\\.kategorier-IAV3',
            "CE": '#intygAvser\\.kategorier-IAV4',
            "D1": '#intygAvser\\.kategorier-IAV5',
            "D1E": '#intygAvser\\.kategorier-IAV6',
            "D": '#intygAvser\\.kategorier-IAV7',
            "DE": '#intygAvser\\.kategorier-IAV8',
            "Taxi": '#intygAvser\\.kategorier-IAV9'
        };

        for (var key in intygetAvser) {
            if (intygetAvser[key]) {
                cy.get(körkortsTyper[key], {timeout: 60000}).check();
            }
        }
    }
}

// -------------------- 'Identitet' --------------------
export function sektionIdentitet(identitet) {
    if (identitet) {

        var indentifieringsTyper = {
            "IDkort": '#identitetStyrktGenom\\.typ-IDK1',
            "FöretagskortTjänstekort": '#identitetStyrktGenom\\.typ-IDK2',
            "Körkort": '#identitetStyrktGenom\\.typ-IDK3',
            "PersonligKännedom": '#identitetStyrktGenom\\.typ-IDK4',
            "Försäkran": '#identitetStyrktGenom\\.typ-IDK5',
            "Pass": '#identitetStyrktGenom\\.typ-IDK6'
        }

        for (var key in identitet) {
            if (identitet[key]) {
                cy.get(indentifieringsTyper[key]).check();
            }
        }
    }
}

// -------------------- 'Allmänt' --------------------
export function sektionAllmänt(Allmänt, pnr) {

    // Lösning för år då diagnosen diabetes ställdes är patientens födelseår + 5
    var fyraFörstaPlusFem = parseInt(pnr.substring(0, 4)) + 5;
    fyraFörstaPlusFem = fyraFörstaPlusFem.toString();

    var fyraFörstaPlusSex = parseInt(pnr.substring(0, 4)) + 6;
    fyraFörstaPlusSex = fyraFörstaPlusSex.toString();
    

    cy.log('År då diagnosen för diabetes ställdes: ' + fyraFörstaPlusFem);

    cy.get('#allmant-diabetesDiagnosAr').type(fyraFörstaPlusFem);

    if (Allmänt.diabetes.typ1) {
        cy.get('#allmant\\.typAvDiabetes-TYP1').check();
    } else if (Allmänt.diabetes.typ2) {
        cy.get('#allmant\\.typAvDiabetes-TYP2').check();
    } else {
        cy.get('#allmant\\.typAvDiabetes-ANNAN').check();
        cy.get('#allmant-beskrivningAnnanTypAvDiabetes').type(Allmänt.diabetes.annanText);
    }

    if (Allmänt.behandling.kost) {
        cy.get('#allmant-behandling-endastKost').check();
    }

    if (Allmänt.behandling.tabletter.ja) {
        cy.get('#allmant-behandling-tabletter').check();
        if (Allmänt.behandling.tabletter.hypoglykemiRisk) {
            cy.get('#allmant-behandling-tablettRiskHypoglykemiYes').check();
        } else {
            cy.get('#allmant-behandling-tablettRiskHypoglykemiNo').check();
        }
    }

    if (Allmänt.behandling.insulin.ja) {
        cy.get('#allmant-behandling-insulin').check();
        cy.get('#allmant-behandling-insulinSedanAr').type(fyraFörstaPlusSex);
    }

    if (Allmänt.behandling.annanBehandling.ja) {
        cy.get('#allmant-behandling-annanBehandling').check();
        cy.get('#allmant-behandling-annanBehandlingBeskrivning').type(Allmänt.behandling.annanBehandling.text);
    }

}

// -------------------- 'Hypoglykemier' --------------------
export function sektionHypoglykemier(hypoglykemier) {

    cy.get('body').then(($body) => {
        if ($body.text().includes('Hypoglykemier')) {
            if (hypoglykemier.kontroll) {
                cy.get('#hypoglykemier-sjukdomenUnderKontrollYes').check();
            } else {
                cy.get('#hypoglykemier-sjukdomenUnderKontrollNo').check();
            }
            if (hypoglykemier.hjärnfunktion) {
                cy.get('#hypoglykemier-nedsattHjarnfunktionYes').check();
            } else {
                cy.get('#hypoglykemier-nedsattHjarnfunktionNo').check();
            }
            if (hypoglykemier.risker) {
                cy.get('#hypoglykemier-forstarRiskerYes').check();
            } else {
                cy.get('#hypoglykemier-forstarRiskerNo').check();
            }
            if (hypoglykemier.symptomen) {
                cy.get('#hypoglykemier-fortrogenMedSymptomYes').check();
            } else {
                cy.get('#hypoglykemier-fortrogenMedSymptomNo').check();
            }
            if (hypoglykemier.varningstecken) {
                cy.get('#hypoglykemier-saknarFormagaVarningsteckenYes').check();
            } else {
                cy.get('#hypoglykemier-saknarFormagaVarningsteckenNo').check();
            }
            if (hypoglykemier.åtgärder) {
                cy.get('#hypoglykemier-kunskapLampligaAtgarderYes').check();
            } else {
                cy.get('#hypoglykemier-kunskapLampligaAtgarderNo').check();
            }
            if (hypoglykemier.blodsocker) {
                cy.get('#hypoglykemier-egenkontrollBlodsockerYes').check();
            } else {
                cy.get('#hypoglykemier-egenkontrollBlodsockerNo').check();
            }
            if (hypoglykemier.senasteÅret) {
                cy.get('#hypoglykemier-aterkommandeSenasteAretYes').check();
                const idagMinus6Mån  = Cypress.moment().subtract(6,  'months').format('YYYY-MM-DD');
                cy.get('#datepicker_hypoglykemier\\.aterkommandeSenasteTidpunkt').type(idagMinus6Mån);

            } else {
                cy.get('#hypoglykemier-aterkommandeSenasteAretNo').check();
            }
            if (hypoglykemier.senasteTreMån) {
                cy.get('#hypoglykemier-aterkommandeSenasteKvartaletYes').check();
                const idagMinus2Mån  = Cypress.moment().subtract(2,  'months').format('YYYY-MM-DD');
                cy.get('#datepicker_hypoglykemier\\.senasteTidpunktVaken').type(idagMinus2Mån);
            } else {
                cy.get('#hypoglykemier-aterkommandeSenasteKvartaletNo').check();
            }
            if (hypoglykemier.trafiken) {
                cy.get('#hypoglykemier-forekomstTrafikYes').check();
                const idagMinus4Mån  = Cypress.moment().subtract(4,  'months').format('YYYY-MM-DD');
                cy.get('#datepicker_hypoglykemier\\.forekomstTrafikTidpunkt').type(idagMinus4Mån);
            } else {
                cy.get('#hypoglykemier-forekomstTrafikNo').check();
            }
        }
    });
}

// -------------------- 'Synfunktioner' --------------------
export function sektionSynfunktioner(synfunktioner) {

    if (synfunktioner.ögonsjukdomar) {
        cy.get('#synfunktion-misstankeOgonsjukdomYes').check();
    } else {
        cy.get('#synfunktion-misstankeOgonsjukdomNo').check();
    }
  /*  if (synfunktioner.ögonbottenfoto) {
        cy.get('#synfunktion-ogonbottenFotoSaknasYes').check();
    } else {
        cy.get('#synfunktion-ogonbottenFotoSaknasNo').check();
    }*/
    
    var ögaText = [synfunktioner.synskärpa.högerÖga.utanKorrektion.ja, synfunktioner.synskärpa.högerÖga.medKorrektion.ja, 
        synfunktioner.synskärpa.vänsterÖga.utanKorrektion.ja, synfunktioner.synskärpa.vänsterÖga.medKorrektion.ja,
        synfunktioner.synskärpa.binokulärt.utanKorrektion.ja, synfunktioner.synskärpa.binokulärt.medKorrektion.ja];

    var ögaVärde = [synfunktioner.synskärpa.högerÖga.utanKorrektion.värde, synfunktioner.synskärpa.högerÖga.medKorrektion.värde, 
        synfunktioner.synskärpa.vänsterÖga.utanKorrektion.värde, synfunktioner.synskärpa.vänsterÖga.medKorrektion.värde,
        synfunktioner.synskärpa.binokulärt.utanKorrektion.värde, synfunktioner.synskärpa.binokulärt.medKorrektion.värde];

    var ögaEle = {
        "synfunktioner.synskärpa.högerÖga.utanKorrektion.ja": '#synfunktion-hoger-utanKorrektion',
        "synfunktioner.synskärpa.högerÖga.medKorrektion.ja": '#synfunktion-hoger-medKorrektion',
        "synfunktioner.synskärpa.vänsterÖga.utanKorrektion.ja": '#synfunktion-vanster-utanKorrektion',
        "synfunktioner.synskärpa.vänsterÖga.medKorrektion.ja": '#synfunktion-vanster-medKorrektion',
        "synfunktioner.synskärpa.binokulärt.utanKorrektion.ja": '#synfunktion-binokulart-utanKorrektion',
        "synfunktioner.synskärpa.binokulärt.medKorrektion.ja": '#synfunktion-binokulart-medKorrektion'
    }

    var keyNames = Object.keys(ögaEle);

    for (var i=0; i < ögaText.length; i++) {
        if (ögaText[i]) {
            var keyName = keyNames[i];
            cy.get(ögaEle[keyName]).type(ögaVärde[i]);
        }
    }
}

// -------------------'Övrigt' -----------------------
export function sektionÖvrigt(övrigt) {
    if (övrigt.ja) {
        cy.get('#ovrigt').type(övrigt.text);
    }
}

// -------------------'Bedömning' -----------------------
export function sektionBedömning(bedömning) {

    var bedömingAlternativ = {
        "AM": '#bedomning\\.uppfyllerBehorighetskrav-VAR12',
        "A1": '#bedomning\\.uppfyllerBehorighetskrav-VAR13',
        "A2": '#bedomning\\.uppfyllerBehorighetskrav-VAR14',
        "A": '#bedomning\\.uppfyllerBehorighetskrav-VAR15',
        "B": '#bedomning\\.uppfyllerBehorighetskrav-VAR16',
        "BE": '#bedomning\\.uppfyllerBehorighetskrav-VAR17',
        "Traktor": '#bedomning\\.uppfyllerBehorighetskrav-VAR18',
        "C1": '#bedomning\\.uppfyllerBehorighetskrav-VAR1',
        "C1E": '#bedomning\\.uppfyllerBehorighetskrav-VAR2',
        "C": '#bedomning\\.uppfyllerBehorighetskrav-VAR3',
        "CE": '#bedomning\\.uppfyllerBehorighetskrav-VAR4',
        "D1": '#bedomning\\.uppfyllerBehorighetskrav-VAR5',
        "D1E": '#bedomning\\.uppfyllerBehorighetskrav-VAR6',
        "D": '#bedomning\\.uppfyllerBehorighetskrav-VAR7',
        "DE": '#bedomning\\.uppfyllerBehorighetskrav-VAR8',
        "Taxi": '#bedomning\\.uppfyllerBehorighetskrav-VAR9'
    };

    if (bedömning.kanInteTaStällning) {
        cy.get('#bedomning\\.uppfyllerBehorighetskrav-VAR11').check();
    } else {
        for (var key in bedömning.kanTaStällning) {
            if (bedömning.kanTaStällning[key]) {
                cy.get(bedömingAlternativ[key]).check();
            }
        }
    }

    var körkortLämplighet = [bedömning.kanTaStällning.C1, bedömning.kanTaStällning.C1E, bedömning.kanTaStällning.C,
        bedömning.kanTaStällning.CE, bedömning.kanTaStällning.D1, bedömning.kanTaStällning.D1E,
        bedömning.kanTaStällning.D, bedömning.kanTaStällning.DE, bedömning.kanTaStällning.Taxi];

    for (var key in körkortLämplighet) {
        if (key) {
            cy.get('body').then(($body) => {
                if ($body.text().includes('Är patienten lämplig att inneha behörighet för C1, C1E, C, CE, D1, D1E, D, DE eller taxi')) {
                    if (bedömning.lämplighet) {
                        cy.get('#bedomning-lampligtInnehavYes').check();
                    } else {
                        cy.get('#bedomning-lampligtInnehavNo').check();
                    }
                }
            });
        }
    }

    if (bedömning.specialistkompetens.ja) {
        cy.get('#bedomning-borUndersokasBeskrivning').type(bedömning.specialistkompetens.text);
    }
}

// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera intyget' --------------------
export function signera() {
    // TODO: Utan wait så tappas ofta slutet på texten bort i sista textboxen.
    // Antagligen hinner WebCert inte auto-spara innan man trycker på "signera".
    // Wait är dock ett anti-pattern så finns något annat sätt så är det att föredra.
    cy.wait(1000);

    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');

    // cy.click() fungerar inte alltid. Det finns ärenden rapporterade
    // (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // https://www.cypress.io/blog/2019/01/22/when-can-the-test-click/ :
    // "If a tree falls in the forest and no one has attached a “fall” event listener, did it really fall?"

    const click = $el => { return $el.click() }

    // Parent() p.g.a. att ett element täcker knappen
    cy.get('#signera-utkast-button').parent().should('be.visible')

    cy.get('#signera-utkast-button')
    .pipe(click, {timeout: 60000}) // ToDo: Lång timeout (problem endast på Jenkins, överlastad slav?)
    .should($el => {
        expect($el.parent()).to.not.be.visible;
    })
}

// -------------------- 'Skicka intyget' --------------------
export function skickaTillTs() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Transportstyrelsen");
}

// -------------------- 'Skriv ut intyget' --------------------
export function skrivUt(typAvUtskrift, intygsId, typAvIntyg){
    switch(typAvUtskrift) {
        case "utkast":
        case "fullständigt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/' + typAvIntyg +'/' + intygsId + "/pdf",
            });
            cy.log('Skriver ut ett ' + typAvUtskrift+ ' intyg (via cy.request, ej grafiskt)');
            break;
        default:
            cy.log('Ingen korrekt typ av utskrift vald');
    }
}
// ------------------'Ersätta intyg'---------------------------
export function ersatta() {
    cy.get('#ersattBtn').click();
    cy.get('#button1ersatt-dialog').click();
}

// ------------------'Radera utkast'--------------------------
export function raderaUtkast() {
    cy.get('#ta-bort-utkast').click();
    cy.get('#confirm-draft-delete-button').click();   
}

// ------------------'Makulera intyg'-------------------------
export function makuleraIntyg(arg) {
    cy.get('#makuleraBtn').click();
    if (arg === "Annat allvarligt fel") {
        cy.get('#reason-ANNAT_ALLVARLIGT_FEL').check();
        cy.get('#clarification-ANNAT_ALLVARLIGT_FEL').type('Testanledning');
        cy.get('#button1makulera-dialog').click();
    } else {
        cy.get('#reason-FEL_PATIENT').check();
        cy.get('#button1makulera-dialog').click();
    }
}