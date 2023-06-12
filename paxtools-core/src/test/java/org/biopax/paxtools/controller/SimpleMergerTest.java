package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * @author rodche
 */
public class SimpleMergerTest {

  @Test
  public final void testMergeModel() {
    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    Model model = factory.createModel();
    Xref ref = model.addNew(UnificationXref.class, "Xref1");
    ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    ref.setId("Q0VCL1");
    Xref uniprotX = ref;
    ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference");
    pr.setDisplayName("ProteinReference");
    pr.addXref(uniprotX);
    ref = model.addNew(RelationshipXref.class, "Xref2");
    ref.setDb("refseq");
    ref.setId("NP_001734");
    pr.addXref(ref);
    // normalizer won't merge diff. types of xref with the same db:id
    ref = model.addNew(PublicationXref.class, "Xref3");
    ref.setDb("pubmed");
    ref.setId("2549346"); // the same id
    pr.addXref(ref);
    ref = model.addNew(RelationshipXref.class, "Xref4");
    ref.setDb("pubmed");
    ref.setId("2549346"); // the same id
    pr.addXref(ref);

    // create the following, add as value to corr. properties,
    // but not add to the model explicitely (merger should find and add them!)
    BioSource bs = factory.create(BioSource.class, "Mouse");
    ref = factory.create(UnificationXref.class, "Xref5");
    ref.setDb("taxonomy");
    ref.setId("10090"); // the same id
    bs.addXref(ref);
    pr.setOrganism(bs);

    assertEquals(5, model.getObjects().size());

    // do merge
    SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
    merger.merge(model, model); // to itself
    // - can use model.merge(model) or model.repair() instead
		
/*		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			(new SimpleIOHandler(BioPAXLevel.L3)).convertToOWL(model, out);
			System.out.println(out.toString());
		} catch (Exception e) {
			fail(e.toString());
		}*/

    bs = (BioSource) model.getByID("Mouse");
    assertEquals(1, bs.getXref().size());
    pr = (ProteinReference) model.getByID("ProteinReference");
    assertEquals(4, pr.getXref().size());
    assertEquals(7, model.getObjects().size()); // Bug fixed: SimpleMerger adds BioSource to the model but not its children (xref)!
  }

  @Test
  public final void testMergeObject() {
    SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    Model model = factory.createModel();

    Xref ref = factory.create(UnificationXref.class, "9606");
    ref.setDb("Taxonomy");
    ref.setId("9606");
    BioSource bs = factory.create(BioSource.class, "Human");
    bs.addXref(ref);

    merger.merge(model, ref);
    assertEquals(1, model.getObjects().size());
    //merge the other object, having child xref (same as already merged)
    merger.merge(model, bs);
    assertEquals(2, model.getObjects().size());

    //merge into an empty target
    model = factory.createModel();
    merger.merge(model, bs);
    assertEquals(2, model.getObjects().size()); //has bs, ref
  }

