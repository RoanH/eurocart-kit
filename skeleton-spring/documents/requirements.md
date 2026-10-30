# EuroCart – Requirements

Afgeleid uit [EuroCart_stakeholder_memos.md](./EuroCart_stakeholder_memos.md). Waar stakeholders elkaar tegenspreken of informatie ontbreekt, is dit opgenomen onder **Aannames en open punten** en niet als harde requirement vastgelegd.

## 1. Requirements

### 1.1 Webshop en checkout

- **R1 – Productcatalogus:** De webshop moet de catalogus met de 51 beschikbare producten kunnen tonen.
- **R2 – Productcategorie:** Ieder product moet een productcategorie hebben en deze moet voor de klant zichtbaar zijn.
- **R3 – Productbeschikbaarheid per markt:** De volledige catalogus mag in alle landen worden verkocht (bevestigd door Compliance). Er is geen beschikbaarheidsfilter per land nodig.
- **R4 – Product selecteren:** Een klant moet een product kunnen selecteren en aan een winkelmandje kunnen toevoegen.
- **R5 – Snelle checkout:** Een klant moet met zo min mogelijk stappen van productselectie naar bestelling kunnen gaan.
- **R6 – Duidelijke checkout:** Voor het afronden van de bestelling moet het volledige te betalen bedrag duidelijk zichtbaar zijn.
- **R7 – Berekening uitlegbaar:** De checkout/calculatiepagina moet zichtbaar maken hoe het bedrag is opgebouwd en waarom het is berekend, zodat een auditor de berekening kan controleren.
- **R8 – Verzendkosten:** Verzendkosten moeten pas in de checkout worden getoond.
- **R8a – Verzendtarief:** De verzendkosten zijn € 5,00 per bestelling, exclusief btw, in ieder land (bevestigd door Finance). Ze hangen niet af van producten, aantallen of gewicht. Over de verzendkosten wordt btw berekend (zie R8b).
- **R8b – Btw over verzendkosten:** Over de verzendkosten geldt in alle landen het hoge (standaard)tarief van het leverland, behalve in Zwitserland: daar geldt het lage tarief (bevestigd door Compliance). Het tarief hangt niet af van de producten in de bestelling.
- **R9 – E-books:** Voor e-books mogen geen verzendkosten worden berekend. Bestaat een bestelling alleen uit e-books, dan zijn de verzendkosten € 0. Bevat de bestelling ook fysieke producten, dan geldt het tarief uit R8a.

### 1.2 Prijzen, valuta en wisselkoersen

- **R10 – Catalogusprijzen:** Catalogusprijzen moeten in euro worden opgeslagen en zijn exclusief btw.
- **R11 – Lokale valuta:** Op de Nederlandse webshop moeten bedragen in euro worden getoond; op de Poolse webshop in Poolse zloty's. Het systeem moet lokale valuta per markt ondersteunen.
- **R12 – Valutaconversie:** Bedragen die vanuit euro worden omgerekend, moeten worden geconverteerd met een vastgestelde wisselkoers.
- **R13 – Kwartaalkoersen:** Wisselkoersen mogen binnen een kwartaal niet wijzigen voor de webshop. Een nieuwe koers gaat in op de eerste werkdag van een kwartaal. De koersen zijn configuratie per kwartaal; de checkoutdatum (Nederlandse tijd) bepaalt welke koers geldt, en elke bestelling slaat de gebruikte koers op. Werkdag = maandag t/m vrijdag, behalve 1 januari (zie A24).
- **R13a – Wisselkoersen (bron: Finance):** 1 GBP = € 1,20 · 1 CHF = € 1,09 · 1 PLN = € 0,23. Aangenomen voor Q4 2026 (zie A6).
- **R14 – Prijsweergave vóór checkout:** De klant moet vóór het plaatsen van de bestelling kunnen zien welk bedrag hij uiteindelijk moet betalen.
- **R14a – Leverland kiezen:** De klant kiest in de checkout het land waar de bestelling heen moet. Dit land bepaalt de btw-tarieven en de verzendkosten en is het land van het afleveradres. Bij een ander land wordt het te betalen bedrag direct opnieuw berekend. Alleen landen die live zijn, zijn te kiezen.

- **R14b – Prijzen incl. btw:** Alle prijzen die de klant op de website ziet (catalogus, winkelmandje, checkout) zijn inclusief de btw van het gekozen land. Bedragen exclusief btw worden in de checkout alleen getoond als de klant daarom vraagt. Intern blijven bedragen exclusief en inclusief btw beschikbaar voor audit. De getoonde productprijs incl. btw is een weergaveprijs (op centen afgerond); de berekening gaat uit van de exacte prijs exclusief btw (R22).

