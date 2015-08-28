package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class FetcherTest {

	private static Model model;

	@BeforeClass
	public static void setupTest() {
		model = BioPAXLevel.L3.getDefaultFactory().createModel();

		//create/add 8 objects
		Pathway pw1 = model.addNew(Pathway.class, "pathway1");
		Pathway pw2 = model.addNew(Pathway.class, "pathway2");
		Pathway pw3 = model.addNew(Pathway.class, "pathway3");
		Pathway pw4 = model.addNew(Pathway.class, "pathway4");
		Protein p1 = model.addNew(Protein.class, "p1");
		Conversion conv1 = model.addNew(Conversion.class, "conv1");
		Protein p2 = model.addNew(Protein.class, "p2");
		Control con1 = model.addNew(Control.class, "con1");

		pw1.addPathwayComponent(pw2);
		pw1.addPathwayComponent(pw3);
		pw1.addPathwayComponent(pw4);
		pw1.addPathwayComponent(conv1);
		pw2.addPathwayComponent(pw3);
		pw2.addPathwayComponent(pw4);
		pw3.addPathwayComponent(pw4);
		pw3.addPathwayComponent(pw1); //loop
		pw3.addPathwayComponent(conv1);
		pw3.addPathwayComponent(con1);
		pw4.addPathwayComponent(pw2); //loop
		pw4.addPathwayComponent(con1);
		conv1.addLeft(p1);
		conv1.addRight(p1);
		con1.addControlled(conv1);
		con1.addController(p2);
	}

	@Test
	public final void testForEndlessLoopOrOutOfMemory() {

		Pathway pw1 = (Pathway) model.getByID("pathway1");
		Set<Protein> proteins = new Fetcher(SimpleEditorMap.L3).fetch(pw1, Protein.class);
		assertEquals(2, proteins.size());

		Set<BioPAXElement> bpes = new Fetcher(SimpleEditorMap.L3).fetch(pw1);
		assertEquals(8, bpes.size());
	}

	@Test
	public final void testFetchDepth() {

		Pathway pw1 = (Pathway) model.getByID("pathway1");
		Set<BioPAXElement> elements;

		//exception
		try {
			new Fetcher(SimpleEditorMap.L3).fetch(pw1, 0);
			fail();
		} catch (IllegalArgumentException e) {
		}
		try {
			new Fetcher(SimpleEditorMap.L3).fetch(pw1, -1);
			fail();
		} catch (IllegalArgumentException e) {
		}

		//depth=1
		elements = new Fetcher(SimpleEditorMap.L3).fetch(pw1, 1);
		assertEquals(4, elements.size());

		//depth=2
		elements = new Fetcher(SimpleEditorMap.L3).fetch(pw1, 2);
		assertEquals(6, elements.size()); //that's it

		//depth=3 - all objects except the pw1 itself (despite it belongs to its sub-pathway - loop)
		elements = new Fetcher(SimpleEditorMap.L3).fetch(pw1, 3);
		assertEquals(7, elements.size());

		//depth > 3 all the same...
		elements = new Fetcher(SimpleEditorMap.L3).fetch(pw1, 4);
		assertEquals(7, elements.size());
	}

}
