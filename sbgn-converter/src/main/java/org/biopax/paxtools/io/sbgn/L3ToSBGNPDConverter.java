package org.biopax.paxtools.io.sbgn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.*;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.*;
import java.util.Map;

/**
 * This class converts BioPAX L3 model into SBGN PD. It does not layout the objects, leaves location
 * information unassigned.
 * <p>
 * This version ignores several BioPAX L3 features during conversion:
 * <ul>
 * <li>Parent-child relationship between physical entities</li>
 * <li>Parent-child relationship between entity references</li>
 * <li>Binding features and covalent binding features of physical entities</li>
 * </ul>
 *
 * Also note that:
 * <ul>
 * <li>Compartment is just a controlled vocabulary in BioPAX, so nesting and neighborhood relations
 * between compartments are not handled here.</li>
 * <li>Control structures in BioPAX and in SBGN PD are a little different. We use AND and NOT
 * glyphs to approximate controls in BioPAX. However, ANDing everything is not really proper because
 * BioPAX do not imply a logical operator between controllers.</li>
 * </ul>
 *
 * @author Ozgun Babur
 */
public class L3ToSBGNPDConverter
{
	private static final Log log = LogFactory.getLog(L3ToSBGNPDConverter.class);

	// Values for SBGN classes

	private static final String COMPARTMENT = "compartment";
	public static final String PROCESS = "process";

	public static final String CONSUMPTION = "consumption";
	public static final String PRODUCTION = "production";
	private static final String CATALYSIS = "catalysis";
	private static final String STIMULATION = "stimulation";
	private static final String INHIBITION = "inhibition";
	private static final String LOGIC_ARC = "logic arc";

	private static final String AND = "and";
	private static final String NOT = "not";

	public static final String STATE_VARIABLE = "state variable";
	public static final String INFO = "unit of information";

	/**
	 * A matching between physical entities and SBGN classes.
	 */
	private static Map<Class<? extends BioPAXElement>, String> typeMatchMap;

	/**
	 * For creating SBGN objects.
	 */
	private static ObjectFactory factory;

	//-- Section: Public methods ------------------------------------------------------------------|

	/**
	 * Converts the given model to SBGN, and writes in the specified file.
	 *
	 * @param model model to convert
	 * @param file file to write
	 */
	public static void writeSBGN(Model model, String file)
	{
		// Create the model
		Sbgn sbgn = createSBGN(model);

		// Write in file

		try
		{
			SbgnUtil.writeToFile(sbgn, new File(file));
		}
		catch (JAXBException e)
		{
			if (log.isErrorEnabled()) log.error(e.getCause(), e);
		}
	}