### 1.3 Btw en compliance

- **R15 – Marktgebonden btw:** Het systeem moet per verkoopland het toepasselijke btw-tarief toepassen.
- **R16 – Btw-berekening per product:** De btw moet per product kunnen worden geregistreerd en berekend, omdat tarieven per product kunnen verschillen.
- **R16a – Btw-tarieven:** De btw-tarieven per land zijn aangeleverd door Compliance (zie 1.3.1). Valt een productcategorie niet onder een genoemd verlaagd tarief, dan geldt het standaardtarief.

#### 1.3.1 Btw-tarieven (bron: Compliance)

| Land | Standaard | Verlaagd | Bijzonderheden |
|---|---|---|---|
| NL | 21% | 9% (boeken, voedsel, medicijnen) | Eurozone |
| DE | 19% | 7% (boeken, voedsel, kranten) | Eurozone |
| FR | 20% | 10% (restauratie, transport) / 5,5% (basisvoeding, energie) / 2,1% (medicijnen op recept) | Eurozone, drie verlaagde niveaus |
| PL | 23% | 8% (medicijnen, transport) / 5% (boeken, basisvoeding) / 0% (bepaalde basisvoeding) | Eigen munt (PLN), wisselende tarieven |
| CH | 8,1% | 2,6% (basisvoeding, boeken, medicijnen) | Eigen munt (CHF) |
| UK | 20% | 5% (energie, kinderautostoelen) / 0% (meeste voedsel, kinderkleding, boeken) | Eigen munt (GBP) |

Toepassing op de catalogus-categorieën:

| Categorie | NL | DE | FR | PL | CH | UK |
|---|---|---|---|---|---|---|
| LAPTOP | 21% | 19% | 20% | 23% | 8,1% | 20% |
| BOOK | 9% | 7% | 20% ¹ | 5% | 2,6% | 0% |
| EBOOK ² | 9% | 7% | 20% ¹ | 5% | 2,6% | 0% |
| FOOD | 9% | 7% | 5,5% | 5% ³ | 2,6% | 0% ³ |
| MEDICINE | 9% | 19% | 20% ⁴ | 8% | 2,6% | 20% |
| Verzendkosten ⁵ | 21% | 19% | 20% | 23% | 2,6% | 20% |

¹ Boeken staan voor FR niet in een verlaagd tarief, dus standaardtarief. Te bevestigen (zie A18).
² E-books zijn behandeld als boeken. Te bevestigen (zie A19).
³ Voor PL (0% voor bepaalde basisvoeding) en UK ("meeste voedsel") is per product nog niet bekend welk tarief geldt (zie A20).
⁴ 2,1% geldt alleen voor medicijnen op recept; de catalogus bevat vrij verkrijgbare medicijnen, dus standaardtarief (zie A21).
⁵ Hoog tarief in alle landen, behalve CH: laag tarief (R8b).
- **R17 – Btw-specificatie:** De checkout moet ten minste de btw-grondslag, het btw-bedrag en het uiteindelijke eindbedrag tonen.
- **R18 – Berekeningsvolgorde:** De berekening moet de juiste volgorde hanteren voor productbedrag, btw, valuta en afronding.
- **R19 – Auditbaarheid:** Bedragen en gebruikte berekeningsregels moeten reproduceerbaar en controleerbaar zijn tijdens een audit.
- **R20 – Medicijnen:** Producten in de categorie medicijnen mogen geen korting krijgen.
- **R21 – Boeken:** Het systeem moet speciale boekregels voor de drie betreffende landen kunnen ondersteunen zodra deze regels zijn aangeleverd.

### 1.4 Afronding

- **R22 – Afronden op eindtotaal:** Afronding moet uitsluitend op het uiteindelijke ordertotaal plaatsvinden, niet tussentijds per product.
- **R23 – Afronden op centen:** In markten zonder afwijkende regel moet het ordertotaal op centen worden afgerond.
- **R24 – Zwitserse afronding:** Voor de Zwitserse markt moet het uiteindelijke ordertotaal op €0,05 worden afgerond (bijvoorbeeld €12,93 naar €12,95 en €12,92 naar €12,90).
- **R25 – Afrondingsconsistentie:** Dezelfde afrondingsregels moeten consequent worden gebruikt in checkout, orderregistratie en financiële administratie.

