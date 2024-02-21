package org.biopax.paxtools.io.sbgn;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.util.HGNC;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.sbgn.ArcClazz;
import org.sbgn.GlyphClazz;
import org.sbgn.Language;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map;

/**
 * This class converts BioPAX L3 model into SBGN PD (SBGN-ML XML).
 * It optionally applies a special COSE layout.
 *
 * Currently, this converter ignores several BioPAX types/properties:
 * <ul>
 * <li>Parent-child relationship between entity references</li>
 * <li>Binding features and covalent binding features of physical entities</li>
 * <li>Pathway, PathwayStep, Evidence, etc.</li>
 * </ul>
 *
 * Also note that:
 * <ul>
 * <li>Compartment is just a controlled vocabulary in BioPAX, so nesting and neighborhood relations
 * between compartments are not handled here.</li>
 * <li>Control structures in BioPAX and in SBGN PD are a little different. We use logical
 * glyphs and arcs to approximate controls in BioPAX. However, in fact,
 * BioPAX does not imply a logical operator between controllers.</li>
 * </ul>
 *
 * @author Ozgun Babur
 */
public class L3ToSBGNPDConverter {
	private static final Logger log = LoggerFactory.getLogger(L3ToSBGNPDConverter.class);

	/*
	 * A matching between physical entities and SBGN classes.
	 */
	private static Map<Class<? extends BioPAXElement>, String> typeMatchMap;

	/*
	 * For creating SBGN objects.
	 */
	private static ObjectFactory factory;

	/*
	 * This class is used for detecting ubiques.
	 */
	protected UbiqueDetector ubiqueDet;

	/*
	 * This class is used for generating short printable strings (text in info boxes) from
	 * recognized entity features.
	 */
	protected FeatureDecorator featStrGen;

	/*
	 * Flag to run a layout before writing down the sbgn.(memberPhysicalEntity)
	 */
	protected boolean doLayout;

	/*
	 * If the number of nodes (biological processes and participants) in the model
	 * is going to be greater than this maximum, then no layout (but a trivial one)
	 * will be applied.
	 */
	protected int maxNodes;

	/*
	 * Mapping from SBGN IDs to the IDs of the related objects in BioPAX.
	 */
	protected Map<String, Set<String>> sbgn2BPMap;

	/*
	 * Option to flatten nested complexes.
	 */
	protected boolean flattenComplexContent;

	/*
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

	/*
	 * ID to glyph map.
	 */
	Map<String, Glyph> glyphMap;

	/*
	 * ID to Arc map
	 */
	Map<String, Arc> arcMap;

	/*
	 * ID to compartment map.
	 */
	Map<String, Glyph> compartmentMap;

	/*
	 * Set of ubiquitous molecules.
	 */
	Set<Glyph> ubiqueSet;

//	private static Document biopaxMetaDoc; //see issue #40

