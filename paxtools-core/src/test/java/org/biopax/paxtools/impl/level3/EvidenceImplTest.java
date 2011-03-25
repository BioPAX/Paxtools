package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class EvidenceImplTest {

	@Test
	public final void testSemanticallyEquivalent() {
		Model m = BioPAXLevel.L3.getDefaultFactory().createModel();

		UnificationXref x = m.addNew(UnificationXref.class, "ExpType_MI_0492");
		x.setDb("MI");
		x.setId("MI:0492");
		EvidenceCodeVocabulary ecv = m.addNew(EvidenceCodeVocabulary.class, "EvidenceCodeVocab_1");
		ecv.addTerm("in vitro");
		ecv.addXref(x);
    	Evidence inVitro = (Evidence) m.addNew(Evidence.class, "Evidence_InVitro");
    	inVitro.addEvidenceCode(ecv);
		
		x = m.addNew(UnificationXref.class, "ExpType_MI_0493");
		x.setDb("MI");
		x.setId("MI:0493");
		ecv = m.addNew(EvidenceCodeVocabulary.class, "EvidenceCodeVocab_2");
		ecv.addTerm("in vivo");
		ecv.addXref(x);
		Evidence inVivo = (Evidence) m.addNew(Evidence.class, "Evidence_InVivo");
      	
    	assertFalse(inVitro.isEquivalent(inVivo));
	}

}
