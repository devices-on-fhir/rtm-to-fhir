import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.varia.NullAppender;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.uhn.fhir.context.FhirContext;

public class RTMtoFHIR {

  // Concept map
  private static Map<String, ConceptDefinitionComponent> concepts;
  private static Date lastUpdate;

  public static void main(String[] args) {

    org.apache.log4j.BasicConfigurator.configure(new NullAppender());

    try {
      // Get concepts from RTMMS
      concepts = new TreeMap<String, ConceptDefinitionComponent>();
      lastUpdate = new Date(0);
      if (args.length > 0) {
        GetFromRTM(new FileReader(args[0]));
      } else {
        URL url = new URL("https://rtmms.nist.gov/rtmms/getTermsJson.do");
        GetFromRTM(new InputStreamReader(url.openStream()));
      }

      // Create CodeSystem resource
      CreateCodeSystem();

    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public static void GetFromRTM(Reader reader) throws Exception {

    // Concept definition
    ConceptDefinitionComponent concept;

    // Need a parser
    JsonParser parser = new JsonParser();
    JsonArray array = parser.parse(reader).getAsJsonArray();

    // Initialize counters
    int entries = 0;
    int codes = 0;
    int synonyms = 0;

    // Expect JSON array where each entry describes a concept. Entries without code (CF_CODE10) are
    // skipped.
    for (JsonElement entry : array) {
      if (entry.isJsonObject()) {
        JsonObject object = entry.getAsJsonObject();
        if (object.has("cfcode10")) {
          JsonElement element = object.get("cfcode10");
          if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
            concept = new ConceptDefinitionComponent();
            concept.setCode(element.getAsString());

            // Set display from reference identifier (REFID)
            if (object.has("referenceId")) {
              element = object.get("referenceId");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                concept.setDisplay(element.getAsString());
              }
            }

            // Set definition from term description
            if (object.has("termDescription")) {
              element = object.get("termDescription");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                concept.setDefinition(element.getAsString());
              }
            }

            // Set designations
            List<ConceptDefinitionDesignationComponent> designation =
                new Vector<ConceptDefinitionDesignationComponent>();
            if (object.has("systematicName")) {
              element = object.get("systematicName");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                designation.add(
                    new ConceptDefinitionDesignationComponent()
                        .setUse(
                            new Coding()
                                .setSystem("http://devices.fhir.org/CodeSystem/MDC-designation-use")
                                .setCode("systematic-name"))
                        .setValue(element.getAsString()));
              }
            }
            if (object.has("commonTerm")) {
              element = object.get("commonTerm");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                designation.add(
                    new ConceptDefinitionDesignationComponent()
                        .setUse(
                            new Coding()
                                .setSystem("http://devices.fhir.org/CodeSystem/MDC-designation-use")
                                .setCode("common-term"))
                        .setValue(element.getAsString()));
              }
            }
            if (object.has("acronym")) {
              element = object.get("acronym");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                designation.add(
                    new ConceptDefinitionDesignationComponent()
                        .setUse(
                            new Coding()
                                .setSystem("http://devices.fhir.org/CodeSystem/MDC-designation-use")
                                .setCode("acronym"))
                        .setValue(element.getAsString()));
              }
            }

            // Set status property
            if (object.has("status")) {
              element = object.get("status");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                String status = "unknown";
                if (element.getAsString().compareTo("PROPOSED") == 0) {
                  status = "proposed"; // ### to be defined
                } else if (element.getAsString().compareTo("APPROVED") == 0) {
                  status = "approved"; // ### to be defined
                }
                concept.addProperty(
                    new CodeSystem.ConceptPropertyComponent()
                        .setCode("status")
                        .setValue(
                            new Coding()
                                .setSystem("http://devices.fhir.org/CodeSystem/MDC-concept-status")
                                .setCode(status)));
              }
            }

            // Set harmonized property
            if (object.has("sources")) {
              element = object.get("sources");
              if (element.isJsonArray()) {
                Boolean harmonized = false;
                for (JsonElement tag : element.getAsJsonArray()) {
                  if (tag.isJsonPrimitive() && (tag.getAsString().compareTo("HRTM") == 0)) {
                    harmonized = true;
                  }
                }
                concept.addProperty(
                    new CodeSystem.ConceptPropertyComponent()
                        .setCode("harmonized")
                        .setValue(new BooleanType(harmonized)));
              }
            }

