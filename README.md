# MDC Code System

These definitions are provided for creating a FHIR code system for the ISO/IEEE 11073-10101 Medical Device Communication Nomenclature standard.

## Using MDC with FHIR
Characteristics of the MDC code system are described in a [documentation page](http://hl7.org/fhir/mdc.html) in the Terminologies section of FHIR specification.

## CodeSystem definitions
For MDC there is a [CodeSystem](http://hl7.org/fhir/codesystem.html) resource that publishes identification, description, and concept properties. Two supplemental code systems are used for coding the concepts.

[CodeSystem/MDC](CodeSystem/CodeSystem-MDC.xml) ISO/IEEE 11073-10101 Nomenclature (without concept definitions)  
[CodeSystem/MDC-designation-use](CodeSystem/CodeSystem-MDC-designation-use.xml) Designation use within the ISO/IEEE 11073-10101 Nomenclature  
[CodeSystem/MDC-concept-status](CodeSystem/CodeSystem-MDC-concept-status.xml) Concept status within the ISO/IEEE 11073-10101 Nomenclature  

## ValueSet definitions
There are example [ValueSet](http://hl7.org/fhir/valueset.html) resources that provide all or a subset of concepts of the MDC code system.

[ValueSet/MDC](ValueSet/ValueSet-MDC.xml) ISO/IEEE 11073-10101 Nomenclature (all codes)  
[ValueSet/MDC-partition](ValueSet/ValueSet-MDC-partition.xml) ISO/IEEE 11073-10101 Partition codes  
[ValueSet/MDC-object](ValueSet/ValueSet-MDC-object.xml) ISO/IEEE 11073-10101 Nomenclature filtered by Object partition  
[ValueSet/MDC-metric](ValueSet/ValueSet-MDC-metric.xml) ISO/IEEE 11073-10101 Nomenclature filtered by Metric (SCADA or Settings) partition  
[ValueSet/MDC-event](ValueSet/ValueSet-MDC-event.xml) ISO/IEEE 11073-10101 Nomenclature filtered by Event partition  
[ValueSet/MDC-dimension](ValueSet/ValueSet-MDC-dimension.xml) ISO/IEEE 11073-10101 Nomenclature filtered by Dimension partition  
