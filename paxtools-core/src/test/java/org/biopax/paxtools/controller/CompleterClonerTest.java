package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class CompleterClonerTest
{
	private Model model;
	private String pwUri;

	@Before
	public void init() {
		MockFactory mock= new MockFactory(BioPAXLevel.L3);
		model = mock.createModel();

		Pathway pw = mock.create(model,Pathway.class,1)[0];
		pwUri = pw.getUri();
		BiochemicalReaction[] rx = mock.create(model, BiochemicalReaction.class,2);
		Control[] co = mock.create(model, Control.class,2);
		PathwayStep[] ps = mock.create(model,PathwayStep.class,2);
		Protein[] p = mock.create(model,Protein.class,2);
		SmallMolecule[] sm = mock.create(model,SmallMolecule.class,2);

		pw.addPathwayComponent(co[0]);
		pw.addPathwayOrder(ps[0]);
		mock.bindArrays(mock.editor("controlled",Control.class),co,rx);
		mock.bindArrays(mock.editor("controller",Control.class),co,p);
		mock.bindArrays(mock.editor("right",BiochemicalReaction.class),rx,sm);
		ps[0].addNextStep(ps[1]);
		mock.bindArrays(mock.editor("stepProcess",PathwayStep.class),ps,rx);
		mock.bindArrays(mock.editor("stepProcess",PathwayStep.class),ps,co);
	}


	@Test
	public void testCompleteAndClone()
	{
		Completer completer = new Completer(SimpleEditorMap.L3);
		Collection<BioPAXElement> completed = completer.complete(Collections.singleton(model.getByID(pwUri)));
		assertTrue(completed.size()>1);

		//PathwayStep1 should not be there if @AutoComplete(forward=false) has any effect on getNextStep()
		Set<PathwayStep> steps = new ClassFilterSet<>(completed,PathwayStep.class);
		assertEquals(1, steps.size());
		//There should be only one BiochemicalReaction that belongs to PathwayStep0
		Set<Conversion> rxs = new ClassFilterSet<>(completed,Conversion.class);
		assertEquals(1, rxs.size());
	}
}