  @Test
  public final void testMergeToInconsistentModel() {
    // Let's merge an object having one xref into a model that
    // implicitly "contains" a different xref with the same URI;
    // merged model will explicitly contain only the xref
    // of the just merged object (not the original, implicit one;
    // and therefore the model is still not self-integral;
    // let's begin...

    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    Model model = factory.createModel(); //target model

    //create an xref outside the model,
    Xref mref = factory.create(UnificationXref.class, "10090");
    mref.setDb("Taxonomy");
    mref.setId("10090");
    //and a BioSource - in the model
    BioSource mouse = model.addNew(BioSource.class, "mouse");
    mouse.addXref(mref);
    //mref is owned by the mouse but is not in the model explicitly
    assertEquals(1, model.getObjects().size());

    //Create two more objects outside the model, one owns the other -
    Xref href = factory.create(UnificationXref.class, "9606");
    href.setDb("Taxonomy");
    href.setId("9606");
    BioSource human = factory.create(BioSource.class, "human");
    human.addXref(href);

    //do merge
    SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
    merger.merge(model, human);

    assertEquals(3, model.getObjects().size()); //there's no mref - Xref 10090!
    assertNull(model.getByID("10090"));


    // now, if both xrefs had the same URI, such as 'ref',
    // then 'human' would become pointing to the mouse taxID,
    // or vice versa, depending on whether the initial model
    // was self-integral or not.

    model = factory.createModel(); //fresh target model

    //create an xref outside the model,
    mref = factory.create(UnificationXref.class, "ref");
    mref.setDb("Taxonomy");
    mref.setId("10090");
    mouse = model.addNew(BioSource.class, "mouse");
    mouse.addXref(mref);

    //Create two more objects outside the model, one owns the other -
    href = factory.create(UnificationXref.class, "ref"); //on purpose - same URI, different xref!
    href.setDb("Taxonomy");
    href.setId("9606");
    human = factory.create(BioSource.class, "human");
    human.addXref(href);

    merger.merge(model, human);

    assertEquals(3, model.getObjects().size()); // - there's no Xref 10090
    Xref x = (UnificationXref) model.getByID("ref");
    assertNotNull(x);
    assertEquals("9606", x.getId());
    BioSource bs = (BioSource) model.getByID("mouse");
    assertNotNull(bs);
    assertEquals("10090", bs.getXref().iterator().next().getId()); //but that xref is not in the model
    assertEquals("9606", human.getXref().iterator().next().getId());

    //But.
    //If the initial target model contained both the mouse and mref,
    model = factory.createModel(); //fresh target model
    mref = model.addNew(UnificationXref.class, "ref");
    mref.setDb("Taxonomy");
    mref.setId("10090");
    mouse = model.addNew(BioSource.class, "mouse");
    mouse.addXref(mref);
    //then
    merger.merge(model, human);
    //makes the human have mouse xref -
    assertEquals(3, model.getObjects().size());
    x = (UnificationXref) model.getByID("ref");
    assertNotNull(x);
    assertEquals("10090", x.getId()); //aha
    bs = (BioSource) model.getByID("mouse");
    assertNotNull(bs);
    assertEquals("10090", bs.getXref().iterator().next().getId()); //ok
    assertNotNull(model.getByID("human"));
    assertEquals("10090", human.getXref().iterator().next().getId());
    //- 'human' (as well as 'mouse') BioSource now points to mouse's taxID xref!

    //PS:
    //in order to make a model self-integral, one can try merging it to itself,
    //which make all implicit objects become explicit members of the model, or fails,
    //if there are different objects using same URIs somewhere...
  }

  @Test
  public final void testMergeWithoutFilter() {
    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    Model model = factory.createModel();
    //create a PR, BS, etc., in the (target) model
    BioSource human = model.addNew(BioSource.class, "human");
    human.setStandardName("Homo sapiens"); //no displayName, no other names
    UnificationXref uxHuman = model.addNew(UnificationXref.class, "9606");
    human.addXref(uxHuman);
    ProteinReference pr = model.addNew(ProteinReference.class, "pr");
    pr.setDisplayName("one");
    pr.setOrganism(human);

    //another PR with the SAME URI to be merged into the target model
    ProteinReference pr1 = factory.create(ProteinReference.class, "pr");
    Xref ux = factory.create(UnificationXref.class, "ux");
    ux.setDb("UniProt");
    ux.setId("P12345");
    pr1.addName("one");
    pr1.addName("two");
    pr1.addXref(ux);
    UnificationXref uxHuman1 = factory.create(UnificationXref.class, "9606"); //also, the same URI as for uxHuman above
    BioSource human1 = factory.create(BioSource.class, "human"); //same URI as for the other BioSource above
    human1.addXref(uxHuman1);
    human1.setDisplayName("human"); //but - no standardName nor other names are set
    pr1.setOrganism(human1);
    PublicationXref px = factory.create(PublicationXref.class, "publication");
    human1.addXref(px);

    //merge NOT using any filters (in the Constructor)
    SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
    merger.merge(model, pr1);

    assertEquals(5, model.getObjects().size());
    ProteinReference mergedPr = (ProteinReference) model.getByID("pr");
    assertTrue(mergedPr.getXref().isEmpty()); //xref wasn't copied
    assertEquals("one", mergedPr.getDisplayName());
    assertEquals(1, mergedPr.getName().size()); //name "two" wasn't copied (when no Filter used)
    assertEquals("Homo sapiens", mergedPr.getOrganism().getStandardName());
    assertNull(mergedPr.getOrganism().getDisplayName());
    assertEquals(1, mergedPr.getOrganism().getName().size()); //there's only displayName ('name' is a sub-prop. of that)
    assertNotNull(model.getByID("publication")); //the pub.xref was merged
    // but it's dangling, because
    assertEquals(1, mergedPr.getOrganism().getXref().size());
    assertEquals(uxHuman, mergedPr.getOrganism().getXref().iterator().next());
  }


