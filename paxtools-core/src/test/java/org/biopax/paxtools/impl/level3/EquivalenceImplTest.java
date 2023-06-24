package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EquivalenceImplTest {

	@Test
	public final void semanticallyEquivalent() {
		Model m = BioPAXLevel.L3.getDefaultFactory().createModel();

		UnificationXref x = m.addNew(UnificationXref.class, "ExpType_MI_0492");
		x.setDb("MI");
		x.setId("MI:0492");
		EvidenceCodeVocabulary ecv = m.addNew(EvidenceCodeVocabulary.class, "EvidenceCodeVocab_1");
		ecv.addTerm("in vitro");
		ecv.addXref(x);
    Evidence inVitro = m.addNew(Evidence.class, "Evidence_InVitro");
    inVitro.addEvidenceCode(ecv);
		
		x = m.addNew(UnificationXref.class, "ExpType_MI_0493");
		x.setDb("MI");
		x.setId("MI:0493");
		ecv = m.addNew(EvidenceCodeVocabulary.class, "EvidenceCodeVocab_2");
		ecv.addTerm("in vivo");
		ecv.addXref(x);
		Evidence inVivo = m.addNew(Evidence.class, "Evidence_InVivo");
      	
    assertFalse(inVitro.isEquivalent(inVivo));
	}
	
	
	@Test
	public final void bindingFeatureEquivalentNPE() {
		BioPAXIOHandler io = new SimpleIOHandler();
		Model m = io.convertFromOWL(getClass().getResourceAsStream("test_bf_isequivalent-npe.owl"));
		BindingFeature a = (BindingFeature) m.getByID(m.getXmlBase() + "id377153490_STAT3_p_bf3");
		BindingFeature b = (BindingFeature) m.getByID(m.getXmlBase() + "id1300330108_R_smad_bf1_omitCE_unknownBindingSite");
		a.isEquivalent(b); //used to fail with NPE right here!
	}

}
