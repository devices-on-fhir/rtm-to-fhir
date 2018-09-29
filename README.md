# MDC Code System

These definitions and tools are provided for creating a FHIR code system for the ISO/IEEE 11073-10101 Nomenclature standard.

## Using MDC with FHIR
Characteristics of the MDC code system are described in a documentation page. There is a [draft version](https://docs.google.com/document/d/1Msh8z6sNCtI2koxf1kpObkmxNcVd6mS_6bTr6JDZOd0/edit?usp=sharing) in Google Docs open for comments. The final version should go into the Terminologies section of the FHIR specification.

## CodeSystem definitions
For MDC there is a [CodeSystem](http://hl7.org/fhir/codesystem.html) resource that publishes identification, description, and concept properties. Two supplemental code systems are used for coding the concepts.

| CodeSystem/MDC | ISO/IEEE 11073-10101 Nomenclature (without concept definitions) |
| CodeSystem/MDC-designation-use | Designation use within the ISO/IEEE 11073-10101 Nomenclature |
| CodeSystem/MDC-concept-status | Concept status within the ISO/IEEE 11073-10101 Nomenclature |

## ValueSet definitions
There are example [ValueSet](http://hl7.org/fhir/valueset.html) resources that provide all or a subset of concepts of the MDC code system.

| ValueSet/MDC | ISO/IEEE 11073-10101 Nomenclature (all codes) |
| ValueSet/MDC-partition | ISO/IEEE 11073-10101 Partition codes |
| ValueSet/MDC-object | ISO/IEEE 11073-10101 Nomenclature filtered by Object partition |
| ValueSet/MDC-metric | ISO/IEEE 11073-10101 Nomenclature filtered by Metric partition |
| ValueSet/MDC-dimension | ISO/IEEE 11073-10101 Nomenclature filtered by Dimension partition |
