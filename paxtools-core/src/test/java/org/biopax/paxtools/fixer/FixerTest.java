package org.biopax.paxtools.fixer;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**

 */
public class FixerTest
{

	public void testGenericNormalization()
	{
		Mock mock = new Mock();
		Protein[] p = mock.create(Protein.class, 3);
		ProteinReference[] pr = mock.create(ProteinReference.class, 2);

		mock.bindArrays("entityReference", Arrays.copyOfRange(p, 0, 2), pr);

		mock.bindInPairs("memberPhysicalEntity",
		                 p[2],p[0],
		                 p[2],p[1]);

		Fixer.normalizeGenerics(mock.model);

		assertThat(true, is(p[2].getEntityReference()!=null));
		assertThat(true, is(p[2].getEntityReference().getMemberEntityReference().contains(pr[0])));
		assertThat(true, is(p[2].getEntityReference().getMemberEntityReference().contains(pr[1])));



	}

    @Test
    public void testFixEquivalentFeatures()
    {
        Mock mock = new Mock();
	    SequenceSite[] ss= mock.create(SequenceSite.class,1);
	    ss[0].setSequencePosition(0);
	    ss[0].setPositionStatus(PositionStatusType.EQUAL);

	    ModificationFeature[] mf = mock.create(ModificationFeature.class, 2);
	    mf[0].setFeatureLocation(ss[0]);
	    mf[1].setFeatureLocation(ss[0]);

	    mf[0].setFeatureLocation(ss[0]);
	    mf[1].setFeatureLocation(ss[0]);

	    ProteinReference[] pr = mock.create(ProteinReference.class, 1);
	    pr[0].addEntityFeature(mf[0]);
	    pr[0].addEntityFeature(mf[1]);

	    Fixer fixer = new Fixer();
	    fixer.replaceEquivalentFeatures(mock.model);
	    assertTrue(mock.model.getObjects(ModificationFeature.class).size()==1);
    }
}
