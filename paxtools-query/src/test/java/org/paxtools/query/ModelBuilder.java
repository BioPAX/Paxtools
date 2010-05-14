package org.paxtools.query;

import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.io.FileOutputStream;

/**
 * @author Ozgun Babur
 */
public class ModelBuilder
{
	static final String IDBASE = "";
	static final BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
	static final String REACTION_ARROW = "_->_";
	static final String ACTIV_ARROW = "_activates_";
	static final String DIR = "../biopax/Level3/examples/";

	@Test
	public void generateModel1() throws Throwable
	{
		Model model = factory.createModel();

		createStateChange("Z", model);
		Conversion last = (Conversion) model.getByID(IDBASE + "Z-1" + REACTION_ARROW + "Z-2");

		createStateChange("A", model);

		createCascadeBetween((Protein) model.getByID(IDBASE + "A-1"), last, "B", 5, model);
		createCascadeBetween((Protein) model.getByID(IDBASE + "A-2"), last, "C", 5, model);

		createStateChange("D", model);
		createStateChange("E", model);
		createComplex(model, "Comp1",
			(Protein) model.getByID(IDBASE + "D-2"),
			(Protein) model.getByID(IDBASE + "E-2"));

		createControl((Protein) model.getByID(IDBASE + "D-2_cm"),
			(Conversion) model.getByID(IDBASE + "A-1" + REACTION_ARROW + "A-2"),
			Catalysis.class, ControlType.INHIBITION, model);

		SimpleExporter se = new SimpleExporter(BioPAXLevel.L3);
		se.convertToOWL(model, new FileOutputStream(DIR + "temp2.owl"));
//		return model;
	}

	public static void createStateChange(String name, Model model)
	{
		ProteinReference ref = factory.reflectivelyCreate(ProteinReference.class);
		ref.setStandardName(name + "_PR");
		ref.setRDFId(IDBASE + ref.getStandardName());
		model.add(ref);

		Protein p1 = factory.reflectivelyCreate(Protein.class);
		p1.setEntityReference(ref);
		p1.setStandardName(name + "-1");
		p1.setRDFId(IDBASE + p1.getStandardName());
		model.add(p1);
		
		Protein p2 = factory.reflectivelyCreate(Protein.class);
		p2.setEntityReference(ref);
		p2.setStandardName(name + "-2");
		p2.setRDFId(IDBASE + p2.getStandardName());
		model.add(p2);

		BiochemicalReaction reac = factory.reflectivelyCreate(BiochemicalReaction.class);
		reac.setStandardName(p1.getStandardName() + REACTION_ARROW + p2.getStandardName());
		reac.setRDFId(IDBASE + reac.getStandardName());
		reac.addLeft(p1);
		reac.addRight(p2);
		reac.setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
		model.add(reac);
	}

	public static void createActivatedStateChange(Protein activator, String name, Model model)
	{
		createStateChange(name, model);
		BiochemicalReaction reac = (BiochemicalReaction)
			model.getByID(IDBASE + name + "-1" + REACTION_ARROW + name + "-2");

		createControl(activator, reac, Catalysis.class, ControlType.ACTIVATION, model);
	}

	public static void createControl(Protein effector, Conversion conv,
									 Class<? extends Control> cls, ControlType type, Model model)
	{
		Control con = factory.reflectivelyCreate(cls);
		con.setStandardName(effector.getStandardName() + ACTIV_ARROW + conv.getStandardName());
		con.setRDFId(IDBASE + con.getStandardName());
		con.addController(effector);
		con.addControlled(conv);
		con.setControlType(type);
		model.add(con);
	}

	public static void createCascadeBetween(Protein first, Conversion last, String name, int length,
											Model model)
	{
		createActivatedStateChange(first, (name + 1), model);

		for (int i = 2; i <= length; i++)
		{
			Protein act = (Protein) model.getByID(IDBASE + (name + (i - 1)) + "-2");
			createActivatedStateChange(act, (name + i), model);
		}

		Protein act = (Protein) model.getByID(IDBASE + (name + length) + "-2");
		createControl(act, last, Catalysis.class, ControlType.ACTIVATION, model);
	}

	public static void createComplex(Model model, String compName, Protein... prots)
	{
		Complex comp = factory.reflectivelyCreate(Complex.class);
		comp.setStandardName(compName);
		comp.setRDFId(IDBASE + comp.getStandardName());
		model.add(comp);

		for (Protein prot : prots)
		{
			Protein p = factory.reflectivelyCreate(Protein.class);
			p.setStandardName(prot.getStandardName() + "_cm");
			p.setRDFId(IDBASE + p.getStandardName());
			comp.addComponent(p);
			model.add(p);
		}

		ComplexAssembly ca = factory.reflectivelyCreate(ComplexAssembly.class);
		ca.setStandardName("Assembly_of_" + compName);
		ca.setRDFId(IDBASE + ca.getStandardName());
		ca.setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
		for (Protein prot : prots)
		{
			ca.addLeft(prot);
		}
		ca.addRight(comp);
		model.add(ca);
	}
}