### 1.5 Kortingen

- **R26 – Vooraf gedefinieerde kortingen:** Kortingen moeten vooraf in de catalogus kunnen worden geconfigureerd.
- **R27 – Stapelkorting per product:** Stapelkorting moet per product kunnen worden toegepast.
- **R28 – Maximale totale korting:** Het totale kortingspercentage op een bestelling mag niet hoger zijn dan 30%.
- **R29 – Kortingen uitsluiten:** Het systeem moet kortingen kunnen uitschakelen voor producten waarvoor dat wettelijk of beleidsmatig vereist is, waaronder medicijnen.

### 1.6 Markten en prioritering

- **R30 – Meerdere landen:** Het systeem moet verkoop in zes landen ondersteunen: Nederland, Duitsland, Frankrijk, Polen, het Verenigd Koninkrijk en Zwitserland.
- **R31 – Gefaseerde livegang:** Het systeem moet het mogelijk maken om eerst een beperkte selectie markten live te zetten en later andere markten toe te voegen.
- **R32 – Eerste prioriteitsmarkt:** Polen moet als eerste prioriteitsmarkt kunnen worden ingericht.
- **R33 – Andere prioriteitsmarkten:** Duitsland en Frankrijk moeten aansluitend kunnen worden ondersteund.

### 1.7 Financiële administratie

- **R34 – Orderregistratie:** Iedere afgerekende bestelling moet correct worden opgeslagen in de financiële administratie.
- **R35 – Samengestelde orders:** De berekening moet correct blijven voor orders met meerdere producten en grote aantallen van hetzelfde product.
- **R36 – Financiële nauwkeurigheid:** De administratie moet de werkelijk aan de klant berekende bedragen bevatten, inclusief btw, korting, valutaomrekening en afronding.
- **R37 – Bon:** Voor een bestelling moet een bon kunnen worden opgesteld met een bedrag exclusief btw.

## 2. Aannames en open punten

Deze punten zijn niet voldoende eenduidig of volledig bevestigd in de interviews. Ze moeten vóór implementatie worden gevalideerd.

