package org.paxtools.query;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
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

		BioPAXIOHandler io = new SimpleIOHandler();
		io.convertToOWL(model, new FileOutputStream(DIR + "temp2.owl"));
//		return model;
	}

	public static void createStateChange(String name, Model model)
	{
		String stdName = name + "_PR";
		ProteinReference ref = model.addNew(ProteinReference.class, IDBASE + stdName);
		ref.setStandardName(stdName);

		stdName = name + "-1";
		Protein p1 = model.addNew(Protein.class, IDBASE + stdName);
		p1.setEntityReference(ref);
		p1.setStandardName(stdName);
		
		stdName = name + "-2";
		Protein p2 = model.addNew(Protein.class, IDBASE + stdName);
		p2.setEntityReference(ref);
		p2.setStandardName(stdName);

		stdName = p1.getStandardName() + REACTION_ARROW + p2.getStandardName();
		BiochemicalReaction reac = model.addNew(BiochemicalReaction.class, 
				IDBASE + stdName);
		reac.setStandardName(stdName);
		reac.addLeft(p1);
		reac.addRight(p2);
		reac.setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
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
		String std = effector.getStandardName() + ACTIV_ARROW + conv.getStandardName();
		Control con = factory.create(cls, IDBASE + std);
		con.setStandardName(std);
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
		Complex comp = model.addNew(Complex.class, IDBASE + compName);
		comp.setStandardName(compName);

		for (Protein prot : prots)
		{
			String std = prot.getStandardName() + "_cm";
			Protein p = model.addNew(Protein.class, IDBASE + std);
			p.setStandardName(std);
			comp.addComponent(p);
		}

		String std = "Assembly_of_" + compName;
		ComplexAssembly ca = model.addNew(ComplexAssembly.class, IDBASE + std);
		ca.setStandardName(std);
		ca.setConversionDirection(ConversionDirectionType.LEFT_TO_RIGHT);
		for (Protein prot : prots)
		{
			ca.addLeft(prot);
		}
		ca.addRight(comp);
	}
}