	/**
	 * Creates an Sbgn object from the given model.
	 *
	 * @param model model to convert to SBGN
	 * @return SBGN representation of the model
	 */
	public static Sbgn createSBGN(Model model)
	{
		assert model.getLevel().equals(BioPAXLevel.L3) : "This method only supports L3 graphs";

		Map<String, Glyph> glyphMap = new HashMap<String, Glyph>();
		Map<String, Glyph> compartmentMap = new HashMap<String, Glyph>();
		Map<String, Arc> arcMap = new HashMap<String, Arc>();

		// Create glyphs for Physical Entities

		for (PhysicalEntity entity : model.getObjects(PhysicalEntity.class))
		{
			if (needsToBeRepresented(entity))
			{
				createGlyph(entity, glyphMap, compartmentMap);
			}
		}

		// Create glyph for conversions and link with arcs

		for (Conversion conv : model.getObjects(Conversion.class))
		{
			// For each conversion we check if we need to create a left-to-right and/or
			// right-to-left process.

			if (conv.getConversionDirection() == null ||
				conv.getConversionDirection().equals(ConversionDirectionType.LEFT_TO_RIGHT) ||
				conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE))
			{
				createProcessAndConnections(conv, ConversionDirectionType.LEFT_TO_RIGHT,
					glyphMap, arcMap);
			}
			else if (conv.getConversionDirection() != null &&
				(conv.getConversionDirection().equals(ConversionDirectionType.RIGHT_TO_LEFT) ||
				conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE)))
			{
				createProcessAndConnections(conv, ConversionDirectionType.RIGHT_TO_LEFT,
					glyphMap, arcMap);
			}
		}

		// Create glyph for template reactions and link with arcs

		for (TemplateReaction tr : model.getObjects(TemplateReaction.class))
		{
			createProcessAndConnections(tr, glyphMap, arcMap);
		}

		// Register created objects into sbgn construct

		Sbgn sbgn = factory.createSbgn();
		org.sbgn.bindings.Map map = new org.sbgn.bindings.Map();
		// todo set the language here when libSBGN supports
		sbgn.setMap(map);
		map.getGlyph().addAll(glyphMap.values());
		map.getGlyph().addAll(compartmentMap.values());
		map.getArc().addAll(arcMap.values());
		return sbgn;
	}

	//-- Section: Create molecules ----------------------------------------------------------------|

	/**
	 * We don't want to represent every PhysicalEntity in SBGN. For instance if a Complex is nested
	 * under another Complex, and if it is not a participant of any interaction, we don't want to
	 * draw it.
	 *
	 * @param entity physical entity to check
	 * @return true if we will draw this entity in SBGN
	 */
	private static boolean needsToBeRepresented(PhysicalEntity entity)
	{
		// We are flatting complex members -- thus inner complexes will not be represented
		if (entity instanceof Complex)
		{
			Complex c = (Complex) entity;
			if (c.getParticipantOf().isEmpty() && !c.getComponentOf().isEmpty())
			{
				return false;
			}
		}
		// Complex members will be created during creation of parent complex
		else if (entity.getParticipantOf().isEmpty() && !entity.getComponentOf().isEmpty())
		{
			return false;
		}
		return true;
	}

	/**
	 * Creates a glyph representing the given PhysicalEntity.
	 *
	 * @param pe PhysicalEntity to represent
	 * @param glyphMap glyph registry
	 * @param compartmentMap compartment repository
	 * @return the created glyph
	 */
	private static Glyph createGlyph(PhysicalEntity pe, Map<String, Glyph> glyphMap, Map<String,
		Glyph> compartmentMap)
	{
		if (glyphMap.containsKey(pe.getRDFId())) return glyphMap.get(pe.getRDFId());

		// Create its glyph and register

		Glyph g = createGlyph(pe);
		glyphMap.put(g.getId(), g);

		// Create compartment -- add this inside the compartment

		Glyph loc = getCompartment(getCompartment(pe), compartmentMap);
		if (loc != null) loc.getGlyph().add(g);

		// Fill-in the complex members if this is a complex

		if (pe instanceof Complex)
		{
			createComplexContent((Complex) pe, glyphMap);
		}

		return g;
	}

	/**
	 * This method creates a glyph for the given PhysicalEntity, sets its title and state variables
	 * if applicable.
	 *
	 * @param pe PhysicalEntity to represent
	 * @return the glyph
	 */
	private static Glyph createGlyph(PhysicalEntity pe)
	{
		String s = typeMatchMap.get(pe.getModelInterface());

		Glyph g = factory.createGlyph();
		g.setId(pe.getRDFId());
		g.setClazz(s);

		// Set the label

		Label label = factory.createLabel();
		label.setText(findALabelForMolecule(pe));
		g.setLabel(label);

		// Put on state variables

		List<Glyph> states = getInformation(pe);
		g.getGlyph().addAll(states);

		return g;
	}

	/**
	 * Fills in the content of a complex.
	 *
	 * @param cx Complex to be filled
	 * @param glyphMap glyph repository
	 */
	private static void createComplexContent(Complex cx, Map<String, Glyph> glyphMap)
	{
		Glyph cg = glyphMap.get(cx.getRDFId());

		for (PhysicalEntity mem : cx.getComponent())
		{
			if (mem instanceof Complex)
			{
				addComplexAsMember((Complex) mem, cg, glyphMap);
			}
			else
			{
				createComplexMember(mem, cg, glyphMap);
			}
		}
	}

	/**
	 * Recursive method for creating the content of a complex. A complex may contain other complexes
	 * (bad practice), but SBGN needs them flattened. If an inner complex is empty, then we
	 * represent it using a glyph. Otherwise we represent only the members of this inner complex,
	 * merging them with the most outer complex.
	 *
	 * @param cx inner complex to add as member
	 * @param container glyph for most outer complex
	 * @param glyphMap glyph repository
	 */
	private static void addComplexAsMember(Complex cx, Glyph container, Map<String, Glyph> glyphMap)
	{
		if (cx.getComponent().isEmpty())
		{
			// Put a glyph for empty inner complex
			createComplexMember(cx, container, glyphMap);
		}
		else
		{
			for (PhysicalEntity mem : cx.getComponent())
			{
				if (mem instanceof Complex)
				{
					// Recursive call for inner complexes
					addComplexAsMember((Complex) mem, container, glyphMap);
				}
				else
				{
					createComplexMember(mem, container, glyphMap);
				}
			}
		}
	}

	/**
	 * Creates a glyph for the complex member.
	 *
	 * @param pe PhysicalEntity to represent as complex member
	 * @param container Glyph for the complex shell
	 * @param glyphMap glyph repository
	 */
	private static void createComplexMember(PhysicalEntity pe, Glyph container,
		Map<String, Glyph> glyphMap)
	{
		Glyph g = createGlyph(pe);
		container.getGlyph().add(g);

		// A PhysicalEntity may appear in many complexes -- we identify the member using its complex
		g.setId(g.getId() + "|" + container.getId());

		glyphMap.put(g.getId(), g);
	}

	/**
	 * Looks for the display name of this PhysicalEntity. If there is none, then it looks for the
	 * display name of its EntityReference. If still no name at hand, it tries the standard
	 * name, and then first element in name lists.
	 *
	 * A good BioPAX file will use a short and specific name (like HGNC symbols) as displayName.
	 *
	 * @param pe PhysicalEntity to find a name
	 * @return a name for labeling
	 */
	private static String findALabelForMolecule(PhysicalEntity pe)
	{
		// Use display name of entity
		String name = pe.getDisplayName();

		if (name == null)
		{
			EntityReference er = null;

			if (pe instanceof SimplePhysicalEntity)
			{
				er = ((SimplePhysicalEntity) pe).getEntityReference();
			}

			if (er != null)
			{
				// Use display name of reference
				name = er.getDisplayName();
			}

			if (name == null)
			{
				// Use standard name of entity
				name = pe.getStandardName();

				if (name == null)
				{
					if (er != null)
					{
						// Use standard name of reference
						name = er.getStandardName();
					}

					if (name == null)
					{
						if (!pe.getName().isEmpty())
						{
							// Use first name of entity
							name = pe.getName().iterator().next();
						}
						else if (er != null && !er.getName().isEmpty())
						{
							// Use first name of reference
							name = er.getName().iterator().next();
						}
					}
				}
			}
		}

		if (name == null)
		{
			// Don't leave it without a name
			name = "noname";
		}
		return name;
	}

	/**
	 * Adds molecule type, and iterates over features of the entity and creates corresponding state
	 * variables. Ignores binding features and covalent-binding features.
	 * 
	 * @param pe entity to collect features
	 * @return list of state variables
	 */
	private static List<Glyph> getInformation(PhysicalEntity pe)
	{
		List<Glyph> list = new ArrayList<Glyph>();

		// Add the molecule type before states if this is a nucleic acid

		if (pe instanceof NucleicAcid)
		{
			Glyph g = factory.createGlyph();
			g.setClazz(INFO);
			Glyph.State s = factory.createGlyphState();
			s.setVariable("mt");
			s.setValue((pe instanceof Dna || pe instanceof DnaRegion) ? "DNA" :
				(pe instanceof Rna || pe instanceof RnaRegion) ? "RNA" : "NucleicAcid");
			g.setState(s);
			list.add(g);
		}

		// Extract state variables

		extractFeatures(pe.getFeature(), true, list);
		extractFeatures(pe.getNotFeature(), false, list);

		return list;
	}

	/**
	 * Converts the features in the given feature set. Adds a "!" in front of NOT features.
	 *
	 * @param features feature set
	 * @param normalFeature specifies the type of features -- normal feature = true,
	 * 		  NOT feature = false
	 * @param list state variables
	 */
	private static void extractFeatures(Set<EntityFeature> features, boolean normalFeature,
		List<Glyph> list)
	{
		for (EntityFeature feature : features)
		{
			if (feature instanceof ModificationFeature || feature instanceof FragmentFeature)
			{
				Glyph stvar = factory.createGlyph();
				stvar.setClazz(STATE_VARIABLE);
				Glyph.State state = factory.createGlyphState();
				stvar.setState(state);

				if (feature instanceof ModificationFeature)
				{
					ModificationFeature mf = (ModificationFeature) feature;
					state.setVariable(mf.getModificationType().toString());
				}
				else
				{
					state.setVariable("Fragment");
				}

				// Add a "!" in front of NOT features

				if (!normalFeature)
				{
					state.setVariable("!" + state.getVariable());
				}

				// Add feature location as state value if exists

				SequenceLocation loc = feature.getFeatureLocation();
				if (loc != null)
				{
					String value;
					if (loc instanceof SequenceSite)
					{
						SequenceSite site = (SequenceSite) loc;
						value = "" + site.getSequencePosition();
					}
					else if (loc instanceof SequenceInterval)
					{
						SequenceInterval itv = (SequenceInterval) loc;
						value = itv.getSequenceIntervalBegin().getSequencePosition() + "-" +
							itv.getSequenceIntervalEnd().getSequencePosition();
					}
					else
					{
						value = loc.toString();
					}
					state.setValue(value);
				}

				list.add(stvar);
			}
		}
	}

	//-- Section: Create compartments -------------------------------------------------------------|

	/**
	 * Creates or gets the compartment with the given name.
	 *
	 * @param name name of the compartment
	 * @param compartmentMap compartment registry
	 * @return the compartment glyph
	 */
	private static Glyph getCompartment(String name, Map<String, Glyph> compartmentMap)
	{
		if (name == null) return null;
		if (compartmentMap.containsKey(name)) return compartmentMap.get(name);

		Glyph comp = factory.createGlyph();
		comp.setId(name);
		Label label = factory.createLabel();
		label.setText(name);
		comp.setClazz(COMPARTMENT);

		compartmentMap.put(name, comp);
		return comp;
	}

	/**
	 * Gets the compartment of the given PhysicalEntity.
	 *
	 * @param pe PhysicalEntity to look for its compartment
	 * @return name of compartment or null if there is none
	 */
	private static String getCompartment(PhysicalEntity pe)
	{
		CellularLocationVocabulary cl = pe.getCellularLocation();
		if (cl != null)
		{
			if (!cl.getTerm().isEmpty())
			{
				return cl.getTerm().iterator().next();
			}
		}
		return null;
	}

	//-- Section: Create reactions ----------------------------------------------------------------|

	/**
	 * Creates a representation for Conversion.
	 *
	 * @param cnv the conversion
	 * @param direction direction of the conversion to create
	 * @param glyphMap glyph repository
	 * @param arcMap arc repository
	 */
	private static void createProcessAndConnections(Conversion cnv,
		ConversionDirectionType direction, Map<String, Glyph> glyphMap, Map<String, Arc> arcMap)
	{
		assert direction.equals(ConversionDirectionType.LEFT_TO_RIGHT) ||
			direction.equals(ConversionDirectionType.RIGHT_TO_LEFT);

		assert cnv.getConversionDirection() == null ||
			cnv.getConversionDirection().equals(direction) ||
			cnv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE);

		// create the process for the conversion in that direction

		Glyph process = factory.createGlyph();
		process.setClazz(PROCESS);
		process.setId(cnv.getRDFId() + direction);
		glyphMap.put(process.getId(), process);

		// Determine input and output sets
		
		Set<PhysicalEntity> input = direction.equals(ConversionDirectionType.LEFT_TO_RIGHT) ?
			cnv.getLeft() : cnv.getRight();
		Set<PhysicalEntity> output = direction.equals(ConversionDirectionType.RIGHT_TO_LEFT) ?
			cnv.getLeft() : cnv.getRight();

		// Create input and outputs ports for the process
		addPorts(process);

		// Associate inputs to input port

		for (PhysicalEntity pe : input)
		{
			Glyph g = glyphMap.get(pe.getRDFId());
			createArc(g, process.getPort().get(0), CONSUMPTION, arcMap);
		}

		// Associate outputs to output port

		for (PhysicalEntity pe : output)
		{
			Glyph g = glyphMap.get(pe.getRDFId());
			createArc(process.getPort().get(1), g, PRODUCTION, arcMap);
		}

		// Associate controllers

		for (Control ctrl : cnv.getControlledOf())
		{
			// If there is a direction mismatch between the process and the control, just skip it

			if (ctrl instanceof Catalysis)
			{
				CatalysisDirectionType catDir = ((Catalysis) ctrl).getCatalysisDirection();
				if (catDir != null)
				{
					if ((catDir.equals(CatalysisDirectionType.LEFT_TO_RIGHT) &&
						direction.equals(ConversionDirectionType.RIGHT_TO_LEFT)) ||
						(catDir.equals(CatalysisDirectionType.RIGHT_TO_LEFT) &&
						direction.equals(ConversionDirectionType.LEFT_TO_RIGHT)))
					{
						// If the conversion is reversible, then this control belongs to the reverse
						// conversion. Otherwise, it will be lost due to direction mismatch. In that
						// case, log a warning.

						if (!direction.equals(ConversionDirectionType.REVERSIBLE))
						{
							if (log.isWarnEnabled()) log.warn("A control is being lost due to " +
								"direction mismatch.\nControl direction = " + catDir +
								"\nConversion direction = " + direction);
						}

						continue;
					}
				}
			}

			Glyph g = createControlStructure(ctrl, glyphMap, arcMap);
			if (g != null) createArc(g, process, getControlType(ctrl), arcMap);
		}
	}

	/**
	 * Creates a representation for TemplateReaction.
	 *
	 * @param tr template reaction
	 * @param glyphMap glyph repository
	 * @param arcMap arc repository
	 */
	private static void createProcessAndConnections(TemplateReaction tr,
		Map<String, Glyph> glyphMap, Map<String, Arc> arcMap)
	{
		// create the process for the reaction

		Glyph process = factory.createGlyph();
		process.setClazz(PROCESS);
		process.setId(tr.getRDFId());
		glyphMap.put(process.getId(), process);

		// Add input and output ports
		addPorts(process);

		// Create a source-and-sink as the input

		Glyph sas = factory.createGlyph();
		sas.setId("SAS_For_" + tr.getRDFId());
		glyphMap.put(sas.getId(), sas);
		createArc(sas, process.getPort().get(0), CONSUMPTION, arcMap);

		// Associate products

		for (PhysicalEntity pe : tr.getProduct())
		{
			Glyph g = glyphMap.get(pe.getRDFId());
			createArc(process.getPort().get(1), g, PRODUCTION, arcMap);
		}

		// Associate controllers

		for (Control ctrl : tr.getControlledOf())
		{
			Glyph g = createControlStructure(ctrl, glyphMap, arcMap);
			if (g != null) createArc(g, process, getControlType(ctrl), arcMap);
		}
	}

	/**
	 * Creates or gets the glyph to connect to the control arc.
	 *
	 * @param ctrl Control to represent
	 * @param glyphMap glyph repository
	 * @param arcMap arc repository
	 * @return glyph representing the controller tree
	 */
	private static Glyph createControlStructure(Control ctrl, Map<String, Glyph> glyphMap,
		Map<String, Arc> arcMap)
	{
		Glyph cg;

		Set<PhysicalEntity> controllers = getControllers(ctrl, glyphMap);

		// If no representable controller found, skip this control
		if (controllers.isEmpty()) cg = null;

		// If there is only one controller with no modulator, put an arc for controller

		else if (controllers.size() == 1 && getControllerSize(ctrl.getControlledOf(), glyphMap) == 0)
		{
			cg = glyphMap.get(controllers.iterator().next().getRDFId());
		}

		else
		{
			// This list will contain handles for each participant of the AND structure
			List<Glyph> toConnect = new ArrayList<Glyph>();

			// Bundle controllers if necessary

			Glyph gg = handlePEGroup(controllers, glyphMap, arcMap);
			toConnect.add(gg);

			// Create handles for each controller

			for (Control ctrl2 : ctrl.getControlledOf())
			{
				Glyph g = createControlStructure(ctrl2, glyphMap, arcMap);
				if (g != null) toConnect.add(g);
			}

			// Handle co-factors of catalysis

			if (ctrl instanceof Catalysis)
			{
				Set<PhysicalEntity> cofs = ((Catalysis) ctrl).getCofactor();
				Glyph g = handlePEGroup(cofs, glyphMap, arcMap);
				toConnect.add(g);
			}

			if (toConnect.isEmpty()) return null;
			else if (toConnect.size() == 1)
			{
				cg = toConnect.iterator().next();
			}
			else
			{
				cg = connectWithAND(toConnect, glyphMap, arcMap);
			}
		}

		// If the control is negative, add a NOT in front of it

		if (cg != null && getControlType(ctrl).equals(INHIBITION))
		{
			cg = addNOT(cg, glyphMap, arcMap);
		}

		return cg;
	}

	/**
	 * Prepares the necessary construct for adding the given PhysicalEntity set to the Control
	 * being drawn.
	 *
	 * @param pes entities to use in control
	 * @param glyphMap glyph repository
	 * @param arcMap arc repository
	 * @return the glyph to connect to the appropriate place
	 */
	private static Glyph handlePEGroup(Set<PhysicalEntity> pes, Map<String, Glyph> glyphMap,
		Map<String, Arc> arcMap)
	{
		if (pes.size() > 1)
		{
			List<Glyph> gs = getGlyphsOfPEs(pes, glyphMap);
			return connectWithAND(gs, glyphMap, arcMap);
		}
		else if (glyphMap.containsKey(pes.iterator().next().getRDFId()))
		{
			return glyphMap.get(pes.iterator().next().getRDFId());
		}
		return null;
	}
	
	/**
	 * Gets the glyphs of the given set of PhysicalEntity objects. Does not create anything.
	 *
	 * @param pes entities to get their glyphs
	 * @param glyphMap glyph repository
	 * @return glyphs of entities
	 */
	private static List<Glyph> getGlyphsOfPEs(Set<PhysicalEntity> pes, Map<String, Glyph> glyphMap)
	{
		List<Glyph> gs = new ArrayList<Glyph>();
		for (PhysicalEntity pe : pes)
		{
			if (glyphMap.containsKey(pe.getRDFId()))
			{
				gs.add(glyphMap.get(pe.getRDFId()));
			}
		}
		return gs;
	}

	/**
	 * Creates an AND glyph downstream of the given glyphs.
	 *
	 * @param gs upstream glyph list
	 * @param glyphMap glyph repository
	 * @param arcMap arc repository
	 * @return AND glyph
	 */
	private static Glyph connectWithAND(List<Glyph> gs, Map<String, Glyph> glyphMap,
		Map<String, Arc> arcMap)
	{
		// Compose an ID for the AND glyph

		String id = "";

		for (Glyph g : gs)
		{
			id = id + (id.length() > 0 ? "-AND-" : "") + g.getId();
		}

		// Create the AND glyph if not exists

		Glyph and;
		if (!glyphMap.containsKey(id))
		{
			and = factory.createGlyph();
			and.setClazz(AND);
			and.setId(id);
			glyphMap.put(and.getId(), and);
		}
		else
		{
			and = glyphMap.get(id);
		}

		// Connect upstream to the AND glyph

		for (Glyph g : gs)
		{
			createArc(g, and, LOGIC_ARC, arcMap);
		}
		return and;
	}

	/**
	 * Adds a NOT glyph next to the given glyph.
	 *
	 * @param g glyph to add NOT
	 * @param glyphMap glyph repository
	 * @param arcMap arc repository
	 * @return NOT glyph
	 */
	private static Glyph addNOT(Glyph g, Map<String, Glyph> glyphMap, Map<String, Arc> arcMap)
	{
		// Assemble an ID for the NOT glyph

		String id = "NOT-" + g.getId();

		// Find or create the NOT glyph

		Glyph not;
		if (!glyphMap.containsKey(id))
		{
			not = factory.createGlyph();
			not.setId(id);
			not.setClazz(NOT);
			glyphMap.put(not.getId(), not);
		}
		else
		{
			not = glyphMap.get(id);
		}

		// Connect the glyph and NOT
		createArc(g, not, LOGIC_ARC, arcMap);

		return not;
	}

	/**
	 * Converts the control type of the Control to the SBGN classes.
	 *
	 * @param ctrl Control to get its type
	 * @return SBGN type of the Control
	 */
	private static String getControlType(Control ctrl)
	{
		if (ctrl instanceof Catalysis)
		{
			// Catalysis has its own class
			return CATALYSIS;
		}

		ControlType type = ctrl.getControlType();
		if (type == null)
		{
			// Use stimulation as the default control type
			return STIMULATION;
		}

		// Map control type to stimulation or inhibition

		switch (type)
		{
			case ACTIVATION:
			case ACTIVATION_ALLOSTERIC:
			case ACTIVATION_NONALLOSTERIC:
			case ACTIVATION_UNKMECH: return STIMULATION;
			case INHIBITION:
			case INHIBITION_ALLOSTERIC:
			case INHIBITION_OTHER:
			case INHIBITION_UNKMECH:
			case INHIBITION_COMPETITIVE:
			case INHIBITION_IRREVERSIBLE:
			case INHIBITION_UNCOMPETITIVE:
			case INHIBITION_NONCOMPETITIVE: return INHIBITION;
		}
		throw new RuntimeException("Invalid control type: " + type);
	}

	/**
	 * Gets the size of representable Controller of this set of Controls.
	 *
	 * @param ctrlSet Controls to check their controllers
	 * @param glyphMap glyph repository
	 * @return size of representable controllers
	 */
	private static int getControllerSize(Set<Control> ctrlSet, Map<String, Glyph> glyphMap)
	{
		int size = 0;
		for (Control ctrl : ctrlSet)
		{
			size += getControllers(ctrl, glyphMap).size();
		}
		return size;
	}

	/**
	 * Gets the size of representable Controller of this Control.
	 *
	 * @param ctrl Control to check its controllers
	 * @param glyphMap glyph repository
	 * @return size of representable controllers
	 */
	private static Set<PhysicalEntity> getControllers(Control ctrl, Map<String, Glyph> glyphMap)
	{
		Set<PhysicalEntity> controllers = new HashSet<PhysicalEntity>();
		for (Controller clr : ctrl.getController())
		{
			if (clr instanceof PhysicalEntity && glyphMap.containsKey(clr.getRDFId()))
			{
				controllers.add((PhysicalEntity) clr);
			}
		}
		return controllers;
	}


	/**
	 * Adds input and output ports to the glyph.
	 *
	 * @param g glyph to add ports
	 */
	private static void addPorts(Glyph g)
	{
		Glyph.Port inputPort = factory.createGlyphPort();
		Glyph.Port outputPort = factory.createGlyphPort();
		inputPort.setId(g.getId() + ".input");
		outputPort.setId(g.getId() + ".output");
		g.getPort().add(inputPort);
		g.getPort().add(outputPort);
	}

	//-- Section: Create arcs ---------------------------------------------------------------------|

	/**
	 * Creates an arc from the source to the target, and sets its class to the specified clazz.
	 * Puts the new arc in the sullied arcMap.
	 *
	 * @param source source of the arc -- either Glyph or Port
	 * @param target target of the arc -- either Glyph or Port
	 * @param clazz class of the arc
	 * @param arcMap arc registry
	 */
	private static void createArc(Object source, Object target, String clazz,
		Map<String, Arc> arcMap)
	{
		assert source instanceof Glyph || source instanceof Glyph.Port : "source = " + source;
		assert target instanceof Glyph || target instanceof Glyph.Port : "target = " + target;

		Arc arc = factory.createArc();
		arc.setSource(source);
		arc.setTarget(target);
		arc.setClazz(clazz);
		arcMap.put(getID(arc), arc);
	}

	/**
	 * Arcs do not have an ID in libSBGN. So, we need to generate mock ID for arcs just to be able
	 * to store them in maps.
	 *
	 * @param arc arc to get its ID
	 * @return ID
	 */
	private static String getID(Arc arc)
	{
		return arc.getSource().toString() + arc.getTarget().toString();
	}

	//-- Section: Static initialization -----------------------------------------------------------|

	static
	{
		factory = new ObjectFactory();

		typeMatchMap = new HashMap<Class<? extends BioPAXElement>, String>();
		typeMatchMap.put(Protein.class, "macromolecule");
		typeMatchMap.put(SmallMolecule.class, "simple chemical");
		typeMatchMap.put(Dna.class, "nucleic acid feature");
		typeMatchMap.put(Rna.class, "nucleic acid feature");
		typeMatchMap.put(DnaRegion.class, "nucleic acid feature");
		typeMatchMap.put(RnaRegion.class, "nucleic acid feature");
		typeMatchMap.put(NucleicAcid.class, "nucleic acid feature");
		typeMatchMap.put(PhysicalEntity.class, "unspecified entity");
		typeMatchMap.put(SimplePhysicalEntity.class, "unspecified entity");
		typeMatchMap.put(Complex.class, "complex");
	}
}
