
# Taxonomy-databasen # 

Taxonomy-databasen innehåller begrepp som används på svenska arbetsmarknaden. Dessa begrepp kallas för **concepts**. Varje concept har en unik ID (concept-ID), en preferred label samt en type. 

I några av concepts ingår attribut som definitioner och alternativa labels. Concepts är kopplade till schemas, se rubrikerna till dem nedan.
Inom schemas är concepts kopplade till relationer.

Redaktionen i JobTech kvalitetssäkrar, utvecklar och uppdateras innehållet i Taxonomy-databasen kontinuerligt. Nya versioner av databasen publiceras regelbundet. Inget av concepts/terms raderas dock från Taxonomy-databasen. Om ett concept blir inaktuellt I matchningssammanhang, taggas den deprecated, men det är fortfarande tillgängligt i API:et för vissa endpoints. 

Taxonomy-databasen innehåller flera schemas. Några av dem är taxonomier på flera nivåer och innehåller hierarkiska relationer mellan concepts. Ett antal av schemas är samlingar av begrepp (concepts). Följande avsnitt ger dig en överblick över schemas.

## Schema: Occupations (yrkesbenämningar)

<!---
Chart created in www.lucidchart.com
--->

![alt text](https://github.com/JobtechSwe/jobtech-taxonomy-api/blob/develop/pictures-for-md/occupation%20schema.svg "Diagram for Occupation Schema")

Taxonomin på yrken (**The occupation taxonomy**) finns på flera nivåer och är baserad på en nationell standard. Innehållet (yrkesbenämningar, synonymer och andra concepts) är skapade och uppdateras kontinuerligt i samråd med aktörerna på arbetsmarknaden. Concepts är direkt eller indirekt kopplade till varandra. 

Yrkes-schema har strukturerats enligt “SSYK, Svensk standard för yrkesklassificering” som bygger på en internationell yrkesklassificering ”ISCO, International Standard Classification of Occupation”. Den nuvarande versionen är SSYK-2012. 

Samtliga concepts I SSYK har externa standardkoder. 
**Observera att SSYK-koderna inte ska användas som unika ID-nummer för specifika concepts eftersom de kan ändras över tid. Använd alltid Concept ID som identifiering för specifika concepts.** 

Den externa standarden på mest övergripande nivån (SSYK-1) innehåller nio **major groups of occupations**, exempelvis "Yrken med krav på fördjupad högskolekompetens". Den här nivån rekommenderas att användas endast för statistiska ändamål.

En annan övergripande nivå på yrken, **Occupation Field**, bygger på sektorer på arbetsmarknaden. De är skapade för att underlätta för arbetssökande att hitta relevanta lediga jobb. Occupation Field är inte en extern standard utan en struktur som är skapad hos Arbetsförmedlingen. 

**Kopplingen mellan SSYK-yrkesgrupper (Occupation Groups) och Occupation Field rekommenderas att användas endast för statiska ändamål.**

Samtliga yrkesbenämningar (Occupation names) är också kopplade direkt till åtminstone ett Occupation Field, några av dem är kopplade till två Occupation Fields. **De här kopplingarna rekommenderas att användas för matchning mellan lediga jobb och arbetssökande.** 

Den mest detaljerade nivån av concepts, **Occupation Name** (yrkesbenämningar) består av begrepp som Redaktionen i JobTech har skapat i samarbete med arbetsgivar- och branschorganisationer, yrkesnämnder och rekryterare, exempelvis “IT-arkitekt/Lösningsarkitekt”. Occupation names innehåller ”officiella” lydelser för yrken.  

Varje concept på lägre nivå är kopplat till högre nivå (parent level). 
Exempel:


<!---
Chart created in www.lucidchart.com
--->

![alt text](https://github.com/JobtechSwe/jobtech-taxonomy-api/blob/develop/pictures-for-md/occupations%20hiearchy.svg "Diagram linked occupation levels")


**Occupation Collections** (yrkessamlingar) innehåller Occupation Names (yrkesbenämningar) som är grupperade enligt ett tema: “Yrken utan krav på utbildning” and “Chefer, direktörer och föreståndare”. Yrkessamlingarna är skapade för att synliggöra grupperingar som inte är baserade på yrkesklassificeringen SSYK. 

**Keywords** består av begrepp som är relaterade till Occupation Names. De är tänkta att hjälpa de som söker jobb att hitta relevanta annonser även om de inte känner till den ”officiella” lydelsen av en yrkesbenämningen. Ett exempel på Keyword är “Asfaltarbetare”, som är kopplad till Occupation Name “Beläggningsarbetare”. **Keywords rekommenderas att användas som “dolda” begrepp som är kopplade till en eller flera ”officiella” Occupation Names. Keywords ska inte exponeras för slutanvändare.**


## Schema: Skills (kompetensord)

**The skills taxonomy** (taxonomin för kompetensord) innehåller två nivåer: Skills headlines (kompetensrubriker) som “Databaser” och Skills (kompetensord) som “SQL-Base, databashanterare”. Varje skill är kopplad till en skill headline samt till en eller flera SSYK Occupation groups. I likhet med Occupation names har Redaktionen i JobTech har skapat taxonomin för skills i samarbete med arbetsgivar- och branschorganisationer, yrkesnämnder och rekryterare. Den innehåller de mest relevanta kompetenser för varje SSYK Occupation Group. 

Kompetensrubriken **“General skills”** innehåller ett antal kompetenser som “Projektledning, erfarenhet” och ”Arbetsledarerfarenhet”. De är inte kopplade till SSYK Occupation groups. **De rekommenderas att användas som valfria kompetenser för alla arbetsgivare och arbetssökande.**

## Schema: Geographical areas (geografiska områden)

Databasen för geografiska områden innehåller taxonomier på fyra nivåer. De är relaterade till varandra i en hierarkisk struktur, i likhet med taxonomin för yrken och kompetenser.

Den mest övergripande taxonomin för geografiska områden innehåller världsdelar och är en [<span class="underline">FN-standard</span>](https://unstats.un.org/unsd/methodology/m49/). I den taxonomin ser man också “Alla länder”.

Nästa nivå i det här schemat innehåller samtliga länder enligt ISO standarden [<span class="underline"> ISO-standard for
countries</span>](https://www.iso.org/iso-3166-country-codes.html). Varje land har en koppling till en världsdel.

Den tredje nivån “regions” innehåller samtliga EU-regioner och tillhörande NUTS-koder. Mera information om NUTS-koderna finns på [<span class="underline">Eurostat</span>](https://ec.europa.eu/eurostat/web/nuts/background). I Sverige motsvarar EU-regionerna “län”. Varje region har en koppling till ett land.

Den fjärde nivån i geografiska områden består av svenska kommuner. Varje kommun har en koppling till en region (län). 

**Geografiska områden rekommenderas att användas när det lediga arbetet finns utanför Sverige alternativt när en person söker arbete utomlands.**

## Schema: Wage type (löneform)

Löneformer innehåller en taxonomi med beskrivning av löneformer som “Rörlig ackords- eller provisionslön”.

## Schema: Employment type (anställningsform)

Anställningsform är en taxonomi med beskrivning som “Säsongsanställning” och “Behovsanställning/Timanställning”.

## Schema: Driving license (körkort)

Taxonomin består av körkortsklasser som gäller I Sverige enligt en [<span class="underline">EU
standard</span>](https://europa.eu/youreurope/citizens/vehicles/driving-licence/driving-licence-recognition-validity/index_en.htm) samt en beskrivning av varje körkortsklass.

“Körkortskombinationer”: Innehåller en taxonomi över vilka fordon som man får köra med olika körkortsklasser. Exempelvis med körkortsklassen A2 får man köra fordon som kräver körkort i klass AM och A1. 

## Schema: Worktime extent (arbetstid)

Taxonomin för arbetstid består av två concepts “Heltid” and “Deltid”.

## Schema: SUN (Svensk utbildningsklassifkation)
“Svensk utbildningsklassifkation SUN” används till att klassificera utbildningar. 
SUN ger förutsättningarna för att producera jämförbar statistik och analys av befolkning, utbildning och det svenska utbildningssystemet, både nationellt och internationellt. SUN är uppbyggd av 2 moduler; en nivåmodul och en inriktningsmodul.

## Schema: SNI (Svensk näringsgrensindelning)

*Detta schema ska uppdateras inom kort.*

”Svensk näringsgrensindelning SNI” används för att klassificera företag och arbetsställen efter vilken verksamhet de bedriver. Denna taxonomi följer [<span class="underline">SCB:s dokumentation</span>](https://www.scb.se/contentassets/d43b798da37140999abf883e206d0545/mis-2007-2.pdf). 

## Schema: Languages (språk)

Taxonomin för språk innehåller naturliga språk som “Engelska” and “Nederländska” och är baserad på en [<span
class="underline">ISO standard</span>](https://www.iso.org/iso-639-language-codes.html). Den rekommenderas att användas för att beskriva de efterfrågade språkkunskaperna i ett ledigt arbete alternativt för att beskriva vilka språk som den arbetssökande behärskar i professionella sammanhang. 

## Schema: Language levels (kunskapsnivåer i språk)

*Detta schema ska uppdateras inom kort.*

## Schema: Employment duration (anställningsvaraktighet)

Anställningsvaraktighet innehåller begrepp som beskriver hur länge en anställning beräknas att vara. Den innehåller concepts som “3 månader – upp till 6 månader”.

## Relationer

Concepts i JobTech Taxonomy-databasen kan vara relaterade till varandra på flera sätt.
Relationerna är delvis baserade på en [<span class="underline">SKOS-standard</span>](https://www.w3.org/TR/skos-reference/#L1170) .  Relationerna kan vara antingen vertikala (beskriver en hierarki) eller horisontella.

### Narrower (smalare)

Den här relationen är vertikal och används för att uttrycka när ett koncept är på en lägre nivå än ett annat i en hierarki. Till exempel: Occupation name (yrkesbenämningen) ”Beläggningsarbetare” är smalare än Occupation group (yrkesgruppen) ”Anläggningsarbetare”.

### Broader (bredare)

Den här relationen är vertikal och används för att uttrycka när ett koncept är på en högre nivå än ett annat i en hierarki. Till exempel: Occupation Group (yrkesgruppen) ”Anläggningsarbetare” är bredare än Occupation name (yrkesbenämningen) ”Beläggningsarbetare”.

### Substitutability (benämningssläktskap)

Den här relationen är horisontell och beskriver närhet mellan två Occupation Names (yrkesbenämningar). Förhållandet kan uttryckas som antingen hög (75) och låg (25) benämningssläktskap mellan yrken. Till exempel: Occupation Name (yrkesbenämningen) ”Beläggningsarbetare” har en hög benämningssläktskap med yrkesbenämningen ”Väg- och anläggningsarbetare”. I API uttrycks yrkesbenämningarna i benämningssläktskapen som demanded och offered. I exemplet ovan är yrkesbenämningen ”Beläggningsarbetare” demanded.
De två nivåerna för benämningssläktskapen är:

-	Hög (eller 75%): mycket nära besläktad med stor likhet i arbetsuppgifterna

-	Låg (eller 25%): några av arbetsuppgifterna är liknande och/eller någon form av utbildningsinsats kan behövas för att man ska fungera i yrkesrollen

Benämningssläktskapet kan vara asymmetrisk, vilket innebär att en hög nivå av släktskap från en yrkesbenämning till en annan inte nödvändigtvis är densamma i motsatt relation. I exemplet ovan är den omvända nivån av släktskap (från Väg- och underhållningsarbetare till Beläggningsarbetare) låg.

Benämningssläktskap mellan yrken skapas och rekommenderas för arbetsgivare som söker kandidater till ett ledigt arbete. Om de inte kan hitta exakt vad de letar efter, får de förslag som kan fungera i stället. Till exempel: en arbetsgivare söker en ”Förskollärare” men kan inte hitta en. I stället får arbetsgivaren förslag på ”Barnskötare” genom benämningssläktskap. I detta fall är släktskapet mellan "Förskollärare" (demanded) till "Barnskötare" (offered) låg.
