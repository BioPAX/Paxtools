package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.constraint.HasAnID;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.PhysicalEntityChain;
import org.biopax.paxtools.pattern.util.HGNC;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Adapter class for a miner.
 *
 * @author Ozgun Babur
 */
public abstract class MinerAdapter implements Miner
{
	/**
	 * Name of the miner.
	 */
	protected String name;

	/**
	 * Description of the miner.
	 */
	protected String description;

	/**
	 * Blacklist for identifying ubiquitous small molecules.
	 */
	protected Blacklist blacklist;

	/**
	 * ID fetcher is used for skipping objects that cannot generate a valid ID during the search.
	 */
	protected IDFetcher idFetcher;

	/**
	 * Memory for object IDs. This is needed for performance issues. Without this, half of SIF
	 * conversion is spent for fetchIDs().
	 */
	protected Map<BioPAXElement, Set<String>> idMap;

	private Pattern pattern;

	/**
	 * Constructor with name and description.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	protected MinerAdapter(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.idMap = Collections.synchronizedMap(new HashMap<BioPAXElement, Set<String>>());
	}

	/**
	 * Sets the blacklist to use during SIF search.
	 * @param blacklist for identifying ubiquitous small molecules
	 */
	public void setBlacklist(Blacklist blacklist)
	{
		this.blacklist = blacklist;
	}


	/**
	 * Sets the ID fetcher to use during SIF search.
	 * @param idFetcher ID generator from BioPAX object
	 */
	public void setIDFetcher(IDFetcher idFetcher)
	{
		this.idFetcher = idFetcher;
	}

	/**
	 * Constructs the pattern to use for mining.
	 * @return the pattern
	 */
	public abstract Pattern constructPattern();

	/**
	 * Gets the pattern, constructs if null.
	 * @return pattern
	 */
	public Pattern getPattern()
	{
		if(pattern == null) {
			pattern = constructPattern();
			if (this instanceof SIFMiner && idFetcher != null && idMap != null) {
				pattern.add(new HasAnID(idFetcher, idMap), ((SIFMiner) this).getSourceLabel());
				pattern.add(new HasAnID(idFetcher, idMap), ((SIFMiner) this).getTargetLabel());
			}
			pattern.optimizeConstraintOrder();
		}
		return pattern;
	}

