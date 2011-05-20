package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PathAccessorTest
{
  @Test
  public void testPaths()
  {
	  MockFactory factory = new MockFactory(BioPAXLevel.L3);
	  Model model = factory.createModel();
	  Protein p1 = model.addNew(Protein.class, "P1");
	  PublicationXref px = model.addNew(PublicationXref.class,"Px1");
	  px.setId("myId");
	  RelationshipXref rx = model.addNew(RelationshipXref.class, "rx1");
	  rx.setId("yourID");
	  ProteinReference r1 = model.addNew(ProteinReference.class, "R1");
	  r1.addXref(px);
	  r1.addXref(rx);
	  p1.setEntityReference(r1);

	  Protein p2 = model.addNew(Protein.class, "P2");
	  PublicationXref px2 = model.addNew(PublicationXref.class,"Px2");
	  px2.setId("hisId");
	  RelationshipXref rx2 = model.addNew(RelationshipXref.class, "rx2");
	  rx2.setId("herID");
	  ProteinReference r2 = model.addNew(ProteinReference.class, "R2");

	  p2.setEntityReference(r2);
	  r2.addXref(px2);
	  r2.addXref(rx2);


	  PathAccessor accessor = new PathAccessor("Protein/entityReference/xref:PublicationXref", BioPAXLevel.L3);
	  Set values = accessor.getValueFromBean(p1);
	  assertEquals(new ClassFilterSet(r1.getXref(), PublicationXref.class).size(), values.size());


	  accessor = new PathAccessor("Protein/entityReference/xref:RelationshipXref", BioPAXLevel.L3);
	  values = accessor.getValueFromBean(p1);
	  assertEquals(new ClassFilterSet(r1.getXref(), RelationshipXref.class).size(), values.size());

	  accessor = new PathAccessor("Protein/entityReference/xref:RelationshipXref/id", BioPAXLevel.L3);
	  values = accessor.getValueFromBean(p1);
	  assertTrue(values.contains("yourID"));

	  accessor = new PathAccessor("PublicationXref/xrefOf", BioPAXLevel.L3);
	  values = accessor.getValueFromBean(px2);
	  assertTrue(values.contains(r2));

  }
}
