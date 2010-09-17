package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.Protein;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CommandManagerTest
{
	@Test
	public void TestCommandManager()
	{
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model model = factory.createModel();
		CommandManager manager = new CommandManager(model);
		Set<BioPAXElement> objects = new HashSet<BioPAXElement>();

		Protein protein = factory.reflectivelyCreate(Protein.class);
		protein.setRDFId("1");
		objects.add(protein);

		Protein protein2 = factory.reflectivelyCreate(Protein.class);
		protein2.setRDFId("2");
		objects.add(protein2);

		MolecularInteraction interaction = factory.reflectivelyCreate(MolecularInteraction.class);
		interaction.setRDFId("3");
		objects.add(interaction);


		manager.addObjects(objects);
		System.out.println(model.getObjects().size());
		manager.undo();
		System.out.println(model.getObjects().size());
		manager.redo();


		PropertyEditor propertyEditor =
				PropertyEditor.createPropertyEditor(MolecularInteraction.class, "participant");
		manager.addProperty(interaction, propertyEditor,protein);
		manager.addProperty(interaction, propertyEditor,protein2);
		System.out.println(model.getObjects().size());
		manager.undo();
		manager.undo();
		System.out.println(model.getObjects().size());
		manager.undo();
		System.out.println(model.getObjects().size());
		manager.redo();
		manager.redo();
		manager.redo();
		System.out.println(model.getObjects().size());






	}
}
