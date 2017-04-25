package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.io.sbgn.idmapping.HGNC;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.sbgn.Language;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map;

import static org.sbgn.GlyphClazz.*;
import static org.sbgn.ArcClazz.*;

/**
 * This class converts BioPAX L3 model into SBGN PD.
 * It optionally applies COSE layout.
 *
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
	private static final Logger log = LoggerFactory.getLogger(L3ToSBGNPDConverter.class);

	/**
	 * Ubique label.
	 */
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
	 * Mapping from SBGN IDs to the IDs of the related objects in BioPAX.
	 */
	protected Map<String, Set<String>> sbgn2BPMap;

	/**
	 * Option to flatten nested complexes.
	 */
	protected boolean flattenComplexContent;

	/**
	 * SBGN process glyph can be used to show reversible reactions. In that case two ports of the
	 * process will only have product glyphs. However, this creates an incompatibility with BioPAX:
	 * reversible biochemical reactions can have catalysis with a direction. But if we use a single
	 * glyph for the process and direct that catalysis to it, then the direction of the catalysis
	 * will be lost. If we use two process nodes for the reversible reaction (one for left-to-right
	 * and another for right-to-left), then we can direct the directed catalysis to only the
	 * relevant process glyph.
	 *
	 * Also note that the layout do not support ports. If layout is run, the ports are removed. In
	 * that case it will be impossible to distinguish left and right components of the reversible
	 * process glyph because they will all be product edges.
	 */
	protected boolean useTwoGlyphsForReversibleConversion;

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

	/**
	 * Empty constructor.
	 */
	public L3ToSBGNPDConverter()
	{
		this(null, null, true);
	}

	/**
	 * Constructor with parameters.
	 * @param ubiqueDet Ubique detector class
	 * @param featStrGen feature string generator class
	 * @param doLayout whether we want to perform layout after SBGN creation.
	 */
	public L3ToSBGNPDConverter(UbiqueDetector ubiqueDet, FeatureDecorator featStrGen,
		boolean doLayout)
	{
		this.ubiqueDet = ubiqueDet;		
		this.featStrGen = (featStrGen != null) ? featStrGen : new CommonFeatureStringGenerator();
		this.doLayout = doLayout;
		this.useTwoGlyphsForReversibleConversion = true;
		this.sbgn2BPMap = new HashMap<String, Set<String>>();
		this.flattenComplexContent = true;
	}

	/**
	 * Getter class for the parameter useTwoGlyphsForReversibleConversion.
	 * @return whether use two glyphs for the reversible conversion
	 */
	public boolean isUseTwoGlyphsForReversibleConversion()
	{
		return useTwoGlyphsForReversibleConversion;
	}

	/**
	 * Sets the option to use two glyphs for the reversible conversion.
	 * @param useTwoGlyphsForReversibleConversion give true if use two glyphs
	 */
	public void setUseTwoGlyphsForReversibleConversion(boolean useTwoGlyphsForReversibleConversion)
	{
		this.useTwoGlyphsForReversibleConversion = useTwoGlyphsForReversibleConversion;
	}

	public boolean isFlattenComplexContent()
	{
		return flattenComplexContent;
	}

	public void setFlattenComplexContent(boolean flattenComplexContent)
	{
		this.flattenComplexContent = flattenComplexContent;
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
		try {
			SbgnUtil.writeToFile(sbgn, new File(file));
		}
		catch (JAXBException e) {
			throw new RuntimeException("writeSBGN, SbgnUtil.writeToFile failed", e);
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
		Sbgn sbgn = createSBGN(model);

		try {
			JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(sbgn, stream);
		}
		catch (JAXBException e) {
			throw new RuntimeException("writeSBGN: JAXB marshalling failed", e);
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
				(conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE) &&
				useTwoGlyphsForReversibleConversion))
			{
				createProcessAndConnections(conv, ConversionDirectionType.LEFT_TO_RIGHT);
			}

			if (conv.getConversionDirection() != null &&
				(conv.getConversionDirection().equals(ConversionDirectionType.RIGHT_TO_LEFT) ||
				(conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE)) &&
				useTwoGlyphsForReversibleConversion))
			{
				createProcessAndConnections(conv, ConversionDirectionType.RIGHT_TO_LEFT);
			}

			if (conv.getConversionDirection() != null &&
				conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE) &&
				!useTwoGlyphsForReversibleConversion)
			{
				createProcessAndConnections(conv, ConversionDirectionType.REVERSIBLE);
			}
		}

		// Create glyph for template reactions and link with arcs
		for (TemplateReaction tr : model.getObjects(TemplateReaction.class)) {
			createProcessAndConnections(tr);
		}

		// Register created objects into sbgn construct

		final Sbgn sbgn = factory.createSbgn();
		org.sbgn.bindings.Map map = new org.sbgn.bindings.Map();
		sbgn.setMap(map);
		map.setLanguage(Language.PD.toString());
		map.getGlyph().addAll(getRootGlyphs(glyphMap.values()));
		map.getGlyph().addAll(getRootGlyphs(ubiqueSet));
		map.getGlyph().addAll(compartmentMap.values());
		map.getArc().addAll(arcMap.values());

		if (doLayout && sbgn.getMap().getGlyph().size() < 1000) { //TODO: always skip layout for graphs >1000 glyphs?
			(new SBGNLayoutManager()).createLayout(sbgn);
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
		boolean ret = true; //means - do create a node

		if (entity instanceof Complex) {
			Complex c = (Complex) entity;
			if (c.getParticipantOf().isEmpty() && !c.getComponentOf().isEmpty()) {
				// Inner complex will be created during creation of the top complex
				ret = false;
			}
		}
		else if (entity.getParticipantOf().isEmpty() && !entity.getComponentOf().isEmpty()) {
			// Complex members will be created during creation of parent complex
			ret = false;
		}
		else if(entity.getParticipantOf().isEmpty() && entity.getComponentOf().isEmpty()) {
			// won't create a node for either a dangling, experimental form entity, or memberPhysicalEntity
			if(!entity.getMemberPhysicalEntityOf().isEmpty())
				log.debug("skip a memberPhysicalEntity (- also not a participant/component of another entity): "
						+ entity.getUri());
			else
				log.debug("skip a dangling or experimental form phys. entity: " + entity.getUri());

			ret = false;
		}
		else if (ubiqueDet != null && ubiqueDet.isUbique(entity)) {
			// Ubiques will be created when they are used
			ret = false;
		}

		return ret;
	}

	/**
	 * Creates a glyph representing the given PhysicalEntity.
	 *
	 * @param pe PhysicalEntity to represent
	 * @return the created glyph
	 */
	private Glyph createGlyph(PhysicalEntity pe)
	{
		String id = convertID(pe.getUri());
		if (glyphMap.containsKey(id)) return glyphMap.get(id);

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

	/**
	 * Assigns compartmentRef of the glyph.
	 * @param pe Related PhysicalEntity
	 * @param g the glyph
	 */
	private void assignLocation(PhysicalEntity pe, Glyph g)
	{
		// Create compartment -- add this inside the compartment
		Glyph loc = getCompartment(pe);
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
		return createGlyphBasics(pe, true);
	}
	/**
	 * This method creates a glyph for the given PhysicalEntity, sets its title and state variables
	 * if applicable.
	 *
	 * @param pe PhysicalEntity to represent
	 * @param idIsFinal if ID is final, then it is recorded for future reference
	 * @return the glyph
	 */
	private Glyph createGlyphBasics(PhysicalEntity pe, boolean idIsFinal)
	{
		String s = typeMatchMap.get(pe.getModelInterface());

		Glyph g = factory.createGlyph();
		g.setId(convertID(pe.getUri()));
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

		// Record the mapping
		if (idIsFinal)
		{
			sbgn2BPMap.put(g.getId(), new HashSet<String>());
			sbgn2BPMap.get(g.getId()).add(pe.getUri());
		}
		return g;
	}

	/**
	 * Gets the representing glyph of the PhysicalEntity.
	 * @param pe PhysicalEntity to get its glyph
	 * @param linkID Edge id, used if the PhysicalEntity is ubique
	 * @return Representing glyph
	 */
	private Glyph getGlyphToLink(PhysicalEntity pe, String linkID)
	{
		if (ubiqueDet == null || !ubiqueDet.isUbique(pe))
		{
			return glyphMap.get(convertID(pe.getUri()));
		}
		else
		{
			// Create a new glyph for each use of ubique
			Glyph g = createGlyphBasics(pe, false);
			g.setId(convertID(pe.getUri()) + linkID);

			sbgn2BPMap.put(g.getId(), new HashSet<String>());
			sbgn2BPMap.get(g.getId()).add(pe.getUri());

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
		Glyph cg = glyphMap.get(convertID(cx.getUri()));

		if (flattenComplexContent)
		{
			for (PhysicalEntity mem : getFlattenedMembers(cx))
			{
				createComplexMember(mem, cg);
			}
		}
		else
		{
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
		// Create a glyph for the inner complex
		Glyph inner = createComplexMember(cx, container);

		for (PhysicalEntity mem : cx.getComponent())
		{
			if (mem instanceof Complex)
			{
				// Recursive call for inner complexes
				addComplexAsMember((Complex) mem, inner);
			}
			else
			{
				createComplexMember(mem, inner);
			}
		}
	}

	/**
	 * Gets the members of the Complex that needs to be displayed in a flattened view.
	 * @param cx to get members
	 * @return members to display
	 */
	private Set<PhysicalEntity> getFlattenedMembers(Complex cx)
	{
		Set<PhysicalEntity> set = new HashSet<PhysicalEntity>();

		for (PhysicalEntity mem : cx.getComponent())
		{
			if (mem instanceof Complex)
			{
				if (!hasNonComplexMember((Complex) mem))
				{
					set.add(mem);
				}
				else
				{
					set.addAll(getFlattenedMembers((Complex) mem));
				}
			}
			else set.add(mem);
		}
		return set;
	}

	/**
	 * Checks if a Complex contains any PhysicalEntity member which is not a Complex.
	 * @param cx to check
	 * @return true if there is a non-complex member
	 */
	private boolean hasNonComplexMember(Complex cx)
	{
		for (PhysicalEntity mem : cx.getComponent())
		{
			if (! (mem instanceof Complex)) return true;
			else
			{
				if (hasNonComplexMember((Complex) mem)) return true;
			}
		}
		return false;
	}

	/**
	 * Creates a glyph for the complex member.
	 *
	 * @param pe PhysicalEntity to represent as complex member
	 * @param container Glyph for the complex shell
	 */
	private Glyph createComplexMember(PhysicalEntity pe, Glyph container)
	{
		Glyph g = createGlyphBasics(pe, false);
		container.getGlyph().add(g);

		// A PhysicalEntity may appear in many complexes -- we identify the member using its complex
		g.setId(g.getId() + "_" + container.getId());

		glyphMap.put(g.getId(), g);

		sbgn2BPMap.put(g.getId(), new HashSet<String>());
		sbgn2BPMap.get(g.getId()).add(pe.getUri());

		return g;
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

		if (name == null || name.trim().isEmpty())
		{
			if (er != null)
			{
				// Use display name of reference
				name = er.getDisplayName();
			}

			if (name == null || name.trim().isEmpty())
			{
				// Use standard name of entity
				name = pe.getStandardName();

				if (name == null || name.trim().isEmpty())
				{
					if (er != null)
					{
						// Use standard name of reference
						name = er.getStandardName();
					}

					if (name == null || name.trim().isEmpty())
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
				if (name == null || (shortName.length() < name.length() &&
					!shortName.isEmpty()))
				{
					name = shortName;
				}
			}
		}
		
		if (name == null || name.trim().isEmpty())
		{
			// Don't leave it without a name
			name = "noname";
		}
		return name;
	}

	/**
	 * Searches for the shortest name of the PhysicalEntity.
	 * @param spe entity to search in
	 * @return the shortest name
	 */
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

	/**
	 * Searches for gene symbol in Xref.
	 * @param xref Xref to search
	 * @return gene symbol
	 */
	private String extractGeneSymbol(Xref xref)
	{
		if (xref.getDb() != null && (
			xref.getDb().equalsIgnoreCase("HGNC Symbol") ||
			xref.getDb().equalsIgnoreCase("Gene Symbol") ||
			xref.getDb().equalsIgnoreCase("HGNC")))
		{
			String ref = xref.getId();

			if (ref != null)
			{
				ref = ref.trim();
				if (ref.contains(":")) ref = ref.substring(ref.indexOf(":") + 1);
				if (ref.contains("_")) ref = ref.substring(ref.indexOf("_") + 1);

				// if the reference is an HGNC ID, then convert it to a symbol
				if (!HGNC.containsSymbol(ref) && Character.isDigit(ref.charAt(0)))
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
			Label label = factory.createLabel();
			String s = "mt:";
			s += ((pe instanceof Dna || pe instanceof DnaRegion) ? "DNA" :
				(pe instanceof Rna || pe instanceof RnaRegion) ? "RNA" : "NuclAc");
			label.setText(s);
			g.setLabel(label);
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

	private Glyph getCompartment(String name)
	{
		if (name == null)
			return null;

		name = name.toLowerCase();

		if (compartmentMap.containsKey(name))
			return compartmentMap.get(name);

		Glyph comp = factory.createGlyph();
		comp.setId(convertID(name));
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
	private Glyph getCompartment(PhysicalEntity pe)
	{
		CellularLocationVocabulary cl = pe.getCellularLocation();
		if (cl != null && !cl.getTerm().isEmpty())
		{
			String name = null;
			// get a cv term,
			// ignoring IDs (should not be there but happens)
			for(String term : cl.getTerm()) {
				term = term.toLowerCase();
				if(!term.matches("(go|so|mi|bto|cl|pato|mod):")) {
				 name = term;
				 break;
				}
			}
			return getCompartment(name);
		} else
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
		assert cnv.getConversionDirection() == null ||
			cnv.getConversionDirection().equals(direction) ||
			cnv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE);

		// create the process for the conversion in that direction

		Glyph process = factory.createGlyph();
		process.setClazz(PROCESS.getClazz());
		process.setId(convertID(cnv.getUri()) + direction);
		glyphMap.put(process.getId(), process);

		// Determine input and output sets
		
		Set<PhysicalEntity> input = direction.equals(ConversionDirectionType.RIGHT_TO_LEFT) ?
			cnv.getRight() : cnv.getLeft();
		Set<PhysicalEntity> output = direction.equals(ConversionDirectionType.RIGHT_TO_LEFT) ?
			cnv.getLeft() : cnv.getRight();

		// Create input and outputs ports for the process
		addPorts(process);

		Map<PhysicalEntity, Stoichiometry> stoic = getStoichiometry(cnv);

		// Associate inputs to input port

		for (PhysicalEntity pe : input)
		{
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(g, process.getPort().get(0), direction == ConversionDirectionType.REVERSIBLE ?
				PRODUCTION.getClazz() : CONSUMPTION.getClazz(), stoic.get(pe));
		}

		// Associate outputs to output port

		for (PhysicalEntity pe : output)
		{
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(process.getPort().get(1), g, PRODUCTION.getClazz(), stoic.get(pe));
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
						// Skip
						continue;
					}
				}
			}

			Glyph g = createControlStructure(ctrl);
			if (g != null) createArc(g, process, getControlType(ctrl), null);
		}

		// Record mapping

		sbgn2BPMap.put(process.getId(), new HashSet<String>());
		sbgn2BPMap.get(process.getId()).add(cnv.getUri());
	}

	/**
	 * Gets the map of stoichiometry coefficients of participants.
	 * @param conv the conversion
	 * @return map from physical entities to their stoichiometry
	 */
	private Map<PhysicalEntity, Stoichiometry> getStoichiometry(Conversion conv)
	{
		Map<PhysicalEntity, Stoichiometry> map = new HashMap<PhysicalEntity, Stoichiometry>();
		for (Stoichiometry stoc : conv.getParticipantStoichiometry())
		{
			map.put(stoc.getPhysicalEntity(), stoc);
		}
		return map;
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
		process.setId(convertID(tr.getUri()));
		glyphMap.put(process.getId(), process);

		// Add input and output ports
		addPorts(process);

		// Create a source-and-sink as the input

		Glyph sas = factory.createGlyph();
		sas.setClazz(SOURCE_AND_SINK.getClazz());
		sas.setId("SAS_For_" + process.getId());
		glyphMap.put(sas.getId(), sas);
		createArc(sas, process.getPort().get(0), CONSUMPTION.getClazz(), null);

		// Associate products

		for (PhysicalEntity pe : tr.getProduct())
		{
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(process.getPort().get(1), g, PRODUCTION.getClazz(), null);
		}

		// Associate controllers

		for (Control ctrl : tr.getControlledOf())
		{
			Glyph g = createControlStructure(ctrl);
			if (g != null) createArc(g, process, getControlType(ctrl), null);
		}

		// Record mapping

		sbgn2BPMap.put(process.getId(), new HashSet<String>());
		sbgn2BPMap.get(process.getId()).add(tr.getUri());
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
			cg = getGlyphToLink(controllers.iterator().next(), convertID(ctrl.getUri()));
		}

		else
		{
			// This list will contain handles for each participant of the AND structure
			List<Glyph> toConnect = new ArrayList<Glyph>();

			// Bundle controllers if necessary

			Glyph gg = handlePEGroup(controllers, convertID(ctrl.getUri()));
			if(gg != null)
				toConnect.add(gg);

			// Create handles for each controller

			for (Control ctrl2 : ctrl.getControlledOf())
			{
				Glyph g = createControlStructure(ctrl2);
				if (g != null)
				{
					// If the control is negative, add a NOT in front of it

					if (getControlType(ctrl2).equals(INHIBITION.getClazz()))
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
				Glyph g = handlePEGroup(cofs, convertID(ctrl.getUri()));
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
		else if (sz == 1 && glyphMap.containsKey(convertID(pes.iterator().next().getUri())))
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
			if (glyphMap.containsKey(convertID(pe.getUri())))
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
			createArc(g, and, LOGIC_ARC.getClazz(), null);
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
		createArc(g, not, LOGIC_ARC.getClazz(), null);

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
			if (clr instanceof PhysicalEntity && glyphMap.containsKey(convertID(clr.getUri())))
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
		inputPort.setId(g.getId() + "-input");
		outputPort.setId(g.getId() + "-output");
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
	private void createArc(Object source, Object target, String clazz, Stoichiometry stoic)
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

		if (stoic != null && stoic.getStoichiometricCoefficient() != 1F)
		{
			Glyph card = factory.createGlyph();
			card.setClazz(CARDINALITY.getClazz());
			Label label = factory.createLabel();
			label.setText(new DecimalFormat("0.##").format(stoic.getStoichiometricCoefficient()));
			card.setLabel(label);
			arc.getGlyph().add(card);
		}

		Arc.Start start = new Arc.Start();
		start.setX(0);
		start.setY(0);
		arc.setStart(start);

		Arc.End end = new Arc.End();
		end.setX(0);
		end.setY(0);
		arc.setEnd(end);

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

	/**
	 * Gets the mapping from SBGN IDs to BioPAX IDs. This mapping is currently one-to-many, but has
	 * potential to become many-to-many in the future.
	 * @return sbgn-to-biopax mapping
	 */
	public Map<String, Set<String>> getSbgn2BPMap()
	{
		return sbgn2BPMap;
	}


	private String convertID(String id)
	{
		return id.replaceAll("[^-\\w]", "_");
	}


	//-- Section: Static initialization -----------------------------------------------------------|

	/**
	 * Initializes resources.
	 */
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
