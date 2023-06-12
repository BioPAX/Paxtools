package org.biopax.paxtools.normalizer;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class Namespace {
  String prefix; // The prefix for this resource

  /*
    A generalization of the concept of the "namespace embedded in local unique identifier".
    Many OBO foundry ontologies use the redundant uppercase name of the ontology in the local identifier,
    such as the GO, which makes the prefixes have a redundant usage as in ``GO:GO:1234567``.
    The `banana` tag explicitly annotates the part in the local identifier that should be stripped, if found.
   */
  String banana;

  String banana_peel; // Delimiter used in banana, e.g. "_" or ":"

//  String preferred_prefix; //preferred spelling/capitalization of the prefix

  String name; // The name of the resource, preferred name

  Set<String> synonyms = Collections.emptySet(); //all the names, prefixes and "synonyms" from this entry and each node listed in the "mappings"

  String pattern; // ID regex (excl. banana+peel...)

  String description;

  boolean deprecated;

  /**
   * A flag denoting if the namespace is embedded in the LUI (if this is true, and it is not accompanied by a banana,
   * assume that the banana is the prefix in all caps plus a colon, as is standard in OBO).
   * Here is an example, remark from bioregistry.io/registry/chebi: MIRIAM Namespace Embedded in LUI
   *   The legacy MIRIAM standard for generating CURIEs with this resource annotates the namespaceEmbeddedInLUI as true.
   *   This means that you may see local unique identifiers that include a redundant prefix and delimiter
   *    (also known as a banana) and therefore look like a CURIE. For Chemical Entities of Biological Interest,
   *    the banana looks like CHEBI:. Therefore, you may see local unique identifiers (LUI) for this resource that look
   *    like CHEBI:24867 (instead of the canonical form 24867) and CURIEs for this resource that look
   *    like chebi:CHEBI:24867 (instead of the canonical form chebi:24867).
   */
  boolean namespace_in_lui;

  /**
   * @return
   * @see #namespace_in_lui
   */
  public String getBanana() {
    return (namespace_in_lui && StringUtils.isEmpty(banana)) ? StringUtils.upperCase(prefix) : banana;
  }

  public String getBanana_peel() {
    if (StringUtils.isNotEmpty(getBanana())) {
      // assume ":" when banana_peel is empty/null
      return StringUtils.isNotEmpty(banana_peel) ? banana_peel : ":";
    } else {
      return ""; //empty banana - empty peel
    }
  }

}
