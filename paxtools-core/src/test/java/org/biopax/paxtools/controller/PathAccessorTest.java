package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

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
	  ProteinReference r1 = model.addNew(ProteinReference.class, "R1");
	  r1.addName("random : xccxawsrfhglghfdklgh");
	  p1.setEntityReference(r1);


	  PathAccessor accessor = new PathAccessor("Protein/entityReference/name", BioPAXLevel.L3);
	  Set values = accessor.getValueFromBean(p1);
	  assertEquals(r1.getName(), values);

  }
}
