# language: sv
@behorighet @uthopp
Egenskap: Behörigheter för en uthoppsläkare

Bakgrund: Inloggad som uthoppsläkare
   Givet att jag är inloggad som uthoppsläkare

Scenario: Inloggad som uthoppsläkare
   Så ska jag ha rollen "LAKARE"
   Och jag ska ha origin "UTHOPP"

@signera @lisjp
Scenario: Kan signera intyg på lisjp
   När jag går in på en patient
   Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
   Och jag fyller i alla nödvändiga fält för intyget
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget
   
@signera @fk7263
Scenario: Kan signera intyg på fk7263
   När jag går in på en patient
   Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263" 
 Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

@fornya @lisjp
Scenario: Kan inte förnya intyg Läkarintyg för sjukpenning
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@fornya @ts @bas
Scenario: Kan inte förnya Transportstyrelsens läkarintyg
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@fornya @ts @diabetes
Scenario: Kan inte förnya Transportstyrelsens läkarintyg, diabetes intyg
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@makulera @lisjp
Scenario: Kan inte makulera intyg
   När jag går in på en patient
   Och jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"
   Så ska makuleraknappen inte vara tillgänglig

@filtrera @osignerade-intyg
Scenario: Kan inte filtrera osignerade intyg på läkare
   När går in på Ej signerade utkast
   Och väljer att visa sökfilter
   Så ska sökfiltret Sparat av inte vara tillgängligt

@fråga-från-fk @fk7263
Scenario: Ska kunna svara på frågor från Försäkringskassan
   När går in på Sök/skriv intyg
   Och jag går in på en patient
   
   ###### TODO ändra till att testa med lisjp intyg ##########################
   #När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"           -
   #När jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
   
   Givet att jag är inloggad som läkare
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan 
   
   Givet att jag är inloggad som uthoppsläkare
   ###########################################################################
   
   Och Försäkringskassan har ställt en "Avstamningsmote" fråga om intyget
   Och jag går in på intyget via uthoppslänk
   Och jag svarar på frågan
   Så kan jag se mitt svar under hanterade frågor

@nyttIntyg @fk7263
Scenario: Ska få varning vid svar med nytt intyg
   När jag går in på en patient
   
   ###### TODO ändra till att testa med lisjp intyg ##########################
   #När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"           -
   #När jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
   
   Givet att jag är inloggad som läkare
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan 
   
   Givet att jag är inloggad som uthoppsläkare
   ###########################################################################
   
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget via uthoppslänk
   Så ska jag se kompletteringsfrågan på intygs-sidan
   Och jag ska inte kunna komplettera med nytt intyg från webcert
   När jag klickar på svara knappen, fortfarande i uthoppsläge
   Och ska kompletteringsdialogen innehålla texten "förnya det befintliga intyget i journalsystemet och komplettera med den nya informationen"

@komplettera @fk7263
Scenario: Ska kunna besvara komplettering med textmeddelande via uthoppslänk
   När jag går in på en patient
   
   ###### TODO ändra till att testa med lisjp intyg ##########################
   #När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"    
   #När jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
   
   Givet att jag är inloggad som läkare
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på utkastet
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan 
   
   Givet att jag är inloggad som uthoppsläkare
   ###########################################################################
   
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Och jag går in på intyget via uthoppslänk
   Så ska jag se kompletteringsfrågan på intygs-sidan
   Och jag klickar på svara knappen, fortfarande i uthoppsläge
   Så jag ska inte kunna komplettera med nytt intyg från webcert
   Så ska svara med textmeddelande vara tillgängligt i dialogen
