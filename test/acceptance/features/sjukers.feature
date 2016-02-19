# language: sv
@smoke @sjukers @notReady @luse
Egenskap: Hantera Läkarintyg, sjukersättning-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare

@sjukers @notReady
Scenario: Skapa och signera ett intyg
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg, sjukersättning" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"	
	När jag går till Mina intyg för patienten "19971019-2387"
	Så ska intyget finnas i Mina intyg