- **A1 – Betaling:** Er is een betaalstap nodig, maar de betaalmethode(n), betaalprovider en foutafhandeling zijn niet gespecificeerd.
- **A2 – ‘Invisible checkout’:** Dit betekent vermoedelijk een zeer korte, afleidingsvrije checkout; de precieze schermen, velden en verplichte klantgegevens zijn onbekend.
- **A3 – Account en gastcheckout:** ~~Er is niet vastgesteld of een klant een account nodig heeft of als gast kan afrekenen.~~ Besloten: alleen gastcheckout, geen account. De klant geeft per bestelling naam en e-mailadres op (zie [slices-checkout.md](./slices-checkout.md)).
- **A4 – Landbepaling:** Deels opgelost met R14a en R14b: het land wordt vanaf het eerste bezoek voorgeselecteerd op basis van de browsertaal en de klant kan het met één klik wijzigen; dat land is het land van het afleveradres. Open: moet het land in plaats daarvan uit het domein komen (bijv. eurocart.de)?
- **A5 – Valutaweergave:** Finance noemt euro als schermvaluta, terwijl Compliance lokale valuta voorschrijft. Compliance is hier als leidend uitgangspunt gebruikt; de definitieve valuta per land moet worden bevestigd.
- **A6 – Wisselkoersen:** Deels opgelost: koersen aangeleverd door Finance (R13a). Open: voor welk kwartaal ze gelden (aangenomen Q4 2026), wie de koersen van volgende kwartalen aanlevert, en of omrekenen vanuit EUR met het exacte quotiënt (1 / 0,23) moet.
- **A7 – Btw-tarieven:** ~~Nog niet aangeleverd.~~ Opgelost: tarieven aangeleverd door Compliance, zie 1.3.1.
- **A8 – Btw-regels:** ~~Weergave incl. of excl. btw.~~ Opgelost voor weergave met R14b: overal incl. btw van het land van de klant, excl. btw alleen op verzoek in de checkout. Nog open: hoe btw precies wordt berekend bij valutaomrekening.
- **A9 – Afrondingsmethode:** ‘Omhoog afronden’ is niet formeel gedefinieerd voor gewone centafronding. De precieze afrondingsfunctie en grensgevallen moeten worden vastgelegd.
- **A10 – Stapelkorting:** De drempel voor stapelkorting (bijvoorbeeld vanaf twee stuks) en het kortingspercentage zijn niet volledig gespecificeerd. Alleen 5% wordt genoemd als voorbeeld.
- **A11 – Kortingsconflict:** Sales noemt mogelijk 50% korting, terwijl Finance maximaal 30% totale korting noemt. De Finance-regel is als harde bovengrens gebruikt totdat de eigenaar van de kortingsregels dit bevestigt.
- **A12 – Boeken:** De speciale regels voor boeken in drie landen ontbreken volledig uit de transcriptie.
- **A13 – Verzendkosten:** ~~Tarieven onbekend.~~ Opgelost: € 5,00 per bestelling exclusief btw (R8a), gratis voor bestellingen met alleen e-books (R9).
- **A14 – Productcategorieën:** Beschikbaarheid is opgelost (hele catalogus overal verkoopbaar) en de koppeling categorie–land–btw staat in 1.3.1. De koppeling tussen categorie en korting moet nog worden aangeleverd.
- **A15 – Omzetprioriteit:** Polen (30%), Duitsland en Frankrijk (elk ongeveer 20%) zijn zakelijke prioriteiten, maar er is geen technische prioritering of deadline per markt vastgelegd.
- **A16 – Auditlogging:** Deels opgelost (slice 03): bestellingen worden opgeslagen zoals afgerekend, met exacte bedragen, gebruikte tarieven en koers, en worden nooit herberekend. Finance heeft een overzicht met controle of de boeken kloppen. Open: versiebeheer van btw-tabellen en bewaartermijn.
- **A17 – Niet-functionele eisen:** Prestatie, beschikbaarheid, beveiliging, privacy, toegankelijkheid en browserondersteuning zijn niet besproken en moeten afzonderlijk worden vastgesteld.
- **A18 – Boeken in Frankrijk:** De aangeleverde lijst noemt geen verlaagd tarief voor boeken in FR, dus het standaardtarief van 20% wordt toegepast. Mogelijk hoort dit bij de ontbrekende speciale boekregels (A12).
- **A19 – E-books:** De lijst noemt alleen "boeken". E-books krijgen voorlopig hetzelfde tarief als boeken.
- **A20 – Voedsel PL/UK:** Welke voedingsproducten onder 0% vallen (PL "bepaalde basisvoeding", UK "meeste voedsel") moet per product worden vastgesteld.
- **A21 – Medicijnen FR:** Niet duidelijk is welke catalogusmedicijnen op recept zijn (2,1%). Voorlopig zijn alle medicijnen vrij verkrijgbaar en vallen ze onder het standaardtarief.
- **A22 – Btw over verzendkosten:** ~~Tarief niet aangeleverd.~~ Opgelost: hoog tarief in alle landen, behalve Zwitserland (laag tarief), zie R8b.
- **A23 – Centverschil in weergave:** Omdat alleen het eindtotaal wordt afgerond (R22), kunnen de afgeronde regeltotalen incl. btw een cent afwijken van het eindtotaal. Te bevestigen door Finance of dit acceptabel is, of dat de prijs incl. btw per stuk bindend moet worden.
- **A24 – Werkdag:** Voor de ingangsdatum van kwartaalkoersen is een werkdag maandag t/m vrijdag, behalve 1 januari (de enige feestdag die alle zes markten delen). Te bevestigen door Finance of andere feestdagen ook meetellen.
- **A25 – Bewaren klantgegevens:** Bestellingen bevatten e-mail en adres in dezelfde database als de bedragen. Bewaartermijn en privacy zijn niet besproken (zie A17).

## 3. Tegenstrijdigheden die besluitvorming vereisen

1. **Valuta:** Finance noemt euro op het klantenscherm; Compliance verlangt lokale valuta per markt.
2. **Kortingslimiet:** Finance noemt maximaal 30% totale korting; Sales noemt 50% korting als wenselijk.
3. **Btw-presentatie:** ~~De catalogus is exclusief btw, maar de gewenste presentatie vóór toevoeging aan het winkelmandje is niet volledig eenduidig.~~ Besloten (slice 02): de klant ziet alle prijzen inclusief btw van zijn land; de catalogus blijft exclusief btw opgeslagen (R10, R14b).
4. **Marktvolgorde:** PO wil vooral zo snel mogelijk een werkend product; Sales prioriteert Polen, daarna Duitsland en Frankrijk. Dit moet als releasebesluit worden vastgelegd.
