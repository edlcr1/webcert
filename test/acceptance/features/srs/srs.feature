#language: sv

@SRS
Egenskap: Webcert visa information från SRS i form av prediktioner på sjukskrivningslängd, statistikbilder och åtgärdsförslag

Bakgrund: 
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "med SRS"
    

@SRS-US-W01
@SRS-US-W02
@SRS-US-W03
@SRS-US-W04
Scenario: När samtycke är givet och ytterligare frågor besvarade ska information från SRS visas.
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    När jag anger att patienten samtycker till SRS
    Och jag fyller i ytterligare svar för SRS
    Och jag trycker på knappen "Visa"
    Så ska prediktion från SRS-tjänsten visas
    Och ska åtgärdsförslag från SRS-tjänsten visas
    När jag trycker på fliken "Statistik"
    Så ska en statistikbild från SRS-tjänsten visas


@SRS-US-W01
Scenario: SRS-knappen ska bara visas när diagnos som har stöd för SRS är ifylld
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag fyller i diagnoskod som "inte finns i SRS"
    Så ska knappen för SRS vara i läge "gömd"

@SRS-US-W01
Scenario: Samtycken som patienter har givit ska lagras
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Ja"

@SRS-US-W01
Scenario: Patient som inte givit samtycke ska ha samtyckesfrågan förifyllt som "nej"
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Nej"

@SRS-US-W01
Scenario: Användaren ska kunna visa och dölja UI-komponenter som hör till SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag klickar på knappen för SRS
    Och ska en frågepanel för SRS "visas"
    Och ska en pil med texten "Visa mindre" visas
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "minimerad"
    Och ska en pil med texten "Visa mer" visas
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "maximerad"

@SRS-US-W01
@notReady
@WIP
Scenario: Prediktion ska kunna visa förhöjd risk
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "har förhöjd risk"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska meddelandet "Förhöjd risk" visas

@SRS-US-W01
@notReady
Scenario: Prediktion ska kunna visa ingen förhöjd risk
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "för ingen förhöjd risk"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska meddelandet "Förhöjd risk" visas

@SRS-US-W02
@notReady
Scenario: Användaren ska kunna ta del av åtgärdsförslag från SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "har åtgärder"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska OBS-åtgärder från "åtgärdslista 1" visas
    Och ska REK-åtgärder från "åtgärdslista 2" visas


@SRS-US-W02
@notReady
Scenario: När åtgärdsförslag inte kan ges ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar åtgärder"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska felmeddelandet "Åtgärdsförslag saknas" visas

@SRS-US-W03
@notReady
Scenario: När prediktion inte kan ges ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar prediktion"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska felmeddelandet "Prediktion saknas" visas


@SRS-US-W04
@notReady
Scenario: När statistikbild för en viss diagnoskod saknas ska användaren informeras.
Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar statistik"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska felmeddelandet "Prediktion saknas" visas