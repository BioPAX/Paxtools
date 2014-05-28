package org.biopax.paxtools.controller;

import org.biopax.paxtools.command.CommandManager;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.Protein;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertTrue;

public class CommandManagerTest
{
	// TODO use assertions instead System.out...
	@Test
	public void TestCommandManager()
	{
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model model = factory.createModel();
		CommandManager manager = new CommandManager(model);
		Set<BioPAXElement> objects = new HashSet<BioPAXElement>();

		Protein protein = factory.create(Protein.class, "1");
		objects.add(protein);

		Protein protein2 = factory.create(Protein.class, "2");
		objects.add(protein2);

		MolecularInteraction interaction = factory.create(MolecularInteraction.class, "3");
		objects.add(interaction);

		manager.addObjects(objects);
		assertTrue(model.getObjects().size() == 3);
		manager.undo();
		assertTrue(model.getObjects().size()==0);
		manager.redo();

		PropertyEditor propertyEditor =
				AbstractPropertyEditor.createPropertyEditor(MolecularInteraction.class, "participant");
		manager.addProperty(interaction, propertyEditor,protein);
		manager.addProperty(interaction, propertyEditor,protein2);
		assertTrue(model.getObjects().size()==3);
		manager.undo();
		manager.undo();
		assertTrue(model.getObjects().size()==3);
		manager.undo();
		assertTrue(model.getObjects().size()==0);
		manager.redo();
		manager.redo();
		manager.redo();
		assertTrue(model.getObjects().size()==3);
	}
}
