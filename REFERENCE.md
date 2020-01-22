*Reference Guide JobTech Taxonomy API*

# The Taxonomy Database

The Taxonomy Database contains terms or phrases used at the Swedish
labour market. These are called **concepts** in the database. Every
concept has a unique concept-ID, a preferred label and a type.

Some concepts include extra attributes like definitions and alternative
labels. The concepts are connected in schemas, see the schema headlines
below.

Within schemas the concepts are linked with relationships.

The content of the Taxonomy Database is constantly improved and updated
by the JobTech editorial team. New versions of the database will be
released at regular intervals. However, none of the concepts/terms are
deleted in the Taxonomy Database. If a concept becomes outdated for the
matching purposes, it is tagged with a deprecated flag, but it is still
available in the API from some endpoints.

The Taxonomy Database contains several schemas. Some of these schemas
are multilevel taxonomies with hierarchical relationships between
concepts. Some schemas are merely simple collections of concepts. The
following section will walk you through the schemas and relations within
the database.

## Schema: Occupations

<!---
Chart created in www.lucidchart.com
--->

![alt text](https://github.com/JobtechSwe/jobtech-taxonomy-api/blob/develop/pictures-for-md/occupation%20schema.svg "Diagram for Occupation Schema")


**The occupation taxonomy** is a multilevel collection, based on a
national standard. The content, occupation names, synonyms and other
concepts, are created and updated in cooperation with actors at the
Swedish labour market. The concepts are connected to each other directly
or indirectly.

Occupation schema is structured according to “SSYK, [Svensk standard för
yrkesklassificering](https://www.scb.se/dokumentation/klassifikationer-och-standarder/standard-for-svensk-yrkesklassificering-ssyk/)"
(Swedish Standard Classification of Occupations), which is based on
“ISCO, [International Standard Classification of
Occupation](https://www.ilo.org/public/english/bureau/stat/isco/)”. The
current version used is SSYK-2012.

All the concepts in the SSYK have external-standard codes.

**Please note that the SSYK codes are not to be used as unique ID
numbers for specific concepts since they are not fixed**. **Always use
the Concept ID as identification for specific concepts. This is
guaranteed to not change over time**.

The external standard type at the topmost level in the schema (SSYK-1)
contain nine **major groups of occupations**, like " Yrken med krav på
fördjupad högskolekompetens". These major groups of occupations are
recommended to be used for statistical purposes only.

Another “top level” groups of occupations, **Occupation Field**, is
based on labour market sectors, created to make it easier for job seekers
to find relevant jobs. Occupation Field is not an external standard.

**The connections between SSYK Occupation Groups and Occupation Field is
recommended to be used for statistical purposes only.**

All Occupation names are also connected to at least one Occupation
Field, some of them are connected to two Occupation Fields. **These
connections are recommended to be used for matching purposes.**

The most detailed concept, **Occupation Name** contain terms collected
by the editorial team in co-operation with employers’ organisations,
professional boards and recruiters. In this level you’ll find concepts
like “IT-arkitekt/Lösningsarkitekt”. Occupation names are the “official”
terms for occupations.

Every concept at a lower and more detailed level is connected to one
concept at the parent level, throughout the taxonomy. Example:

<!---
Chart created in www.lucidchart.com
--->

![alt text](https://github.com/JobtechSwe/jobtech-taxonomy-api/blob/develop/pictures-for-md/occupations%20hiearchy.svg "Diagram linked occupation levels")


In the type **Occupation Collections,** you’ll find listings of
Occupation Names grouped by variables that may span over different
occupation areas: “Yrken utan krav på utbildning” and “Chefer,
direktörer och föreståndare”. These collections are created to highlight
a certain group of occupations, not based on SSYK.

The **Keyword** type contains search terms related to Occupation Names.
They can be used to help candidates find job ads they are interested in
even if they don’t know the exact Occupation Name. An example is the
Keyword “Asfaltarbetare”, mapped to the Occupation Name
“Beläggningsarbetare”. Keywords are recommended to be used as “hidden”
terms, connected to one or several official Occupation Names. Keywords
should not be exposed to end users.

## Schema: Skills

The skills taxonomy contains two levels: Skills headlines like
“Databaser” and Skills like “SQL-Base, databashanterare”. Each of the
skill concepts are mapped to a parent skill headline and to one or
several SSYK Occupation groups. The database of skills is created and
updated in co-operation with employers’ organisations, professional
boards and recruiters and includes the most relevant skills for each
four-digit SSYK Occupation Group.

The skill headline named “**General skills**” contains broader skills
like “Projektledning, erfarenhet”. They are not mapped to any Occupation
groups. They are recommended to use as optional skills for all job
seekers and employers.

## Schema: Geographical areas

The database contains a four-level taxonomy of geographical areas. Like
the occupation and skill taxonomy, the concepts are related to each
other in a hierarchical structure.

The top geographic type lists all continents in the world, including
Antarctica. The taxonomy is based on the [<span class="underline">UN
standard for
continents</span>](https://unstats.un.org/unsd/methodology/m49/). In
this type, there is also the concept “Alla länder”, which is a list of
all countries.

The second type in this taxonomy contains all countries,
according to [<span class="underline">ISO standard for
countries</span>](https://www.iso.org/iso-3166-country-codes.html). Each
country in this level has a parent continent in the top level.

The third type is simply called “regions” and contains all regions
within the EU with a “NUTS code” (See [<span
class="underline">Eurostat</span>](https://ec.europa.eu/eurostat/web/nuts/background) for
information about NUTS). In Sweden the regions correspond to “län”.
Every region is mapped to a specific parent country in the second level
in the taxonomy.

The fourth type of the geographic areas contains the Swedish
municipalities. Each municipality is mapped to a specific parent region
in the above level. 

**Geographical areas are recommended to use when a
vacancy is abroad or when a job seeker looks for a job abroad.**

## Schema: Wage type

This schema only has one type. This type contains descriptions of forms
of payment, like “Rörlig ackords- eller provisionslön”.

## Schema: Employment type

This schema only contains one type. It lists types of employment, like
“Säsongsanställning” och “Behovsanställning/Timanställning”.

## Schema: Driving license

This single type schema contains driving license categories in Sweden,
according to [<span class="underline">EU
standard</span>](https://europa.eu/youreurope/citizens/vehicles/driving-licence/driving-licence-recognition-validity/index_en.htm),
and the description and limitation of each license.

“Körkortskombinationer”: All but the “lowest” ranked license also
contain a list of the licenses that are implicit within that level. The
A2 license for example has the Implicit license attribute listing AM and
A1. These are lower level licenses for scooters that you are
automatically allowed to drive if you carry the A2 license.

## Schema: Worktime extent

This schema only contains the two concepts “Heltid” and “Deltid”.

## Schema: SUN

“Svensk utbildningsklassifkation” SUN is used for classifying education.
SUN provides the conditions for producing comparable statistics and
analysis of population, education and the Swedish education system, both
nationally and internationally. SUN consists of two classifications: one
describing education *level* and another describing education
*orientation*.

## Schema: SNI

*This schema will soon be updated.*

“Svensk näringsgrensindelning SNI” contains terms for industries. This
taxonomy follows the [<span class="underline">SCB
documentation</span>](https://www.scb.se/contentassets/d43b798da37140999abf883e206d0545/mis-2007-2.pdf) and
has two levels.

The SNI-level-1 contains general area term of industries. An example is
the concept “Tillverkning”.

The second level, SNI-level-2, lists the industries in more detail. It
has concepts like “Livsmedelsframställning”. Every concept in this level
has a parent concept in the first level.

## Schema: Languages

The language taxonomy lists natural languages like “Engelska” and
“Nederländska”. The language taxonomy is based on [<span
class="underline">ISO
standard</span>](https://www.iso.org/iso-639-language-codes.html) and
it’s recommended to highlight which languages are requested for a
vacancy and the languages a job seeker is able to work with.

## Schema: Language levels

*This schema will soon be updated.*

## Schema: Employment duration

The employment duration taxonomy contains concepts describing how long
an employment is meant to last. The schema contains concepts like “3
månader – upp till 6 månader”.

## Relations

The concepts in the Taxonomy database may be related to each other in a
number of ways. The different types of relations are in part based on
[<span class="underline">this SKOS standard</span>](https://www.w3.org/TR/skos-reference/\#L1170)

The relations can be either vertical (describing a hierarchy) or
horizontal.

### Narrower

This relation is vertical and is used to express when one concept is on
a lower level than another in a hierarchy. For example: the occupation
“Beläggningsarbetare” is narrower than the occupation group
“Anläggningsarbetare”.

### Broader

This relation is vertical and is used to express when one concept is on
a higher level than another in a hierarchy. For example: the occupation
group “Anläggningsarbetare” is broader than the occupation
“Beläggningsarbetare”.

### Substitutability

This relation is horizontal and describes the closeness of two
occupations. The relation can be expressed as both *high* (75) and *low*
(25) substitutability between occupations. For example: the occupation
“Beläggningsarbetare” has a high substitutability with the occupation
“Väg- och anläggningsarbetare”. In the API the objects in the
substitutability relations are expressed as a source occupation and a
target occupation. In the example above the occupation
“Beläggningsarbetare” would be the source.

The two levels can be described as following:

-   High (or 75%): very closely related with a high level of similarity
    in tasks

-   Low (or 25%): some tasks are similar and/or some education or
    training might be needed to traverse the gap

The substitutability relation may be asymmetrical, meaning that a high
substitutability from one occupation to another does not necessarily
mean that the reverse is true. For instance in the example above, the
reversed substitutability (from Väg- och underhållningsarbetare to
Beläggningsarbetare) is in fact low.

The substitutability relations are created and recommended for employers
looking for candidates. If they cannot find exactly what they are
looking for, they get suggestions that may work out for them instead.
For example: an employer is looking for a candidate for “Förskollärare”
but cannot find one. Instead they get suggestions for “Barnskötare”
through the substitutability relation. In this case the substitutability
from “Förskollärare” to “Barnskötare” is low.
