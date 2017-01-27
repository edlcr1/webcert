# language: sv
@faltvalidering @fk7263 
Egenskap: Fältvalidering fk7263

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient


Scenariomall: Validera felaktigt <typAvFält> i <intygsTyp>
	När jag går in på att skapa ett <intygsTyp> intyg
	Och jag fyller i text i <typAvFält> fältet
   	Så ska valideringsfelet <feltext> visas
	Och ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
 	Och ska jag se en lista med endast det saknade <fältet>
 	Och jag ändrar till giltig text i <typAvFält>
 	Så ska inget valideringsfel visas
 	Och ska jag inte se en lista med vad som saknas
 	Så ska utkastets statusheader meddela <meddelande>

Exempel:
 	| intygsTyp                     |	 typAvFält		     	| feltext       				    | fältet	| meddelande |
    |"Läkarintyg FK 7263"	|	"UndersökningsDatum"	| "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"			| "Intyget baseras på" |  "Utkastet är sparat, men obligatoriska uppgifter saknas." |

Scenario: Validera att meddelande visas angående vad intyget saknar
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag klickar på signera-knappen
	Så ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
	Och ska jag se en lista med vad som saknas
	Så ska valideringsfelet "Intyget baseras på" visas
	Så ska valideringsfelet "Diagnos/diagnoser" visas
	Så ska valideringsfelet "Funktionsnedsättning - observationer" visas
	Så ska valideringsfelet "Aktivitetsbegränsning" visas
	Så ska valideringsfelet "Arbete" visas
	Så ska valideringsfelet "Jag bedömer att" visas
	Så ska valideringsfelet "Rekommendationer"  inte visas
	Så ska valideringsfelet "Prognos"  inte visas
	Så ska valideringsfelet "Vårdenhetens kontaktuppgifter"  inte visas
	Och jag lägger till fältet "Diagnoskod"
	När jag fyller i blanksteg i "Funktionsnedsättnings" fältet
	När jag fyller i blanksteg i "Aktivitetsbegränsning" fältet
	När jag fyller i blanksteg i "Arbetsuppgifter" fältet
	Så ska valideringsfelet "Funktionsnedsättning - observationer" visas
	Så ska valideringsfelet "Aktivitetsbegränsning" visas
	Så ska valideringsfelet "Arbete" visas
	Och jag lägger till fältet "Arbete"
	Och jag lägger till fältet "Aktivitetsbegransning"
	Och jag lägger till fältet "Funktionsnedsattning"
	Och jag lägger till fältet "Arbetsförmåga"
	Och jag lägger till fältet "Intyget baseras på"
	Så ska valideringsfelet "Intyget baseras på"  inte visas
	Så ska valideringsfelet "Arbete"  inte visas
	Så ska valideringsfelet "Aktivitetsbegränsning"  inte visas
	Så ska valideringsfelet "Funktionsnedsättning"  inte visas
	Och jag signerar intyget
	Så ska intygets status vara "Intyget är signerat"