	static {
		factory = new ObjectFactory();
		typeMatchMap = new HashMap<>();
		typeMatchMap.put(Protein.class, GlyphClazz.MACROMOLECULE.getClazz());
		typeMatchMap.put(SmallMolecule.class, GlyphClazz.SIMPLE_CHEMICAL.getClazz());
		typeMatchMap.put(Dna.class, GlyphClazz.NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(Rna.class, GlyphClazz.NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(DnaRegion.class, GlyphClazz.NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(RnaRegion.class, GlyphClazz.NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(NucleicAcid.class, GlyphClazz.NUCLEIC_ACID_FEATURE.getClazz());
		typeMatchMap.put(PhysicalEntity.class, GlyphClazz.UNSPECIFIED_ENTITY.getClazz());
		typeMatchMap.put(Complex.class, GlyphClazz.COMPLEX.getClazz());
		typeMatchMap.put(Gene.class, GlyphClazz.NUCLEIC_ACID_FEATURE.getClazz());

//		//a document for adding metadata elements to insert into SBGN-ML PD glyphs (inside Extensions/Notes element).
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		try {
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			db.reset();
//			biopaxMetaDoc = db.newDocument();
//		} catch (ParserConfigurationException e) {
//			throw new RuntimeException("Cannot initialize BioPAX extensions DOM.", e);
//		}
	}

	/**
	 * Constructor.
	 */
	public L3ToSBGNPDConverter() {
		this(null, null, false);
	}

	/**
	 * Constructor with parameters.
	 * @param ubiqueDet Ubique detector class
	 * @param featStrGen feature string generator class
	 * @param doLayout whether we want to perform layout after SBGN creation.
	 */
	public L3ToSBGNPDConverter(UbiqueDetector ubiqueDet, FeatureDecorator featStrGen, boolean doLayout)
	{
		this.ubiqueDet = ubiqueDet;
		this.featStrGen = (featStrGen != null) ? featStrGen : new CommonFeatureStringGenerator();
		this.doLayout = doLayout;
		this.useTwoGlyphsForReversibleConversion = true;
		this.sbgn2BPMap = new HashMap<>();
		this.flattenComplexContent = true;
		this.maxNodes = 1000;
	}

	public void setDoLayout(boolean doLayout) {
		this.doLayout = doLayout;
	}

	/**
	 * Getter class for the parameter useTwoGlyphsForReversibleConversion.
	 * @return whether use two glyphs for the reversible conversion
	 */
	public boolean isUseTwoGlyphsForReversibleConversion() {
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

	public boolean isFlattenComplexContent() {
		return flattenComplexContent;
	}

	public void setFlattenComplexContent(boolean flattenComplexContent) {
		this.flattenComplexContent = flattenComplexContent;
	}

	/**
	 * Converts the given model to SBGN, and writes in the specified file.
	 *
	 * @param model model to convert
	 * @param file file to write
	 */
	public void writeSBGN(Model model, String file) {
		try {
			Sbgn sbgn = createSBGN(model);
			SbgnUtil.writeToFile(sbgn, new File(file));
		} catch (Exception e) {
			throw new RuntimeException("writeSBGN failed", e);
		}
	}

	/**
	 * Converts the given model to SBGN, and writes in the specified output stream.
	 *
	 * @param model model to convert
	 * @param stream output stream to write
	 */
	public void writeSBGN(Model model, OutputStream stream) {
		Sbgn sbgn = createSBGN(model);
		try {
			JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(sbgn, stream);
		} catch (JAXBException e) {
			throw new RuntimeException("writeSBGN: JAXB marshalling failed", e);
		}
	}

	/**
	 * Creates an SBGN object from the given BioPAX L3 model.
	 *
	 * Currently, it converts physical entities (interaction participants)
	 * and Conversion, Control, including their sub-classes, TemplateReaction,
	 * MolecularInteraction, GeneticInteraction, and base Interaction.class types.
	 * I does not convert other BioPAX entities and utility classes, such as:
	 * Pathway, Evidence, PathwayStep.
	 *
	 * @param model model to convert to SBGN
	 * @return SBGN representation of the BioPAX model
	 */
	public Sbgn createSBGN(Model model) {
		assert model.getLevel().equals(BioPAXLevel.L3) : "This method only supports L3 graphs";

		glyphMap = new HashMap<>();
		compartmentMap = new HashMap<>();
		arcMap = new HashMap<>();
		ubiqueSet = new HashSet<>();

		int n = 0; //approximate number of SBGN nodes

		// Create glyphs for Physical Entities
		for (Entity entity : model.getObjects(Entity.class)) {
			if (needsToBeCreatedInitially(entity)) {
				createGlyph(entity);
				++n;
			}
		}

		// Create glyph for conversions and link with arcs
		for (Interaction interaction : model.getObjects(Interaction.class))
		{
			if(interaction.getParticipant().isEmpty())
				continue;

			// For each conversion we check if we need to create a left-to-right and/or right-to-left process.
			if(interaction instanceof Conversion) {
				Conversion conv = (Conversion) interaction;
				if (conv.getConversionDirection() == null ||
						conv.getConversionDirection().equals(ConversionDirectionType.LEFT_TO_RIGHT) ||
						(conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE) &&
								useTwoGlyphsForReversibleConversion)) {
					createProcessAndConnections(conv, ConversionDirectionType.LEFT_TO_RIGHT);
				} else if (conv.getConversionDirection() != null &&
						(conv.getConversionDirection().equals(ConversionDirectionType.RIGHT_TO_LEFT) ||
								(conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE)) &&
										useTwoGlyphsForReversibleConversion)) {
					createProcessAndConnections(conv, ConversionDirectionType.RIGHT_TO_LEFT);
				} else if (conv.getConversionDirection() != null &&
						conv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE) &&
						!useTwoGlyphsForReversibleConversion) {
					createProcessAndConnections(conv, ConversionDirectionType.REVERSIBLE);
				}
			} else if(interaction instanceof TemplateReaction) {
				createProcessAndConnections((TemplateReaction) interaction);
			} else if(interaction instanceof MolecularInteraction) {
				createBasicProcess(interaction);
			} else if(interaction instanceof GeneticInteraction) {
				createGiProcess((GeneticInteraction) interaction);
			} else if(!(interaction instanceof Control)) {
				createBasicProcess(interaction);
			} else { //a Control, special case
				Control control = (Control) interaction;
				//a Control without controlled process but with controller and is controlledOf
				if(control.getControlled().isEmpty()) {
					Glyph g = createControlStructure(control);
					processControllers(control.getControlledOf(), g);
				}//else - do nothing - as it's converted anyway when the controlled interactions are processed
			}

			++n;
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

//    //Store some metadata within the standard SBGN-ML extension element: Notes
//		biopaxMetaDoc.setDocumentURI(model.getUri()); //can be null
//		SBGNBase.Notes modelNotes = new SBGNBase.Notes();
//		sbgn.setNotes(modelNotes);
//		Element elt = biopaxMetaDoc.createElementNS("","metadata");
//		elt.setTextContent(String.format("{name:\"%s\",uri:\"%s\"}", model.getName(), model.getUri())
//			.replaceAll("null",""));
//		modelNotes.getAny().add(elt);

		final boolean layout = doLayout && n < this.maxNodes && !arcMap.isEmpty();
		try {
			//Must call this, although actual layout might never run;
			//in some real data tests, skipping createLayout method
			//led to malformed SBGN model, unfortunately...
			(new SBGNLayoutManager()).createLayout(sbgn, layout);
		} catch (Exception e) {
			throw new RuntimeException("SBGN Layout of " + model.getXmlBase()
					+ ((model.getName()==null) ? "" : model.getName()) + " failed.", e);
		}
		if(!layout) log.warn(String.format("No layout, for either " +
				"it's disabled: %s, or ~ no. nodes > %s: %s, or - no edges: %s",
				!doLayout, maxNodes, n>maxNodes, arcMap.isEmpty()));

		return sbgn; //modified sbgn (even when no layout is run)
	}

	// Associate all the controllers of this process;
	private void processControllers(Set<Control> controls, Glyph process) {
		for (Control ctrl : controls) {
			Glyph g = createControlStructure(ctrl); //if ctrl has upstream controls (modulations), they're processed as well
			if (g != null) {
				createArc(g, process, getControlType(ctrl), null);
				processControllers(ctrl.getControlledOf(), g);
			}
		}
	}

	/*
	 * Initially, we don't want to represent every PhysicalEntity or Gene node.
	 * For example, if a Complex is nested under another Complex,
	 * and if it is not a participant of any interaction,
	 * then we don't want to draw it separately; also skip for dangling entities.
	 *
	 * @param ent physical entity or gene (it returns false for other entity types) to test
	 * @return true if we want to draw this entity in SBGN; false - otherwise, or - to be auto-created later
	 */
	private boolean needsToBeCreatedInitially(Entity ent) {
		boolean create = false;

		if(ent instanceof PhysicalEntity || ent instanceof Gene) {
			if(ubiqueDet != null && ubiqueDet.isUbique(ent))
				create = false; // ubiques will be created where they are actually used.
			else if (!ent.getParticipantOf().isEmpty())
				create = true;
			else if(ent instanceof Complex && ((Complex) ent).getComponentOf().isEmpty()
					&& ((Complex) ent).getMemberPhysicalEntityOf().isEmpty())
				create = true; //do make a root/top complex despite it's dangling
		}

		return create;
	}

	/*
	 * Creates a glyph representing the given PhysicalEntity.
	 *
	 * @param e PhysicalEntity or Gene to represent
	 * @return the created glyph
	 */
	private Glyph createGlyph(Entity e)
	{
		String id = convertID(e.getUri());
		if (glyphMap.containsKey(id))
			return glyphMap.get(id);

		// Create its glyph and register
		Glyph g = createGlyphBasics(e, true);
		glyphMap.put(g.getId(), g);

		//TODO: export metadata (e.g., from bpe.getAnnotations() map) using the SBGN Extension feature
//		SBGNBase.Extension ext = new SBGNBase.Extension();
//		g.setExtension(ext);
//		Element el = biopaxMetaDoc.createElement(e.getModelInterface().getSimpleName());
//		el.setAttribute("uri", e.getUri());
//		ext.getAny().add(el);

		if (g.getClone() != null)
			ubiqueSet.add(g);

		if(e instanceof PhysicalEntity) {
			PhysicalEntity pe = (PhysicalEntity) e;
			assignLocation(pe, g);
			if("or".equalsIgnoreCase(g.getClazz())) {
				buildGeneric(pe, g, null);
			} else if (pe instanceof Complex) {
				createComplexContent((Complex) pe, g);
			}
		}

		return g;
	}

	/*
	 * Assigns compartmentRef of the glyph.
	 * @param pe Related PhysicalEntity
	 * @param g the glyph
	 */
	private void assignLocation(PhysicalEntity pe, Glyph g) {
		// Create compartment -- add this inside the compartment
		Glyph loc = getCompartment(pe);
		if (loc != null) {
			g.setCompartmentRef(loc);
		}
	}

	/*
	 * This method creates a glyph for the given PhysicalEntity, sets its title and state variables
	 * if applicable.
	 *
	 * @param e PhysicalEntity or Gene to represent
	 * @param idIsFinal if ID is final, then it is recorded for future reference
	 * @return the glyph
	 */
	private Glyph createGlyphBasics(Entity e, boolean idIsFinal)
	{
		Glyph g = factory.createGlyph();
		g.setId(convertID(e.getUri()));

		String s = typeMatchMap.get(e.getModelInterface());
		if(( //use 'or' sbgn class for special generic physical entities
			e instanceof Complex && !((Complex)e).getMemberPhysicalEntity().isEmpty()
				&& ((Complex) e).getComponent().isEmpty())
			||
			(e instanceof SimplePhysicalEntity && ((SimplePhysicalEntity) e).getEntityReference()==null
				&& !((SimplePhysicalEntity) e).getMemberPhysicalEntity().isEmpty()))
		{
			s = GlyphClazz.OR.getClazz();
		}
		g.setClazz(s);

		// Set the label
		Label label = factory.createLabel();
		label.setText(findLabelFor(e));
		g.setLabel(label);

		// Detect if ubique
		if (ubiqueDet != null && ubiqueDet.isUbique(e))
		{
			g.setClone(factory.createGlyphClone());
		}

		// Put on state variables
		if (!g.getClazz().equals(GlyphClazz.OR.getClazz())) {
			g.getGlyph().addAll(getInformation(e));
		}

		// Record the mapping
		if (idIsFinal) {
			Set<String> uris = new HashSet<>();
			uris.add(e.getUri());
			sbgn2BPMap.put(g.getId(), uris);
		}

		return g;
	}

	/*
	 * Gets the representing glyph of the PhysicalEntity or Gene.
	 * @param e PhysicalEntity or Gene to get its glyph
	 * @param linkID Edge id, used if the Entity is ubique
	 * @return Representing glyph
	 */
	private Glyph getGlyphToLink(Entity e, String linkID)
	{
		if (ubiqueDet == null || !ubiqueDet.isUbique(e))
		{
			return glyphMap.get(convertID(e.getUri()));
		}
		else {
			// Create a new glyph for each use of ubique
			Glyph g = createGlyphBasics(e, false);
			g.setId(convertID(e.getUri()) + "_" + ModelUtils.md5hex(linkID));
			Set<String> uris = new HashSet<>();
			uris.add(e.getUri());
			sbgn2BPMap.put(g.getId(), uris);
			if(e instanceof PhysicalEntity && ((PhysicalEntity)e).getCellularLocation() != null) {
				assignLocation((PhysicalEntity) e, g);
			}
			ubiqueSet.add(g);
			return g;
		}
	}
	
	/*
	 * Fills in the content of a complex.
	 *
	 * @param cx Complex to be filled
	 * @param cg its glyph
	 */
	private void createComplexContent(Complex cx, Glyph cg)
	{
		if (flattenComplexContent)
		{
			for (PhysicalEntity mem : getFlattenedMembers(cx))
			{
				createComplexMember(mem, cg);
			}
		}
		else {
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

	private void buildGeneric(PhysicalEntity generic, Glyph or, Glyph container) {
		assert "or".equalsIgnoreCase(or.getClazz()) : "must be 'or' glyph class";

		for (PhysicalEntity m : generic.getMemberPhysicalEntity())
		{
			Glyph g = createGlyphBasics(m,false);
			if(container!=null)
				container.getGlyph().add(g);
			String gid = g.getId() + "_" + ModelUtils.md5hex("memberof_" + or.getId());
			g.setId(gid);
			glyphMap.put(gid, g);
			Set<String> uris =  new HashSet<>();
			uris.add(m.getUri());
			sbgn2BPMap.put(gid, uris);
			assignLocation(m, g);
			createArc(g, or, ArcClazz.LOGIC_ARC.getClazz(), null);
			if(m instanceof Complex)
				createComplexContent((Complex) m, g);
		}
	}


	/*
	 * Recursive method for creating the content of a complex. A complex may contain other complexes
	 * (bad practice), but SBGN needs them flattened. If an inner complex is empty, then we
	 * represent it using a glyph. Otherwise we represent only the members of this inner complex,
	 * merging them with the most outer complex.
	 *
	 * @param cx inner complex to add as member
	 * @param container glyph for most outer complex
	 */
	private void addComplexAsMember(Complex cx, Glyph container) {
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

	/*
	 * Gets the members of the Complex that needs to be displayed in a flattened view.
	 * @param cx to get members
	 * @return members to display
	 */
	private Set<PhysicalEntity> getFlattenedMembers(Complex cx) {
		Set<PhysicalEntity> set = new HashSet<>();
		for (PhysicalEntity mem : cx.getComponent()) {
			if (mem instanceof Complex) {
				if (!hasNonComplexMember((Complex) mem)) {
					set.add(mem);
				} else {
					set.addAll(getFlattenedMembers((Complex) mem));
				}
			} else set.add(mem);
		}

		return set;
	}

	/*
	 * Checks if a Complex contains any PhysicalEntity member which is not a Complex.
	 * @param cx to check
	 * @return true if there is a non-complex member
	 */
	private boolean hasNonComplexMember(Complex cx) {
		for (PhysicalEntity mem : cx.getComponent()) {
			if (!(mem instanceof Complex) || hasNonComplexMember((Complex) mem)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Creates a glyph for the complex member.
	 *
	 * @param pe PhysicalEntity to represent as complex member
	 * @param container Glyph for the complex shell
	 */
	private Glyph createComplexMember(PhysicalEntity pe, Glyph container) {
		Glyph g = createGlyphBasics(pe, false);
		container.getGlyph().add(g);

		// A PhysicalEntity may appear in many complexes -- we identify the member using its complex
		g.setId(g.getId() + "_" + ModelUtils.md5hex(container.getId()));
		glyphMap.put(g.getId(), g);

		Set<String> uris =  new HashSet<>();
		uris.add(pe.getUri());
		sbgn2BPMap.put(g.getId(), uris);

		if("or".equalsIgnoreCase(g.getClazz())) {
			buildGeneric(pe, g, container);
		}

		return g;
	}

	/*
	 * Looks for the display name of this PhysicalEntity. If there is none, then it looks for the
	 * display name of its EntityReference. If still no name at hand, it tries the standard
	 * name, and then first element in name lists.
	 *
	 * A good BioPAX file will use a short and specific name (like HGNC symbols) as displayName.
	 *
	 * @param pe PhysicalEntity or Gene to find a name
	 * @return a name for labeling
	 */
	private String findLabelFor(Entity pe) {
		// Use gene symbol of PE
		for (Xref xref : pe.getXref()) {
			String sym = extractGeneSymbol(xref);
			if (sym != null) return sym;
		}

		// Use gene symbol of ER
		EntityReference er = null;

		if (pe instanceof SimplePhysicalEntity) {
			er = ((SimplePhysicalEntity) pe).getEntityReference();
		}

		if (er != null) {
			for (Xref xref : er.getXref()) {
				String sym = extractGeneSymbol(xref);
				if (sym != null) return sym;
			}
		}
		
		// Use display name of entity
		String name = pe.getDisplayName();

		if (name == null || name.trim().isEmpty())
		{
			if (er != null) {
				name = er.getDisplayName();
			}

			if (name == null || name.trim().isEmpty()) {
				name = pe.getStandardName();
				if (name == null || name.trim().isEmpty()) {
					if (er != null) {
						name = er.getStandardName();
					}
					if (name == null || name.trim().isEmpty()) {
						if (!pe.getName().isEmpty()) {
							// Use first name of entity
							name = pe.getName().iterator().next();
						} else if (er != null && !er.getName().isEmpty()) {
							// Use first name of reference
							name = er.getName().iterator().next();
						}
					}
				}
			}
		}

		// Search for the shortest name of chemicals
		if (pe instanceof SmallMolecule) {
			String shortName = getShortestName((SmallMolecule) pe);
			if (shortName != null) {
				if (name == null || (shortName.length() < name.length() && !shortName.isEmpty())) {
					name = shortName;
				}
			}
		}
		
		if (name == null || name.trim().isEmpty()) {
			// Don't leave it without a name
			name = "noname";
		}

		return name;
	}

	/*
	 * Searches for the shortest name of the PhysicalEntity.
	 * @param spe entity to search in
	 * @return the shortest name
	 */
	private String getShortestName(SimplePhysicalEntity spe)
	{
		String name = null;

		for (String s : spe.getName()) {
			if (name == null || s.length() > name.length()) name = s;
		}

		EntityReference er = spe.getEntityReference();
		
		if (er != null) {
			for (String s : er.getName()) {
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
		String xrefDb = xref.getDb();
		if (xrefDb != null && (
			StringUtils.startsWithIgnoreCase(xrefDb, "hgnc") ||	StringUtils.equalsIgnoreCase(xrefDb, "Gene Symbol"))
		) {
			String ref = xref.getId();
			if (ref != null) {
				ref = ref.trim();
				if (ref.contains("_")) ref = ref.substring(ref.indexOf("_") + 1); //"dirty" auto-fix for e.g. HGNC_1234
				ref = HGNC.getSymbolByHgncIdOrSym(ref);
			}
			return ref;
		}
		return null;
	}
	
	/*
	 * Adds molecule type, and iterates over features of the entity and creates corresponding state
	 * variables. Ignores binding features and covalent-binding features.
	 * 
	 * @param e entity or gene to collect features
	 * @return list of state variables
	 */
	private List<Glyph> getInformation(Entity e) {
		List<Glyph> list = new ArrayList<>();

		// Add the molecule type before states if this is a nucleic acid or gene
		if (e instanceof NucleicAcid || e instanceof Gene) {
			Glyph g = factory.createGlyph();
			g.setClazz(GlyphClazz.UNIT_OF_INFORMATION.getClazz());
			Label label = factory.createLabel();
			String s;
			if(e instanceof Dna)
				s = "mt:DNA";
			else if(e instanceof DnaRegion)
				s = "ct:DNA";
			else if(e instanceof Rna)
				s = "mt:RNA";
			else if(e instanceof RnaRegion)
				s = "ct:RNA";
			else if(e instanceof Gene)
				s = "ct:gene";
			else
				s = "mt:NuclAc";

			label.setText(s);
			g.setLabel(label);
			list.add(g);
		}

		// Extract state variables
		if(e instanceof PhysicalEntity) {
			PhysicalEntity pe = (PhysicalEntity) e;
			extractFeatures(pe.getFeature(), true, list);
			extractFeatures(pe.getNotFeature(), false, list);
		}

		return list;
	}

	/*
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
		for (EntityFeature feature : features) {
			if (feature instanceof ModificationFeature || feature instanceof FragmentFeature) {
				Glyph stvar = factory.createGlyph();
				stvar.setClazz(GlyphClazz.STATE_VARIABLE.getClazz());
				Glyph.State state = featStrGen.createStateVar(feature, factory);
				if (state != null) {
					// Add a "!" in front of NOT features
					if (!normalFeature) {
						state.setValue("!" + state.getValue());
					}
					stvar.setState(state);
					list.add(stvar);
				}
			}
		}
	}

	private Glyph getCompartment(String name) {
		if (name == null || name.isEmpty())
			return null;

		name = name.toLowerCase().trim();
		final String id = name.replaceAll("\\s+","_");
		Glyph comp = compartmentMap.get(id);

		if (comp == null) {
			//create a new compartment glyph and store in the map
			comp = factory.createGlyph();
			comp.setId(id);
			comp.setClazz(GlyphClazz.COMPARTMENT.getClazz());
			Label label = factory.createLabel();
			comp.setLabel(label);
			label.setText(name);
			compartmentMap.put(id, comp);
		}

		return comp;
	}

	/*
	 * Gets the compartment of the given PhysicalEntity.
	 *
	 * @param pe PhysicalEntity to look for its compartment
	 * @return name of compartment or null if there is none
	 */
	private Glyph getCompartment(PhysicalEntity pe) {
		CellularLocationVocabulary cl = pe.getCellularLocation();
		if (cl != null && !cl.getTerm().isEmpty()) {
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

	/*
	 * Creates a representation for Conversion.
	 *
	 * @param cnv the conversion
	 * @param direction direction of the conversion to create
	 */
	private void createProcessAndConnections(Conversion cnv, ConversionDirectionType direction)
	{
		assert cnv.getConversionDirection() == null ||
			cnv.getConversionDirection().equals(direction) ||
			cnv.getConversionDirection().equals(ConversionDirectionType.REVERSIBLE);

		// create the process for the conversion in that direction
		Glyph process = factory.createGlyph();
		process.setClazz(GlyphClazz.PROCESS.getClazz());
		process.setId(convertID(cnv.getUri()) + "_" + direction.name().replaceAll("_",""));
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
		for (PhysicalEntity pe : input) {
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(g, process.getPort().get(0), direction == ConversionDirectionType.REVERSIBLE ?
				ArcClazz.PRODUCTION.getClazz() : ArcClazz.CONSUMPTION.getClazz(), stoic.get(pe));
		}

		// Associate outputs to output port
		for (PhysicalEntity pe : output) {
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(process.getPort().get(1), g, ArcClazz.PRODUCTION.getClazz(), stoic.get(pe));
		}

		processControllers(cnv.getControlledOf(), process);

		// Record mapping
		Set<String> uris = new HashSet<>();
		uris.add(cnv.getUri());
		sbgn2BPMap.put(process.getId(), uris);
	}

	/*
	 * Gets the map of stoichiometry coefficients of participants.
	 * @param conv the conversion
	 * @return map from physical entities to their stoichiometry
	 */
	private Map<PhysicalEntity, Stoichiometry> getStoichiometry(Conversion conv) {
		Map<PhysicalEntity, Stoichiometry> map = new HashMap<PhysicalEntity, Stoichiometry>();

		for (Stoichiometry stoc : conv.getParticipantStoichiometry())
			map.put(stoc.getPhysicalEntity(), stoc);

		return map;
	}

	/*
	 * Creates a representation for TemplateReaction.
	 *
	 * @param tr template reaction
	 */
	private void createProcessAndConnections(TemplateReaction tr) {
		// create the process for the reaction
		Glyph process = factory.createGlyph();
		process.setClazz(GlyphClazz.PROCESS.getClazz());
		process.setId(convertID(tr.getUri()));
		glyphMap.put(process.getId(), process);

		final Set<PhysicalEntity> products = tr.getProduct();

		// parent property 'participant' is sometimes defined directly
		// for a TemplateReaction object despite 'product', 'template'
		// must be used instead (this is bad biopax data modeling practice; e.g. in Reactome);
		// we can try to infer template/product and link what's left
		// after first processing actual 'product' and 'template' prop values.
		final Set<PhysicalEntity> participants = //a new modifiable set to
			new HashSet<>(new ClassFilterSet<>(tr.getParticipant(), PhysicalEntity.class));

		// link products, if any
		for (PhysicalEntity pe : products) {
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(process, g, ArcClazz.PRODUCTION.getClazz(), null);
			participants.remove(pe);
		}

		// link template, if present
		PhysicalEntity template = tr.getTemplate();
		if(template != null) {
			Glyph g = getGlyphToLink(template, process.getId());
			createArc(g, process, ArcClazz.CONSUMPTION.getClazz(), null);
			participants.remove(template);
		} else if(participants.isEmpty()) {
			//when no template is defined and cannot be inferred, create a source-and-sink as the input
			Glyph sas = factory.createGlyph();
			sas.setClazz(GlyphClazz.SOURCE_AND_SINK.getClazz());
			sas.setId("unknown-template_" + ModelUtils.md5hex(process.getId()));
			glyphMap.put(sas.getId(), sas);
			createArc(sas, process, ArcClazz.CONSUMPTION.getClazz(), null);
		}

		//infer input or output type arc for the rest of participants that left there, if any
		for (PhysicalEntity pe : participants) {
			Glyph g = getGlyphToLink(pe, process.getId());
			if(template==null)
				createArc(g, process, ArcClazz.CONSUMPTION.getClazz(), null);
			else
				createArc(process, g, ArcClazz.PRODUCTION.getClazz(), null);
		}
		// Associate controllers
		processControllers(tr.getControlledOf(), process);
		// Record mapping
		sbgn2BPMap.put(process.getId(), new HashSet<>(Collections.singleton(tr.getUri())));
	}

	private void createBasicProcess(Interaction interaction) {
		// create the process for the conversion in that direction
		Glyph process = factory.createGlyph();
		process.setClazz(GlyphClazz.PROCESS.getClazz());
		process.setId(convertID(interaction.getUri()));
		glyphMap.put(process.getId(), process);
		// Associate participants
		for (PhysicalEntity pe : new ClassFilterSet<>(interaction.getParticipant(), PhysicalEntity.class)) {
			Glyph g = getGlyphToLink(pe, process.getId());
			createArc(g, process, ArcClazz.CONSUMPTION.getClazz(), null);
		}
		// Record mapping
		sbgn2BPMap.put(process.getId(), new HashSet<>(Collections.singleton(interaction.getUri())));
		// Associate controllers
		processControllers(interaction.getControlledOf(), process);
	}

	private void createGiProcess(GeneticInteraction interaction) {
		// create the process for the conversion in that direction
		Glyph process = factory.createGlyph();
		process.setClazz(GlyphClazz.AND.getClazz());
		process.setId(convertID(interaction.getUri()));
		glyphMap.put(process.getId(), process);

		PhenotypeVocabulary v = interaction.getPhenotype();
		if(v != null && !v.getTerm().isEmpty()) {
			String term = v.getTerm().iterator().next().toLowerCase().trim();
			String id = convertID(term);
			Glyph g = glyphMap.get(id);
			if(g == null) {
				g = factory.createGlyph();
				g.setId(id);
				g.setClazz(GlyphClazz.PHENOTYPE.getClazz());
				Label label = factory.createLabel();
				label.setText(term);
				g.setLabel(label);
				glyphMap.put(g.getId(), g);
			}
			createArc(process, g, ArcClazz.STIMULATION.getClazz(), null);
		}
		// Associate participants
		for (Entity e : interaction.getParticipant()) {
			Glyph g = getGlyphToLink(e, process.getId());
			createArc(g, process, ArcClazz.LOGIC_ARC.getClazz(), null);
		}
		// Record mapping
		sbgn2BPMap.put(process.getId(), new HashSet<>(Collections.singleton(interaction.getUri())));
		// Associate controllers
		processControllers(interaction.getControlledOf(), process);
	}

	/*
	 * Creates or gets the glyph to connect to the control arc.
	 *
	 * @param ctrl Control to represent
	 * @return glyph representing the controller tree
	 */
	private Glyph createControlStructure(Control ctrl) {
		Glyph cg;
		Set<PhysicalEntity> controllers = getControllers(ctrl);
		// If no representable controller found, skip this control
		if (controllers.isEmpty()) {
			cg = null;
		} else if (controllers.size() == 1 && getControllerSize(ctrl.getControlledOf()) == 0)
		{ // If there is only one controller with no modulator, put an arc for controller
			cg = getGlyphToLink(controllers.iterator().next(), convertID(ctrl.getUri()));
		} else {
			// This list will contain handles for each participant of the AND structure
			List<Glyph> toConnect = new ArrayList<>();

			// Bundle controllers if necessary
			Glyph gg = handlePEGroup(controllers, convertID(ctrl.getUri()));
			if(gg != null)
				toConnect.add(gg);

			// Handle co-factors of catalysis
			if (ctrl instanceof Catalysis) {
				Set<PhysicalEntity> cofs = ((Catalysis) ctrl).getCofactor();
				Glyph g = handlePEGroup(cofs, convertID(ctrl.getUri()));
				if (g != null) 
					toConnect.add(g);
			}

			if (toConnect.isEmpty()) {
				return null;
			} else if (toConnect.size() == 1) {
				cg = toConnect.iterator().next();
			} else {
				cg = connectWithAND(toConnect);
			}
		}

		return cg;
	}

	/*
	 * Prepares the necessary construct for adding the given PhysicalEntity set to the Control
	 * being drawn.
	 *
	 * @param pes entities to use in control
	 * @return the glyph to connect to the appropriate place
	 */
	private Glyph handlePEGroup(Set<PhysicalEntity> pes, String context) {
		int sz = pes.size();		
		if (sz > 1) {
			List<Glyph> gs = getGlyphsOfPEs(pes, context);
			return connectWithAND(gs);
		} else if (sz == 1) {
			PhysicalEntity pe = pes.iterator().next();
			if(glyphMap.containsKey(convertID(pe.getUri())))
				return getGlyphToLink(pe, context);
		}
		
		//'pes' was empty
		return null;
	}
	
	/*
	 * Gets the glyphs of the given set of PhysicalEntity objects. Does not create anything.
	 *
	 * @param pes entities to get their glyphs
	 * @return glyphs of entities
	 */
	private List<Glyph> getGlyphsOfPEs(Set<PhysicalEntity> pes, String context) {
		List<Glyph> gs = new ArrayList<>();

		for (PhysicalEntity pe : pes)
			if (glyphMap.containsKey(convertID(pe.getUri())))
				gs.add(getGlyphToLink(pe, context));

		return gs;
	}

	/*
	 * Creates an AND glyph downstream of the given glyphs.
	 *
	 * @param gs upstream glyph list
	 * @return AND glyph
	 */
	private Glyph connectWithAND(List<Glyph> gs) {
		// Compose an ID for the AND glyph
		StringBuilder sb = new StringBuilder();
		Iterator<Glyph> iterator = gs.iterator();
		if(iterator.hasNext())
		    sb.append(iterator.next());

		while (iterator.hasNext())
			sb.append("-AND-").append(iterator.next().getId());

		String id = ModelUtils.md5hex(sb.toString()); //shorten a very long id

		// Create the AND glyph if not exists
		Glyph and = glyphMap.get(id);
		if (and == null) {
			and = factory.createGlyph();
			and.setClazz(GlyphClazz.AND.getClazz());
			and.setId(id);
			glyphMap.put(and.getId(), and);
		}

		// Connect upstream to the AND glyph
		for (Glyph g : gs)
			createArc(g, and, ArcClazz.LOGIC_ARC.getClazz(), null);

		return and;
	}

	/*
	 * Converts the control type of the Control to the SBGN classes.
	 *
	 * @param ctrl Control to get its type
	 * @return SBGN type of the Control
	 */
	private String getControlType(Control ctrl) {
		if (ctrl instanceof Catalysis) {
			// Catalysis has its own class
			return ArcClazz.CATALYSIS.getClazz();
		}

		ControlType type = ctrl.getControlType();
		if (type == null) {
			// Use stimulation as the default control type
			return ArcClazz.STIMULATION.getClazz();
		}

		// Map control type to stimulation or inhibition
		switch (type) {
			case ACTIVATION:
			case ACTIVATION_ALLOSTERIC:
			case ACTIVATION_NONALLOSTERIC:
			case ACTIVATION_UNKMECH:
				return ArcClazz.STIMULATION.getClazz();
			case INHIBITION:
			case INHIBITION_ALLOSTERIC:
			case INHIBITION_OTHER:
			case INHIBITION_UNKMECH:
			case INHIBITION_COMPETITIVE:
			case INHIBITION_IRREVERSIBLE:
			case INHIBITION_UNCOMPETITIVE:
			case INHIBITION_NONCOMPETITIVE:
				return ArcClazz.INHIBITION.getClazz();
			default:
				throw new RuntimeException("Invalid control type: " + type);
		}
	}

	/*
	 * Gets the size of representable Controller of this set of Controls.
	 *
	 * @param ctrlSet Controls to check their controllers
	 * @return size of representable controllers
	 */
	private int getControllerSize(Set<Control> ctrlSet) {
		int size = 0;
		for (Control ctrl : ctrlSet) {
			size += getControllers(ctrl).size();
		}

		return size;
	}

	/*
	 * Gets the size of representable Controller of this Control.
	 *
	 * @param ctrl Control to check its controllers
	 * @return size of representable controllers
	 */
	private Set<PhysicalEntity> getControllers(Control ctrl) {
		Set<PhysicalEntity> controllers = new HashSet<>();
		for (Controller clr : ctrl.getController()) {
			if (clr instanceof PhysicalEntity && glyphMap.containsKey(convertID(clr.getUri()))) {
				controllers.add((PhysicalEntity) clr);
			}
		}

		return controllers;
	}


	/*
	 * Adds input and output ports to the glyph.
	 *
	 * @param g glyph to add ports
	 */
	private void addPorts(Glyph g) {
		Port inputPort = factory.createPort();
		Port outputPort = factory.createPort();
		inputPort.setId("INP_" + g.getId());
		outputPort.setId("OUT_" + g.getId());
		g.getPort().add(inputPort);
		g.getPort().add(outputPort);
	}

	/*
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

		arc.setId(sourceID + "--TO--" + targetID);

		if (stoic != null && stoic.getStoichiometricCoefficient() > 1) {
			Glyph card = factory.createGlyph();
			card.setClazz(GlyphClazz.CARDINALITY.getClazz());
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

	/*
	 * Collects root-level glyphs in the given glyph collection.
	 *
	 * @param glyphCol glyph collection to search
	 * @return set of roots
	 */
	private Set<Glyph> getRootGlyphs(Collection<Glyph> glyphCol) {
		Set<Glyph> root = new HashSet<>(glyphCol);
		Set<Glyph> children = new HashSet<>();

		for (Glyph glyph : glyphCol) {
			addChildren(glyph, children);
		}
		root.removeAll(children);

		return root;
	}

	/*
	 * Adds children of this glyph to the specified set recursively.
	 * @param glyph to collect children
	 * @param set to add
	 */
	private void addChildren(Glyph glyph, Set<Glyph> set) {
		for (Glyph child : glyph.getGlyph()) {
			set.add(child);
			addChildren(child, set);
		}
	}

	/**
	 * Gets the mapping from SBGN IDs to BioPAX IDs.
	 * This mapping is currently many-to-one, but can
	 * potentially become many-to-many in the future.
	 * @return sbgn-to-biopax mapping
	 */
	public Map<String, Set<String>> getSbgn2BPMap() {
		return sbgn2BPMap;
	}

	private String convertID(String id) {
		return id;
	}

}