	/**
	 * Gets the name of the miner.
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the description of the miner.
	 * @return description
	 */
	public String getDescription()
	{
		return description;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Map<BioPAXElement, Set<String>> getIdMap()
	{
		return idMap;
	}

	public void setIdMap(Map<BioPAXElement, Set<String>> idMap)
	{
		this.idMap = Collections.synchronizedMap(new HashMap<BioPAXElement, Set<String>>(idMap));
	}

	/**
	 * Uses the name as sting representation of the miner.
	 * @return name
	 */
	@Override
	public String toString()
	{
		return getName();
	}

	//----- Section: Helper methods ---------------------------------------------------------------|

	/**
	 * Searches for the gene symbol of the given EntityReference.
	 * @param pr to search for a symbol
	 * @return symbol
	 */
	protected String getGeneSymbol(ProteinReference pr)
	{
		for (Xref xr : pr.getXref())
		{
			String db = xr.getDb();
			if (db != null)
			{
				db = db.toLowerCase();
				if (db.startsWith("hgnc"))
				{
					String id = xr.getId();
					if (id != null)
					{
						String symbol = HGNC.getSymbol(id);
						if (symbol != null && !symbol.isEmpty())
						{
							return symbol;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Searches for the uniprot name of the given human EntityReference.
	 * @param pr to search for the uniprot name
	 * @return uniprot name
	 */
	protected String getUniprotNameForHuman(ProteinReference pr)
	{
		for (String name : pr.getName())
		{
			if (name.endsWith("_HUMAN")) return name;
		}
		return null;
	}

	/**
	 * Searches for the gene symbol of the given EntityReference.
	 * @param m current match
	 * @param label label of the related EntityReference in the pattern
	 * @return symbol
	 */
	protected String getGeneSymbol(Match m, String label)
	{
		ProteinReference pr = (ProteinReference) m.get(label, getPattern());
		return getGeneSymbol(pr);
	}

	/**
	 * Searches for the uniprot name of the given human EntityReference.
	 * @param m current match
	 * @param label label of the related EntityReference in the pattern
	 * @return uniprot name
	 */
	protected String getUniprotNameForHuman(Match m, String label)
	{
		ProteinReference er = (ProteinReference) m.get(label, getPattern());
		return getUniprotNameForHuman(er);
	}

	/**
	 * Checks if the type of a Control is inhibition.
	 * @param ctrl Control to check
	 * @return true if type is inhibition related
	 */
	public boolean isInhibition(Control ctrl)
	{
		return ctrl.getControlType() != null && ctrl.getControlType().toString().startsWith("I");
	}

	//----- Section: Mining modifications ---------------------------------------------------------|

	/**
	 * Accessor for modification features.
	 */
	private static final PathAccessor FEAT_ACC =
		new PathAccessor("PhysicalEntity/feature:ModificationFeature");

	/**
	 * Accessor for modification terms.
	 */
	private static final PathAccessor TERM_ACC =
		new PathAccessor("ModificationFeature/modificationType/term");

	/**
	 * Accessor to sequence site of modification.
	 */
	private static final PathAccessor SITE_ACC =
		new PathAccessor("ModificationFeature/featureLocation:SequenceSite/sequencePosition");

	/**
	 * Accessor to sequence interval begin site of modification.
	 */
	private static final PathAccessor INTERVAL_BEGIN_ACC = new PathAccessor(
		"ModificationFeature/featureLocation:SequenceInterval/sequenceIntervalBegin/sequencePosition");

	/**
	 * Accessor to sequence interval end site of modification.
	 */
	private static final PathAccessor INTERVAL_END_ACC = new PathAccessor(
		"ModificationFeature/featureLocation:SequenceInterval/sequenceIntervalEnd/sequencePosition");

	/**
	 * Sorts the modifications and gets them in a String.
	 * @param set modifications
	 * @return a String listing the modifications
	 */
	public Set<String> toStringSet(Set<ModificationFeature> set)
	{
		List<ModificationFeature> list = new ArrayList<>(set);

		Collections.sort(list, (o1, o2) -> {
			String t1 = getModificationTerm(o1);
			String t2 = getModificationTerm(o2);

			Integer l1 = getPositionStart(o1);
			Integer l2 = getPositionStart(o2);

			if (t1 == null && t2 == null) return l1.compareTo(l2);
			if (t1 == null) return 1;
			if (t2 == null) return -1;
			if (t1.equals(t2)) return l1.compareTo(l2);
			return t1.compareTo(t2);
		});

		return getInString(list);
	}

	/**
	 * Gets the modifications is a string that is separated with comma.
	 * @param list modification list
	 * @return String representing the modifications
	 */
	private Set<String> getInString(List<ModificationFeature> list)
	{
		Set<String> text = new HashSet<>(list.size());

		for (ModificationFeature mf : list)
		{
			String term = getModificationTerm(mf);
			String loc = getPositionInString(mf);

			if (term != null)
			{
				String s = term + loc;
				if (!text.contains(s)) text.add(s);
			}
		}
		return text;
	}

	/**
	 * Gets the String term of the modification feature.
	 * @param mf modification feature
	 * @return modification term
	 */
	public String getModificationTerm(ModificationFeature mf)
	{
		Set vals = TERM_ACC.getValueFromBean(mf);
		if (vals.isEmpty()) return null;
		return vals.iterator().next().toString();
	}

	/**
	 * Gets the first position of the modification feature.
	 * @param mf modification feature
	 * @return first location
	 */
	public int getPositionStart(ModificationFeature mf)
	{
		Set vals = SITE_ACC.getValueFromBean(mf);

		if (!vals.isEmpty())
		{
			return ((Integer) vals.iterator().next());
		}

		vals = INTERVAL_BEGIN_ACC.getValueFromBean(mf);

		if (!vals.isEmpty())
		{
			return ((Integer) vals.iterator().next());
		}

		return -1;
	}

	/**
	 * Gets the position of the modification feature as a String.
	 * @param mf modification feature
	 * @return location
	 */
	public String getPositionInString(ModificationFeature mf)
	{
		Set vals = SITE_ACC.getValueFromBean(mf);

		if (!vals.isEmpty())
		{
			int x = ((Integer) vals.iterator().next());
			if (x > 0) return "@" + x;
		}

		vals = INTERVAL_BEGIN_ACC.getValueFromBean(mf);

		if (!vals.isEmpty())
		{
			int begin = ((Integer) vals.iterator().next());

			vals = INTERVAL_END_ACC.getValueFromBean(mf);

			if (!vals.isEmpty())
			{
				int end = ((Integer) vals.iterator().next());

				if (begin > 0 && end > 0 && begin <= end)
				{
					if (begin == end) return "@"  + begin;
					else return "@" + "[" + begin + "-" + end + "]";
				}
			}
		}

		return "";
	}

	/**
	 * Gets modifications of the given element in a string. The element has to be a PhysicalEntity.
	 * @param m match
	 * @param label label of the PhysicalEntity
	 * @return modifications
	 */
	protected Set<String> getModifications(Match m, String label)
	{
		PhysicalEntity pe = (PhysicalEntity) m.get(label, getPattern());
		return toStringSet(new HashSet<>(FEAT_ACC.getValueFromBean(pe)));
	}

	/**
	 * Gets modifications of the given elements in a string set. The elements has to be a
	 * PhysicalEntity and they must be two ends of a chain with homology and/or complex membership
	 * relations.
	 * @param m match
	 * @param memLabel the member-end of the PhysicalEntity chain
	 * @param comLabel the complex-end of the PhysicalEntity chain
	 * @return modifications
	 */
	protected Set<String> getModifications(Match m, String memLabel, String comLabel)
	{
		PhysicalEntityChain chain = getChain(m, memLabel, comLabel);
		return toStringSet(chain.getModifications());
	}

	/**
	 * Gets cellular locations of the given elements in a string set. The elements has to be a
	 * PhysicalEntity and they must be two ends of a chain with homology and/or complex membership
	 * relations.
	 * @param m match
	 * @param memLabel the member-end of the PhysicalEntity chain
	 * @param comLabel the complex-end of the PhysicalEntity chain
	 * @return cellular locations
	 */
	protected Set<String> getCellularLocations(Match m, String memLabel, String comLabel)
	{
		PhysicalEntityChain chain = getChain(m, memLabel, comLabel);
		return chain.getCellularLocations();
	}

	/**
	 * Gets delta modifications of the given elements in string sets. The elements has to be two
	 * PhysicalEntity chains. The result array is composed of two strings: gained (0) and lost (1).
	 *
	 * @param m match
	 * @param memLabel1 the member-end of the first PhysicalEntity chain
	 * @param comLabel1 the complex-end of the first PhysicalEntity chain
	 * @param memLabel2 the member-end of the second PhysicalEntity chain
	 * @param comLabel2 the complex-end of the second PhysicalEntity chain
	 * @return delta modifications
	 */
	protected Set<String>[] getDeltaModifications(Match m, String memLabel1, String comLabel1,
		String memLabel2, String comLabel2)
	{
		PhysicalEntityChain chain1 = getChain(m, memLabel1, comLabel1);
		PhysicalEntityChain chain2 = getChain(m, memLabel2, comLabel2);

		Set<ModificationFeature> before = chain1.getModifications();
		Set<ModificationFeature> after = chain2.getModifications();

		Set<String> afterMods = toStringSet(after);
		Set<String> beforeMods = toStringSet(before);
		removeCommon(afterMods, beforeMods);

		return new Set[]{afterMods, beforeMods};
	}

	/**
	 * Gets delta compartments of the given two PE chains. The result array is composed of two
	 * string sets: gained (0) and lost (1).
	 *
	 * @param m match
	 * @param memLabel1 the member-end of the first PhysicalEntity chain
	 * @param comLabel1 the complex-end of the first PhysicalEntity chain
	 * @param memLabel2 the member-end of the second PhysicalEntity chain
	 * @param comLabel2 the complex-end of the second PhysicalEntity chain
	 * @return delta compartments
	 */
	protected Set<String>[] getDeltaCompartments(Match m, String memLabel1, String comLabel1,
		String memLabel2, String comLabel2)
	{
		PhysicalEntityChain chain1 = getChain(m, memLabel1, comLabel1);
		PhysicalEntityChain chain2 = getChain(m, memLabel2, comLabel2);

		Set<String> before = chain1.getCellularLocations();
		Set<String> after = chain2.getCellularLocations();
		removeCommon(after, before);

		return new Set[]{after, before};
	}

	protected PhysicalEntityChain getChain(Match m, String memLabel, String comLabel)
	{
		return new PhysicalEntityChain((PhysicalEntity) m.get(memLabel, getPattern()),
			(PhysicalEntity)m.get(comLabel, getPattern()));
	}

	protected void removeCommon(Set<String> set1, Set<String> set2)
	{
		Set<String> common = new HashSet<>(set1);
		common.retainAll(set2);
		set1.removeAll(common);
		set2.removeAll(common);
	}

	/**
	 * Converts the set of string to a single string.
	 * @param set the set
	 * @param sep separator string
	 * @return concatenated string
	 */
	protected String concat(Set<String> set, String sep)
	{
		String s = "";

		int i = set.size();
		for (String ss : set)
		{
			s += ss;
			if (--i > 0) s += sep;
		}
		return s;
	}

	/**
	 * Identifies negative and positive controls. Assumes positive by default.
	 * @param ctrl control to check
	 * @return sign
	 */
	protected int sign(Control ctrl)
	{
		ControlType type = ctrl.getControlType();
		if (type != null && type.name().startsWith("I")) return -1;
		return 1;
	}

	/**
	 * Checks the cumulative sign of the chained controls.
	 * @param m result match
	 * @param ctrlLabel labels for controls
	 * @return sign
	 */
	protected int sign(Match m, String... ctrlLabel)
	{
		int sign = 1;

		for (String lab : ctrlLabel)
		{
			Control ctrl = (Control) m.get(lab, getPattern());
			sign *= sign(ctrl);
		}
		return sign;
	}

	/**
	 * Checks if a PE chain is labeled as inactive.
	 * @param m the result match
	 * @param simpleLabel simple end of the chain
	 * @param complexLabel complex end of the chain
	 * @return true if labeled inactive
	 */
	protected boolean labeledInactive(Match m, String simpleLabel, String complexLabel)
	{
		PhysicalEntityChain chain = getChain(m, simpleLabel, complexLabel);
		PhysicalEntityChain.Activity activity = chain.checkActivityLabel();
		return activity == PhysicalEntityChain.Activity.INACTIVE;
	}

	//----- Section: Result as SIF format ---------------------------------------------------------|

	/**
	 * This method writes the output as pairs of gene symbols of the given two ProteinReference.
	 * Parameters labels have to map to ProteinReference.
	 * @param matches the search result
	 * @param out output stream for text output
	 * @param directed if true, reverse pairs is treated as different pairs
	 * @param label1 label for the first ProteinReference in the result matches
	 * @param label2 label for the second ProteinReference in the result matches
	 * @throws IOException if cannot write to output stream
	 */
	protected void writeResultAsSIF(Map<BioPAXElement, List<Match>> matches, OutputStream out,
		boolean directed, String label1, String label2) throws IOException
	{
		if (matches.isEmpty()) return;
		if (this instanceof SIFMiner)
		{
			writeSIFsUsingSIFFramework(matches, out);
			return;
		}

		// Memory for already written pairs.
		Set<String> mem = new HashSet<>();

		String mid = getRelationType() == null ? "\t" : "\trelation\t";

		OutputStreamWriter writer = new OutputStreamWriter(out);
		String header = getHeader();
		writer.write(header  == null ? label1 + mid + label2 : header);

		for (BioPAXElement ele : matches.keySet())
		{
			for (Match m : matches.get(ele))
			{
				Set<String> s1 = getIdentifiers(m, label1);
				Set<String> s2 = getIdentifiers(m, label2);

				for (String s1s : s1)
				{
					for (String s2s : s2)
					{
						String type = getRelationType();
						String sep = type == null ? "\t" : "\t" + type + "\t";

						String relation = s1s + sep + s2s;
						String reverse = s2s + sep + s1s;

						if (!mem.contains(relation) && (directed || !mem.contains(reverse)))
						{
							writer.write("\n" + relation);
							mem.add(relation);
							if (!directed) mem.add(reverse);
						}
					}
				}
			}
		}
		writer.flush();
	}

	/**
	 * This method writes the output as pairs of gene symbols of the given two ProteinReference.
	 * Parameters labels have to map to ProteinReference.
	 * @param matches the search result
	 * @param out output stream for text output
	 * @throws IOException if cannot write to output stream
	 */
	protected void writeSIFsUsingSIFFramework(Map<BioPAXElement, List<Match>> matches,
		OutputStream out) throws IOException
	{
		Map<SIFInteraction, SIFInteraction> sifMap = new HashMap<SIFInteraction, SIFInteraction>();

		for (List<Match> matchList : matches.values())
		{
			for (Match match : matchList)
			{
				for (SIFInteraction inter : this.createSIFInteraction(match, new CommonIDFetcher()))
				{
					if (inter.hasIDs())
					{
						if (sifMap.containsKey(inter))
						{
							sifMap.get(inter).mergeWith(inter);
						}
						else sifMap.put(inter, inter);
					}
				}
			}
		}
		OutputStreamWriter writer = new OutputStreamWriter(out);

		boolean first = true;
		for (SIFInteraction inter : sifMap.keySet())
		{
			if (first) first = false;
			else writer.write("\n");

			writer.write(inter.toString());
		}
		writer.flush();
	}

	/**
	 * Checks if the relation captured by match has a type. THis method just returns null but any
	 * child class using <code>writeResultAsSIF</code> method can implement this to have a
	 * relationship type between gene symbol pairs.
	 * @return type of the relation
	 */
	protected String getRelationType()
	{
		if (this instanceof SIFMiner)
		{
			return ((SIFMiner) this).getSIFType().getTag();
		}
		else return null;
	}

	/**
	 * Gets the first line of the result file. This method should be overridden to customize the
	 * header of the result file.
	 * @return header
	 */
	public String getHeader()
	{
		return null;
	}

	//----- Section: Result more detailed than SIF ------------------------------------------------|

	/**
	 * Writes the result as a tab delimited format, where the column values are customized.
	 * @param matches result matches
	 * @param out output stream
	 * @param columns number of columns in the result
	 * @throws IOException if cannot write to the stream
	 */
	protected void writeResultDetailed(Map<BioPAXElement, List<Match>> matches, OutputStream out,
		int columns) throws IOException
	{
		OutputStreamWriter writer = new OutputStreamWriter(out);

		// write the header

		String header = getHeader();
		if (header != null)
		{
			writer.write(header);
		}
		else
		{
			for (int i = 0; i < columns; i++)
			{
				writer.write("col-" + (i+1));
				if (i < columns - 1) writer.write("\t");
			}
		}

		// memory for already written lines
		Set<String> mem = new HashSet<>();

		// write values

		for (BioPAXElement ele : matches.keySet())
		{
			for (Match m : matches.get(ele))
			{
				String line = "";
				boolean aborted = false;

				for (int i = 0; i < columns; i++)
				{
					String s = getValue(m, i);

					if (s == null)
					{
						aborted = true;
						break;
					}
					else
					{
						line += s + "\t";
					}
				}

				if (aborted) continue;

				line = line.trim();

				if (!mem.contains(line))
				{
					writer.write("\n" + line);
					mem.add(line);
				}
			}
		}
		writer.flush();
	}

	/**
	 * This method has to be overridden if <code>writeResultDetailed</code> method is used. It
	 * creates the column value of the given Match. If this method returns <code>null</code> for any
	 * column, then the current match is ignored.
	 * @param m current match
	 * @param col current column
	 * @return column value
	 */
	public String getValue(Match m, int col)
	{
		return null;
	}

	/**
	 * Creates a SIF interaction for the given match.
	 * @param m match to use for SIF creation
	 * @param fetcher ID generator from BioPAX object
	 * @return SIF interaction
	 */
	public Set<SIFInteraction> createSIFInteraction(Match m, IDFetcher fetcher)
	{
		BioPAXElement sourceBpe = m.get(((SIFMiner) this).getSourceLabel(), getPattern());
		BioPAXElement targetBpe = m.get(((SIFMiner) this).getTargetLabel(), getPattern());

		Set<String> sources = fetchIDs(sourceBpe, fetcher);
		Set<String> targets = fetchIDs(targetBpe, fetcher);

		SIFType sifType = ((SIFMiner) this).getSIFType();

		Set<SIFInteraction> set = new HashSet<>();

		for (String source : sources)
		{
			for (String target : targets)
			{
				if (source.equals(target)) continue;
				else if (sifType.isDirected() || source.compareTo(target) < 0)
				{
					set.add(new SIFInteraction(source, target, sourceBpe, targetBpe, sifType,
						new HashSet<>(m.get(getMediatorLabels(), getPattern())),
						new HashSet<>(m.get(getSourcePELabels(), getPattern())),
						new HashSet<>(m.get(getTargetPELabels(), getPattern()))));
				}
				else
				{
					set.add(new SIFInteraction(target, source, targetBpe, sourceBpe, sifType,
						new HashSet<>(m.get(getMediatorLabels(), getPattern())),
						new HashSet<>(m.get(getTargetPELabels(), getPattern())),
						new HashSet<>(m.get(getSourcePELabels(), getPattern()))));
				}
			}
		}
		return set;
	}

	protected Set<String> fetchIDs(BioPAXElement ele, IDFetcher fetcher)
	{
		if (!idMap.containsKey(ele))
		{
			Set<String> ids = fetcher.fetchID(ele);
			idMap.put(ele, ids);
		}
		return idMap.get(ele);
	}

	/**
	 * If a SIF miner wants to tell which essential BioPAX elements mediated this relation, then
	 * they need to override this method and pass the labels of elements.
	 * @return labels of elements to collect publication refs
	 */
	public String[] getMediatorLabels()
	{
		return null;
	}

	/**
	 * If a SIF miner wants to tell which PhysicalEntity objects acted as source of the relation,
	 * they need to override this method and pass the labels of elements.
	 * @return labels of elements
	 */
	public String[] getSourcePELabels()
	{
		return null;
	}

	/**
	 * If a SIF miner wants to tell which PhysicalEntity objects acted as source of the relation,
	 * they need to override this method and pass the labels of elements.
	 * @return labels of elements
	 */
	public String[] getTargetPELabels()
	{
		return null;
	}

	/**
	 * Uses uniprot name or gene symbol as identifier.
	 * @param m current match
	 * @param label label of the related EntityReference in the pattern
	 * @return identifier
	 */
	protected Set<String> getIdentifiers(Match m, String label)
	{
		BioPAXElement el = m.get(label, getPattern());

		if (idFetcher != null) return idFetcher.fetchID(el);

		Set<String> set = new HashSet<>();
		if (el instanceof ProteinReference)
		{
			set.add(getGeneSymbol((ProteinReference) el));
		}
		else if (el instanceof SmallMoleculeReference)
		{
			set.add(getCompoundName((SmallMoleculeReference) el));
		}

		return set;
	}

	/**
	 * Gets the name of the small molecule to use in SIF.
	 * @param smr small molecule ref
	 * @return a name
	 */
	protected String getCompoundName(SmallMoleculeReference smr)
	{
		return smr.getDisplayName();
	}
}
