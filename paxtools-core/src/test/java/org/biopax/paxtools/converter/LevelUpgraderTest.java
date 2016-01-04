package org.biopax.paxtools.converter;

import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;

public class LevelUpgraderTest {


	@Test
	public final void testGetLocalId() {
		BioPAXElement bpe = BioPAXLevel.L2.getDefaultFactory().create("protein", "http://example.com#someid");
		assertEquals("someid",LevelUpgrader.getLocalId(bpe));
		bpe = BioPAXLevel.L2.getDefaultFactory().create("protein", "http://example.com/someid");
		assertEquals("someid",LevelUpgrader.getLocalId(bpe));
	}

	@Test
	public final void testFilter() throws IOException {
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(
			getClass().getClassLoader()
				.getResourceAsStream("L2/biopax-example-short-pathway.owl"));
		
		physicalEntityParticipant pep31 = (physicalEntityParticipant) model.getByID(model.getXmlBase() + "physicalEntityParticipant31");
		assertTrue(pep31 instanceof physicalEntityParticipant);
		assertTrue(pep31.getPHYSICAL_ENTITY() instanceof protein);
		assertNotNull(pep31.getCELLULAR_LOCATION());
		
		model = (new LevelUpgrader()).filter(model);
		
		if (model != null) {
			io.convertToOWL(model, new FileOutputStream(
					getClass().getClassLoader().getResource("").getFile() + File.separator + "converted.owl"));
		}
		
		
		SmallMolecule p9 = (SmallMolecule) model.getByID(model.getXmlBase() + "physicalEntityParticipant9");
		Protein p31 = (Protein) model.getByID(model.getXmlBase() + "physicalEntityParticipant31");
		assertTrue(p9 instanceof SmallMolecule);
		assertTrue(p31 instanceof Protein);
		assertNotNull(p9.getCellularLocation());
		assertNotNull(p31.getCellularLocation());
		assertEquals(p9.getCellularLocation(), p31.getCellularLocation());
	}

    @Test
	public final void testFilterBigger() throws IOException {
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(
			getClass().getClassLoader()
				.getResourceAsStream("L2/biopax-example-ecocyc-glycolysis.owl"));
		model = (new LevelUpgrader()).filter(model);
		if (model != null) {
			io.convertToOWL(model, new FileOutputStream(
					getClass().getClassLoader().getResource("").getFile() + File.separator + "converted-big.owl"));
		}
	}
}