  @Test
  public final void testMergeWithFilter() {
    BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
    Model model = factory.createModel();
    //create a PR, BS, etc., in the (target) model
    BioSource human = model.addNew(BioSource.class, "human");
    human.setStandardName("Homo sapiens"); //no displayName, no other names
    UnificationXref uxHuman = model.addNew(UnificationXref.class, "9606");
    uxHuman.setDb("taxonomy");
    uxHuman.setId("9606");
    human.addXref(uxHuman);
    ProteinReference pr = model.addNew(ProteinReference.class, "pr");
    pr.setDisplayName("one");
    pr.setOrganism(human);

    //another PR with the same URI to be merged into the target model
    ProteinReference pr1 = factory.create(ProteinReference.class, "pr");
    Xref ux = factory.create(UnificationXref.class, "ux");
    ux.setDb("UniProt");
    ux.setId("P12345");
    pr1.addName("one");
    pr1.addName("two");
    pr1.addXref(ux);
    UnificationXref uxHuman1 = factory.create(UnificationXref.class, "9606"); //use the same URI as for uxHuman above
    uxHuman1.setDb("Taxonomy"); //Capitalized, unlike the uxHuman above
    uxHuman1.setId("9606");
    BioSource human1 = factory.create(BioSource.class, "human"); //same URI as for the other BioSource above
    human1.addXref(uxHuman1);
    human1.setDisplayName("human"); //but - no standardName nor other names are set
    pr1.setOrganism(human1);
    PublicationXref px = factory.create(PublicationXref.class, "publication");
    px.setId("00000");
    px.setDb("PubMed");
    human1.addXref(px);

    // Merge using a Filter, which
    // enables copying mul. card. property values of same-URI source EntityReference objects
    // (for all other types, if the target model has a same-type object with the same URI as in the sources
    // , incl. nested objects, then )
    SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3, object -> object instanceof EntityReference);
    merger.merge(model, pr1);

    assertEquals(5, model.getObjects().size());
    ProteinReference mergedPr = (ProteinReference) model.getByID("pr");
    assertFalse(mergedPr.getXref().isEmpty());
    assertEquals("one", mergedPr.getDisplayName());
    assertEquals(2, mergedPr.getName().size());

    assertEquals("Homo sapiens", mergedPr.getOrganism().getStandardName());
    assertNull(mergedPr.getOrganism().getDisplayName());
    //and, because BioSource is not considered in the Filter (see SimpleMerger's constructor parameter),
    // the 'human' name and publication xref px were not copied from source (human1) to the target (human) BioSource:
    assertEquals(1, mergedPr.getOrganism().getName().size());
    assertEquals(1, mergedPr.getOrganism().getXref().size());
    px = (PublicationXref) model.getByID("publication");
    assertNotNull(px); //the pub.xref was merged to the target model,
    // but it's dangling, because -
    for (XReferrable xReferrable : px.getXrefOf()) {
      assertFalse(model.contains(xReferrable));
    }

    // Well.
    // Next, merge using another Filter (adding BioSource type there)
    merger = new SimpleMerger(SimpleEditorMap.L3, object -> {
      return object instanceof EntityReference
          || object instanceof BioSource; //i.e., enable copying BioSource's props too
    });
    model = factory.createModel(); //a fresh empty model
    model.add(pr);
    model.add(human);
    model.add(uxHuman);

    merger.merge(model, pr1);

    assertEquals(5, model.getObjects().size());
    mergedPr = (ProteinReference) model.getByID("pr");
    assertFalse(mergedPr.getXref().isEmpty());
    assertEquals("one", mergedPr.getDisplayName());
    assertEquals(2, mergedPr.getName().size());
    assertEquals("Homo sapiens", mergedPr.getOrganism().getStandardName()); //it's the BioSource from orig. 'pr'
    assertNull(mergedPr.getOrganism().getDisplayName()); //single-cardinality prop. were not touched
    //and, because BioSource is now part of the Filter,
    //the 'human' (name) and publication xref px are copied from human1 to human BioSource:
    assertEquals(2, mergedPr.getOrganism().getName().size());
    assertTrue(mergedPr.getOrganism().getName().contains("human")); //originally, it wasn't there
    assertEquals(2, mergedPr.getOrganism().getXref().size()); //both xrefs
    px = (PublicationXref) model.getByID("publication");
    assertNotNull(px); //the pub.xref was copied; it's NOT dangling -
    assertTrue(mergedPr.getOrganism().getXref().contains(px));
    //the same URI uni.xref did actually not overwrite the original uxHuman (having lowercase "taxonomy" as its db)
    assertTrue(mergedPr.getOrganism().getXref().contains(uxHuman));
    assertEquals(uxHuman, uxHuman1); //two xrefs have same type and URI are equal,
    assertFalse(mergedPr.getOrganism().getXref().contains(uxHuman1)); //different object but same URI
  }

}
