package org.biopax.paxtools.io.sbgn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.conversion.HGNC;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.sbgn.Language;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map;

import static org.sbgn.GlyphClazz.*;
import static org.sbgn.ArcClazz.*;

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
	//-- Section: Static fields -------------------------------------------------------------------|

	private static final Log log = LogFactory.getLog(L3ToSBGNPDConverter.class);

	public static final String IS_UBIQUE = "IS_UBIQUE";

	/**
	 * A matching between physical entities and SBGN classes.
	 */
	private static Map<Class<? extends BioPAXElement>, String> typeMatchMap;

	/**
	 * For creating SBGN objects.
	 */
	private static ObjectFactory factory;

	//-- Section: Instance variables --------------------------------------------------------------|

	/**
	 * This class is used for detecting ubiques.
	 */
	protected UbiqueDetector ubiqueDet;

	/**
	 * This class is used for generating short printable strings (text in info boxes) from
	 * recognized entity features.
	 */
	protected FeatureDecorator featStrGen;

	/**
	 * Flag to run a layout before writing down the sbgn.
	 */
	protected boolean doLayout;


	/**
	 * ID to glyph map.
	 */
	Map<String, Glyph> glyphMap;

	/**
	 * ID to Arc map
	 */
	Map<String, Arc> arcMap;

	/**
	 * ID to compartment map.
	 */
	Map<String, Glyph> compartmentMap;

	/**
	 * Set of ubiquitous molecules.
	 */
	Set<Glyph> ubiqueSet;

	//-- Section: Public methods ------------------------------------------------------------------|


	public L3ToSBGNPDConverter()
	{
		this(null, null, true);
	}

	public L3ToSBGNPDConverter(UbiqueDetector ubiqueDet, FeatureDecorator featStrGen,
		boolean doLayout)
	{
		this.ubiqueDet = ubiqueDet;		
		this.featStrGen = featStrGen;
		this.doLayout = doLayout;
		
		if (this.featStrGen == null)
			this.featStrGen = new CommonFeatureStringGenerator();
	}

	/**
	 * Converts the given model to SBGN, and writes in the specified file.
	 *
	 * @param model model to convert
	 * @param file file to write
	 */
	public void writeSBGN(Model model, String file)
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
	 * Converts the given model to SBGN, and writes in the specified output stream.
	 *
	 * @param model model to convert
	 * @param stream output stream to write
	 */
	public void writeSBGN(Model model, OutputStream stream)
	{
		// Create the model
		Sbgn sbgn = createSBGN(model);

		// Write in file

		try
		{
			sbgn.toString();
			JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(sbgn, stream);
		}
		catch (JAXBException e)
		{
			if (log.isErrorEnabled()) log.error(e.getCause(), e);
			e.printStackTrace(new PrintWriter(stream));
		}
	}

	/**
	 * Creates an Sbgn object from the given model.
	 *
	 * @param model model to convert to SBGN
	 * @return SBGN representation of the model
	 */
	public Sbgn createSBGN(Model model)
	{
		assert model.getLevel().equals(BioPAXLevel.L3) : "This method only supports L3 graphs";

		glyphMap = new HashMap<String, Glyph>();
		compartmentMap = new HashMap<String, Glyph>();
		arcMap = new HashMap<String, Arc>();
		ubiqueSet = new HashSet<Glyph>();

		// Create glyphs for Physical Entities

		for (PhysicalEntity entity : model.getObjects(PhysicalEntity.class))
		{
			if (needsToBeCreatedInitially(entity))
			{
				createGlyph(entity);
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
				createProcessAndConnections(conv, ConversionDirectionType.LEFT_TO_RIGHT);
			}
			else if (conv.getConversionDirection() != null &&
				(conv.getConversionDirection().equals(ConversionDirectionType.RIGHT_TO_LEFT) ||
				conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE)))
			{
				createProcessAndConnections(conv, ConversionDirectionType.RIGHT_TO_LEFT);
			}
		}

		// Create glyph for template reactions and link with arcs

		for (TemplateReaction tr : model.getObjects(TemplateReaction.class))
		{
			createProcessAndConnections(tr);
		}

		// Register created objects into sbgn construct

		Sbgn sbgn = factory.createSbgn();
		org.sbgn.bindings.Map map = new org.sbgn.bindings.Map();
		sbgn.setMap(map);
		map.setLanguage(Language.PD.toString());
		
		map.getGlyph().addAll(getRootGlyphs(glyphMap.values()));
		map.getGlyph().addAll(getRootGlyphs(ubiqueSet));
		map.getGlyph().addAll(compartmentMap.values());
		map.getArc().addAll(arcMap.values());

		if (doLayout)
		{
			/*------------------ChiLay layout modification ----------------------------*/
			//Apply Layout to SBGN objects
			SBGNLayoutManager coseLayoutManager = new SBGNLayoutManager();
			sbgn = coseLayoutManager.createLayout(sbgn);
		}
		
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
	private boolean needsToBeCreatedInitially(PhysicalEntity entity)
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
		// Ubiques will be created when they are used
		else if (ubiqueDet != null && ubiqueDet.isUbique(entity))
		{
			return false;
		}
		return true;
	}

	/**
	 * Creates a glyph representing the given PhysicalEntity.
	 *
	 * @param pe PhysicalEntity to represent
	 * @return the created glyph
	 */
	private Glyph createGlyph(PhysicalEntity pe)
	{
		if (glyphMap.containsKey(pe.getRDFId())) return glyphMap.get(pe.getRDFId());

		// Create its glyph and register

		Glyph g = createGlyphBasics(pe);
		glyphMap.put(g.getId(), g);
		if (g.getClone() != null) ubiqueSet.add(g);
		
		assignLocation(pe, g);

		// Fill-in the complex members if this is a complex

		if (pe instanceof Complex)
		{
			createComplexContent((Complex) pe);
		}

		return g;
	}

	private void assignLocation(PhysicalEntity pe, Glyph g)
	{
		// Create compartment -- add this inside the compartment

		Glyph loc = getCompartment(getCompartment(pe));
		if (loc != null) 
		{
			g.setCompartmentRef(loc);
		}
	}

	/**
	 * This method creates a glyph for the given PhysicalEntity, sets its title and state variables
	 * if applicable.
	 *
	 * @param pe PhysicalEntity to represent
	 * @return the glyph
	 */
	private Glyph createGlyphBasics(PhysicalEntity pe)
	{
		String s = typeMatchMap.get(pe.getModelInterface());

		Glyph g = factory.createGlyph();
		g.setId(pe.getRDFId());
		g.setClazz(s);

		// Set the label

		Label label = factory.createLabel();
		label.setText(findALabelForMolecule(pe));
		g.setLabel(label);

		// Detect if ubique

		if (ubiqueDet != null && ubiqueDet.isUbique(pe))
		{
			g.setClone(factory.createGlyphClone());
		}

		// Put on state variables

		List<Glyph> states = getInformation(pe);
		g.getGlyph().addAll(states);

		return g;
	}

	private Glyph getGlyphToLink(PhysicalEntity pe, String linkID)
	{
		if (ubiqueDet != null && !ubiqueDet.isUbique(pe))
		{
			return glyphMap.get(pe.getRDFId());
		}
		else
		{
			// Create a new glyph for each use of ubique
			Glyph g = createGlyphBasics(pe);
			g.setId(pe.getRDFId() + linkID);
			assignLocation(pe, g);
			ubiqueSet.add(g);
			return g;
		}
	}
	
	/**
	 * Fills in the content of a complex.
	 *
	 * @param cx Complex to be filled
	 */
	private void createComplexContent(Complex cx)
	{
		Glyph cg = glyphMap.get(cx.getRDFId());

		for (PhysicalEntity mem : cx.getComponent())
		{
			if (mem instanceof Complex)
			{
				addComplexAsMember((Complex) mem, cg);
			}
			else
			{
				createComplexMember(mem, cg);
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
	 */
	private void addComplexAsMember(Complex cx, Glyph container)
	{
		if (cx.getComponent().isEmpty())
		{
			// Put a glyph for empty inner complex
			createComplexMember(cx, container);
		}
		else
		{
			for (PhysicalEntity mem : cx.getComponent())
			{
				if (mem instanceof Complex)
				{
					// Recursive call for inner complexes
					addComplexAsMember((Complex) mem, container);
				}
				else
				{
					createComplexMember(mem, container);
				}
			}
		}
	}

	/**
	 * Creates a glyph for the complex member.
	 *
	 * @param pe PhysicalEntity to represent as complex member
	 * @param container Glyph for the complex shell
	 */
	private void createComplexMember(PhysicalEntity pe, Glyph container)
	{
		Glyph g = createGlyphBasics(pe);
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
	private String findALabelForMolecule(PhysicalEntity pe)
	{
		// Use gene symbol of PE

		for (Xref xref : pe.getXref())
		{
			String sym = extractGeneSymbol(xref);
			if (sym != null) return sym;
		}

		// Use gene symbol of ER

		EntityReference er = null;

		if (pe instanceof SimplePhysicalEntity)
		{
			er = ((SimplePhysicalEntity) pe).getEntityReference();
		}

		if (er != null)
		{
			for (Xref xref : er.getXref())
			{
				String sym = extractGeneSymbol(xref);
				if (sym != null) return sym;
			}
		}
		
		// Use display name of entity
		String name = pe.getDisplayName();

		if (name == null)
		{
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

		// Search for the shortest name of chemicals
		if (pe instanceof SmallMolecule)
		{
			String shortName = getShortestName((SmallMolecule) pe);

			if (shortName != null)
			{
				if (name == null || shortName.length() < name.length()) name = shortName;
			}
		}
		
		if (name == null)
		{
			// Don't leave it without a name
			name = "noname";
		}
		return name;
	}

	private String getShortestName(SimplePhysicalEntity spe)
	{
		String name = null;

		for (String s : spe.getName())
		{
			if (name == null || s.length() > name.length()) name = s;
		}

		EntityReference er = spe.getEntityReference();
		
		if (er != null)
		{
			for (String s : er.getName())
			{
				if (name == null || s.length() > name.length()) name = s;				
			}
		}
		return name;
	}

	private String extractGeneSymbol(Xref xref)
	{
		if (xref.getDb() != null && (
			xref.getDb().equals("HGNC") ||
			xref.getDb().equals("Gene Symbol")))
		{
			String ref = xref.getId();

			if (ref != null)
			{
				ref = ref.trim();
				if (ref.contains(":")) ref = ref.substring(ref.indexOf(":") + 1);
				if (ref.contains("_")) ref = ref.substring(ref.indexOf("_") + 1);

				if (!HGNC.containsSymbol(ref))
				{
					ref = HGNC.getSymbol(ref);
				}
			}
			return ref;
		}
		return null;
	}
	
	/**
	 * Adds molecule type, and iterates over features of the entity and creates corresponding state
	 * variables. Ignores binding features and covalent-binding features.
	 * 
	 * @param pe entity to collect features
	 * @return list of state variables
	 */
	private List<Glyph> getInformation(PhysicalEntity pe)
	{
		List<Glyph> list = new ArrayList<Glyph>();

		// Add the molecule type before states if this is a nucleic acid

		if (pe instanceof NucleicAcid)
		{
			Glyph g = factory.createGlyph();
			g.setClazz(UNIT_OF_INFORMATION.getClazz());
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
	private void extractFeatures(Set<EntityFeature> features, boolean normalFeature,
		List<Glyph> list)
	{
		for (EntityFeature feature : features)
		{
			if (feature instanceof ModificationFeature || feature instanceof FragmentFeature)
			{
				Glyph stvar = factory.createGlyph();
				stvar.setClazz(STATE_VARIABLE.getClazz());

				Glyph.State state = featStrGen.createStateVar(feature, factory);

				if (state != null)
				{
					// Add a "!" in front of NOT features

					if (!normalFeature)
					{
						state.setValue("!" + state.getValue());
					}

					stvar.setState(state);

					list.add(stvar);
				}
			}
		}
	}

	//-- Section: Create compartments -------------------------------------------------------------|

	/**
	 * Creates or gets the compartment with the given name.
	 *
	 * @param name name of the compartment
	 * @return the compartment glyph
	 */
	private Glyph getCompartment(String name)
	{
		if (name == null) return null;
		if (compartmentMap.containsKey(name)) return compartmentMap.get(name);

		Glyph comp = factory.createGlyph();
		comp.setId(name);
		Label label = factory.createLabel();
		label.setText(name);
		comp.setLabel(label);
		comp.setClazz(COMPARTMENT.getClazz());

		compartmentMap.put(name, comp);
		return comp;
	}

	/**
	 * Gets the compartment of the given PhysicalEntity.
	 *
	 * @param pe PhysicalEntity to look for its compartment
	 * @return name of compartment or null if there is none
	 */
	private String getCompartment(PhysicalEntity pe)
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
	 */
	private void createProcessAndConnections(Conversion cnv,
		ConversionDirectionType direction)
	{
		assert direction.equals(ConversionDirectionType.LEFT_TO_RIGHT) ||
			direction.equals(ConversionDirectionType.RIGHT_TO_LEFT);

		assert cnv.getConversionDirection() == null ||
			cnv.getConversionDirection().equals(direction) ||
			cnv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE);

		// create the process for the conversion in that direction

		Glyph process = factory.createGlyph();
		process.setClazz(PROCESS.getClazz());
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
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(g, process.getPort().get(0), CONSUMPTION.getClazz());
		}

		// Associate outputs to output port

		for (PhysicalEntity pe : output)
		{
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(process.getPort().get(1), g, PRODUCTION.getClazz());
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

			Glyph g = createControlStructure(ctrl);
			if (g != null) createArc(g, process, getControlType(ctrl));
		}
	}

	/**
	 * Creates a representation for TemplateReaction.
	 *
	 * @param tr template reaction
	 */
	private void createProcessAndConnections(TemplateReaction tr)
	{
		// create the process for the reaction

		Glyph process = factory.createGlyph();
		process.setClazz(PROCESS.getClazz());
		process.setId(tr.getRDFId());
		glyphMap.put(process.getId(), process);

		// Add input and output ports
		addPorts(process);

		// Create a source-and-sink as the input

		Glyph sas = factory.createGlyph();
		sas.setClazz(SOURCE_AND_SINK.getClazz());
		sas.setId("SAS_For_" + tr.getRDFId());
		glyphMap.put(sas.getId(), sas);
		createArc(sas, process.getPort().get(0), CONSUMPTION.getClazz());

		// Associate products

		for (PhysicalEntity pe : tr.getProduct())
		{
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(process.getPort().get(1), g, PRODUCTION.getClazz());
		}

		// Associate controllers

		for (Control ctrl : tr.getControlledOf())
		{
			Glyph g = createControlStructure(ctrl);
			if (g != null) createArc(g, process, getControlType(ctrl));
		}
	}

	/**
	 * Creates or gets the glyph to connect to the control arc.
	 *
	 * @param ctrl Control to represent
	 * @return glyph representing the controller tree
	 */
	private Glyph createControlStructure(Control ctrl)
	{
		Glyph cg;

		Set<PhysicalEntity> controllers = getControllers(ctrl);

		// If no representable controller found, skip this control
		if (controllers.isEmpty()) cg = null;

		// If there is only one controller with no modulator, put an arc for controller

		else if (controllers.size() == 1 && getControllerSize(ctrl.getControlledOf()) == 0)
		{
			cg = getGlyphToLink(controllers.iterator().next(), ctrl.getRDFId());
		}

		else
		{
			// This list will contain handles for each participant of the AND structure
			List<Glyph> toConnect = new ArrayList<Glyph>();

			// Bundle controllers if necessary

			Glyph gg = handlePEGroup(controllers, ctrl.getRDFId());
			if(gg != null)
				toConnect.add(gg);

			// Create handles for each controller

			for (Control ctrl2 : ctrl.getControlledOf())
			{
				Glyph g = createControlStructure(ctrl2);
				if (g != null)
				{
					// If the control is negative, add a NOT in front of it

					if (getControlType(ctrl2).equals(INHIBITION))
					{
						g = addNOT(g);
					}

					toConnect.add(g);
				}
			}

			// Handle co-factors of catalysis

			if (ctrl instanceof Catalysis)
			{
				Set<PhysicalEntity> cofs = ((Catalysis) ctrl).getCofactor();
				Glyph g = handlePEGroup(cofs, ctrl.getRDFId());
				if (g != null) 
					toConnect.add(g);
			}

			if (toConnect.isEmpty()) 
				return null;
			else if (toConnect.size() == 1)
			{
				cg = toConnect.iterator().next();
			}
			else
			{
				cg = connectWithAND(toConnect);
			}
		}

		return cg;
	}

	/**
	 * Prepares the necessary construct for adding the given PhysicalEntity set to the Control
	 * being drawn.
	 *
	 * @param pes entities to use in control
	 * @return the glyph to connect to the appropriate place
	 */
	private Glyph handlePEGroup(Set<PhysicalEntity> pes, String context)
	{
		int sz = pes.size();		
		if (sz > 1)
		{
			List<Glyph> gs = getGlyphsOfPEs(pes, context);
			return connectWithAND(gs);
		}
		else if (sz == 1 && glyphMap.containsKey(pes.iterator().next().getRDFId()))
		{
			return getGlyphToLink(pes.iterator().next(), context);
		}
		
		//'pes' was empty
		return null;
	}
	
	/**
	 * Gets the glyphs of the given set of PhysicalEntity objects. Does not create anything.
	 *
	 * @param pes entities to get their glyphs
	 * @return glyphs of entities
	 */
	private List<Glyph> getGlyphsOfPEs(Set<PhysicalEntity> pes, String context)
	{
		List<Glyph> gs = new ArrayList<Glyph>();
		for (PhysicalEntity pe : pes)
		{
			if (glyphMap.containsKey(pe.getRDFId()))
			{
				gs.add(getGlyphToLink(pe, context));
			}
		}
		return gs;
	}

	/**
	 * Creates an AND glyph downstream of the given glyphs.
	 *
	 * @param gs upstream glyph list
	 * @return AND glyph
	 */
	private Glyph connectWithAND(List<Glyph> gs)
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
			and.setClazz(AND.getClazz());
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
			createArc(g, and, LOGIC_ARC.getClazz());
		}
		return and;
	}

	/**
	 * Adds a NOT glyph next to the given glyph.
	 *
	 * @param g glyph to add NOT
	 * @return NOT glyph
	 */
	private Glyph addNOT(Glyph g)
	{
		// Assemble an ID for the NOT glyph

		String id = "NOT-" + g.getId();

		// Find or create the NOT glyph

		Glyph not;
		if (!glyphMap.containsKey(id))
		{
			not = factory.createGlyph();
			not.setId(id);
			not.setClazz(NOT.getClazz());
			glyphMap.put(not.getId(), not);
		}
		else
		{
			not = glyphMap.get(id);
		}

		// Connect the glyph and NOT
		createArc(g, not, LOGIC_ARC.getClazz());

		return not;
	}

	/**
	 * Converts the control type of the Control to the SBGN classes.
	 *
	 * @param ctrl Control to get its type
	 * @return SBGN type of the Control
	 */
	private String getControlType(Control ctrl)
	{
		if (ctrl instanceof Catalysis)
		{
			// Catalysis has its own class
			return CATALYSIS.getClazz();
		}

		ControlType type = ctrl.getControlType();
		if (type == null)
		{
			// Use stimulation as the default control type
			return STIMULATION.getClazz();
		}

		// Map control type to stimulation or inhibition

		switch (type)
		{
			case ACTIVATION:
			case ACTIVATION_ALLOSTERIC:
			case ACTIVATION_NONALLOSTERIC:
			case ACTIVATION_UNKMECH: return STIMULATION.getClazz();
			case INHIBITION:
			case INHIBITION_ALLOSTERIC:
			case INHIBITION_OTHER:
			case INHIBITION_UNKMECH:
			case INHIBITION_COMPETITIVE:
			case INHIBITION_IRREVERSIBLE:
			case INHIBITION_UNCOMPETITIVE:
			case INHIBITION_NONCOMPETITIVE: return INHIBITION.getClazz();
		}
		throw new RuntimeException("Invalid control type: " + type);
	}

	/**
	 * Gets the size of representable Controller of this set of Controls.
	 *
	 * @param ctrlSet Controls to check their controllers
	 * @return size of representable controllers
	 */
	private int getControllerSize(Set<Control> ctrlSet)
	{
		int size = 0;
		for (Control ctrl : ctrlSet)
		{
			size += getControllers(ctrl).size();
		}
		return size;
	}

	/**
	 * Gets the size of representable Controller of this Control.
	 *
	 * @param ctrl Control to check its controllers
	 * @return size of representable controllers
	 */
	private Set<PhysicalEntity> getControllers(Control ctrl)
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
	private void addPorts(Glyph g)
	{
		Port inputPort = factory.createPort();
		Port outputPort = factory.createPort();
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
	 */
	private void createArc(Object source, Object target, String clazz)
	{
		assert source instanceof Glyph || source instanceof Port : "source = " + source;
		assert target instanceof Glyph || target instanceof Port : "target = " + target;

		Arc arc = factory.createArc();
		arc.setSource(source);
		arc.setTarget(target);
		arc.setClazz(clazz);
		
		String sourceID = source instanceof Glyph ?
			((Glyph) source).getId() : ((Port) source).getId();
		String targetID = target instanceof Glyph ?
			((Glyph) target).getId() : ((Port) target).getId();

		arc.setId(sourceID + "--to--" + targetID);
		arcMap.put(arc.getId(), arc);
	}

	/**
	 * Collects root-level glyphs in the given glyph collection.
	 *
	 * @param glyphCol glyph collection to search
	 * @return set of roots
	 */
	private Set<Glyph> getRootGlyphs(Collection<Glyph> glyphCol)
	{
		Set<Glyph> root = new HashSet<Glyph>(glyphCol);
		Set<Glyph> children = new HashSet<Glyph>();

		for (Glyph glyph : glyphCol)
		{
			addChildren(glyph, children);
		}
		root.removeAll(children);
		return root;
	}

	/**
	 * Adds children of this glyph to the specified set recursively.
	 * @param glyph to collect children
	 * @param set to add
	 */
	private void addChildren(Glyph glyph, Set<Glyph> set)
	{
		for (Glyph child : glyph.getGlyph())
		{
			set.add(child);
			addChildren(child, set);
		}
	}
	
	//-- Section: Static initialization -----------------------------------------------------------|

	static
	{
		factory = new ObjectFactory();

		typeMatchMap = new HashMap<Class<? extends BioPAXElement>, String>();
		typeMatchMap.put(Protein.class, MACROMOLECULE.getClazz());
		typeMatchMap.put(SmallMolecule.class, SIMPLE_CHEMICAL.getClazz());
		typeMatchMap.put(Dna.class, NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(Rna.class, NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(DnaRegion.class, NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(RnaRegion.class, NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(NucleicAcid.class, NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(PhysicalEntity.class, UNSPECIFIED_ENTITY.getClazz());
		typeMatchMap.put(SimplePhysicalEntity.class, UNSPECIFIED_ENTITY.getClazz());
		typeMatchMap.put(Complex.class, COMPLEX.getClazz());
	}
}
