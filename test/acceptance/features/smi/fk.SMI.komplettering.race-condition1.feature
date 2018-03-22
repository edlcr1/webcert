# language: sv
@race-condition-komplettering @notReady
 # kompletteringsfråga saknas på utkast-sida
Egenskap: Komplettering av SMI-intyg - Temorära test-scenarion instance 1


Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient
   
Scenariomall: Ska kunna besvara komplettering med nytt SMI-intyg #<Number>
   När jag går in på att skapa ett slumpat SMI-intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

   När Försäkringskassan skickar ett "KOMPLT" meddelande på intyget
   Och jag går in på intyget
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan
   
Exempel:
|Number|
|1|
|2|
|3|
|4|
|5|
|6|
|7|
|8|
|9|
|10|
|11|
|12|
|13|
|14|
|15|
|16|
|17|
|18|
|19|
|20|
|21|
|22|
|23|
|24|
|25|