            // Set partition property
            if (object.has("partition")) {
              element = object.get("partition");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                concept.addProperty(
                    new CodeSystem.ConceptPropertyComponent()
                        .setCode("partition")
                        .setValue(new StringType(element.getAsString())));
              }
            }

            // Check for synonym entry
            ConceptDefinitionComponent synonym = concepts.get(concept.getCode());
            if (synonym != null) {
              if (true) { // ### concept is preferred
                designation.add(
                    0,
                    new ConceptDefinitionDesignationComponent()
                        .setUse(
                            new Coding()
                                .setSystem("http://devices.fhir.org/CodeSystem/MDC-designation-use")
                                .setCode("synonym"))
                        .setValue(synonym.getDisplay()));
                concept.setDesignation(designation);
                concepts.put(concept.getCode(), concept);
              } else { // ### synonym is preferred
                designation = synonym.getDesignation();
                designation.add(
                    0,
                    new ConceptDefinitionDesignationComponent()
                        .setUse(
                            new Coding()
                                .setSystem("http://devices.fhir.org/CodeSystem/MDC-designation-use")
                                .setCode("synonym"))
                        .setValue(concept.getDisplay()));
                synonym.setDesignation(designation);
                concepts.put(synonym.getCode(), synonym);
              }
              synonyms++;
            } else {
              concept.setDesignation(designation);
              concepts.put(concept.getCode(), concept);
            }

            // Determine date of last update
            if (object.has("updateDate")) {
              element = object.get("updateDate");
              if (element.isJsonPrimitive() && !element.getAsString().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
                Date update = dateFormat.parse(element.getAsString());
                if (update.after(lastUpdate)) {
                  lastUpdate = update;
                }
              }
            }
            codes++;
          }
        }
        entries++;
      }
    }

    // Show statistics
    System.out.println("Entries processed: " + entries);
    System.out.println("Codes found: " + codes);
    System.out.println("Synonym terms: " + synonyms);
  }

  public static void CreateCodeSystem() throws Exception {

    // Need a context
    FhirContext ctx = FhirContext.forR4();

    // Start with empty CodeSystem resource
    CodeSystem codeSystem = new CodeSystem();

    // Add static contents
    codeSystem.setUrl("urn:iso:std:iso:11073:10101");
    codeSystem.setIdentifier(
        new Identifier().setSystem("urn:ietf:rfc:3986").setValue("urn:oid:2.16.840.1.113883.6.24"));
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    codeSystem.setVersion(dateFormat.format(lastUpdate));
    codeSystem.setName("MDC");
    codeSystem.setTitle("MDC Nomenclature");
    codeSystem.setStatus(PublicationStatus.DRAFT);
    codeSystem.setExperimental(false);
    codeSystem.setDate(new Date());
    codeSystem.setPublisher("Health Level Seven International (Health Care Devices Work Group)");
    codeSystem.setDescription(
        "ISO/IEEE 11073-10101 Nomenclature for point-of-care medical device communication.");
    codeSystem.setCaseSensitive(false);
    codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
    codeSystem.addProperty(
        new CodeSystem.PropertyComponent()
            .setCode("status")
            .setDescription("Status of the term code in NIST RTMMS.")
            .setType(CodeSystem.PropertyType.CODING));
    codeSystem.addProperty(
        new CodeSystem.PropertyComponent()
            .setCode("harmonized")
            .setDescription(
                "Indicates whether the term code has been agreed upon during the open consensus-based Rosetta harmonization process.")
            .setType(CodeSystem.PropertyType.BOOLEAN));
    codeSystem.addProperty(
        new CodeSystem.PropertyComponent()
            .setCode("partition")
            .setDescription(
                "Partition is a group of semantics that are assigned to a contiguous term code range and have a categorical relationship.")
            .setType(CodeSystem.PropertyType.CODE));

    // Add concepts
    for (Iterator<Map.Entry<String, ConceptDefinitionComponent>> entries =
            concepts.entrySet().iterator();
        entries.hasNext(); ) {
      Map.Entry<String, ConceptDefinitionComponent> entry = entries.next();
      codeSystem.addConcept(entry.getValue());
    }

    // Write resource to file
    Writer output =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream("CodeSystem-MDC.xml"), "utf-8"));
    ctx.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(codeSystem, output);
    output =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream("CodeSystem-MDC.json"), "utf-8"));
    ctx.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(codeSystem, output);
  }
}
