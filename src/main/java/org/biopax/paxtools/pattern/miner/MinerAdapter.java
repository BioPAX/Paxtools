package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
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
	 * Pattern to use for mining.
	 */
	protected Pattern pattern;

	/**
	 * Constructor with name and description.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	protected MinerAdapter(String name, String description)
	{
		this.name = name;
		this.description = description;
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
		if (pattern == null) pattern = constructPattern();

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
	 * @param er to search for a symbol
	 * @return symbol
	 */
	protected String getGeneSymbol(EntityReference er)
	{
		for (Xref xr : er.getXref())
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
	 * @param er to search for the uniprot name
	 * @return uniprot name
	 */
	protected String getUniprotNameForHuman(EntityReference er)
	{
		for (String name : er.getName())
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
		EntityReference er = (EntityReference) m.get(label, getPattern());
		return getGeneSymbol(er);
	}

	/**
	 * Searches for the uniprot name of the given human EntityReference.
	 * @param m current match
	 * @param label label of the related EntityReference in the pattern
	 * @return uniprot name
	 */
	protected String getUniprotNameForHuman(Match m, String label)
	{
		EntityReference er = (EntityReference) m.get(label, getPattern());
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
	public String listModifications(Set<ModificationFeature> set)
	{
		List<ModificationFeature> list = new ArrayList<ModificationFeature>(set);

		Collections.sort(list, new Comparator<ModificationFeature>()
		{
			@Override
			public int compare(ModificationFeature o1, ModificationFeature o2)
			{
				String t1 = getModificationTerm(o1);
				String t2 = getModificationTerm(o2);

				Integer l1 = getPositionStart(o1);
				Integer l2 = getPositionStart(o2);

				if (t1 == null && t2 == null) return l1.compareTo(l2);
				if (t1 == null) return 1;
				if (t2 == null) return -1;
				if (t1.equals(t2)) return l1.compareTo(l2);
				return t1.compareTo(t2);
			}
		});

		return getInString(list);
	}

	/**
	 * Gets the modifications is a string that is separated with comma.
	 * @param list modification list
	 * @return String representing the modifications
	 */
	private String getInString(List<ModificationFeature> list)
	{
		List<String> text = new ArrayList<String>(list.size());

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

		String s = "";
		for (String t : text)
		{
			s += "[" + t + "] ";
		}
		return s.trim();
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
	protected String getModifications(Match m, String label)
	{
		PhysicalEntity pe = (PhysicalEntity) m.get(label, getPattern());
		return listModifications(new HashSet<ModificationFeature>(FEAT_ACC.getValueFromBean(pe)));
	}

	/**
	 * Gets modifications of the given elements in a string. The elements has to be a PhysicalEntity
	 * and they must be two ends of a chain with homology and/or complex membership relations.
	 * @param m match
	 * @param memLabel the member-end of the PhysicalEntity chain
	 * @param comLabel the complex-end of the PhysicalEntity chain
	 * @return modifications
	 */
	protected String getModifications(Match m, String memLabel, String comLabel)
	{
		PhysicalEntityChain chain = new PhysicalEntityChain(
			(PhysicalEntity) m.get(memLabel, getPattern()),
			(PhysicalEntity)m.get(comLabel, getPattern()));

		return listModifications(chain.getModifications());
	}

	/**
	 * Gets delta modifications of the given elements in a string. The elements has to be two
	 * PhysicalEntity chains. The result array is composed of two strings: gained (0) and lost (1).
	 * Feature tokens are separated with comma.
	 * @param m match
	 * @param memLabel1 the member-end of the first PhysicalEntity chain
	 * @param comLabel1 the complex-end of the first PhysicalEntity chain
	 * @param memLabel2 the member-end of the second PhysicalEntity chain
	 * @param comLabel2 the complex-end of the second PhysicalEntity chain
	 * @return delta modifications
	 */
	protected String[] getDeltaModifications(Match m, String memLabel1, String comLabel1,
		String memLabel2, String comLabel2)
	{
		PhysicalEntityChain chain1 = new PhysicalEntityChain(
			(PhysicalEntity) m.get(memLabel1, getPattern()),
			(PhysicalEntity)m.get(comLabel1, getPattern()));

		PhysicalEntityChain chain2 = new PhysicalEntityChain(
			(PhysicalEntity) m.get(memLabel2, getPattern()),
			(PhysicalEntity)m.get(comLabel2, getPattern()));

		Set<ModificationFeature> before = chain1.getModifications();
		Set<ModificationFeature> intersect = new HashSet<ModificationFeature>(before);
		Set<ModificationFeature> after = chain2.getModifications();
		intersect.retainAll(after);
		before.removeAll(intersect);
		after.removeAll(intersect);

		String afterMods = listModifications(after);
		String beforeMods = listModifications(before);

		return new String[]{afterMods, beforeMods};
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
	public void writeResultAsSIF(Map<BioPAXElement, List<Match>> matches, OutputStream out,
		boolean directed, String label1, String label2) throws IOException
	{
		if (matches.isEmpty()) return;

		// Memory for already written pairs.
		Set<String> mem = new HashSet<String>();

		String mid = getRelationType(matches.values().iterator().next().get(0)) == null ?
			"\t" : "\trelation\t";

		OutputStreamWriter writer = new OutputStreamWriter(out);
		String header = getHeader();
		writer.write(header  == null ? label1 + mid + label2 : header);

		for (BioPAXElement ele : matches.keySet())
		{
			for (Match m : matches.get(ele))
			{
				ProteinReference pr1 = (ProteinReference) m.get(label1, getPattern());
				ProteinReference pr2 = (ProteinReference) m.get(label2, getPattern());

				String s1 = getGeneSymbol(pr1);
				String s2 = getGeneSymbol(pr2);

				if (s1 != null && s2 != null)
				{
					String type = getRelationType(m);
					String sep = type == null ? "\t" : "\t" + type + "\t";

					String relation = s1 + sep + s2;
					String reverse = s2 + sep + s1;

					if (!mem.contains(relation) && (directed || !mem.contains(reverse)))
					{
						writer.write("\n" + relation);
						mem.add(relation);
						if (!directed) mem.add(reverse);
					}
				}
			}
		}
		writer.flush();
	}

	/**
	 * Checks if the relation captured by match has a type. THis method just returns null but any
	 * child class using <code>writeResultAsSIF</code> method can implement this to have a
	 * relationship type between gene symbol pairs.
	 * @param m the match
	 * @return type of the relation
	 */
	public String getRelationType(Match m)
	{
		if (this instanceof SIFMiner)
		{
			return ((SIFMiner) this).getSIFType(m).getTag();
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
	public void writeResultDetailed(Map<BioPAXElement, List<Match>> matches, OutputStream out,
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
		Set<String> mem = new HashSet<String>();

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
	 * @return SIF interaction
	 */
	public SIFInteraction createSIFInteraction(Match m, IDFetcher fetcher)
	{
		if (this instanceof SIFMiner)
		{
				return new SIFInteraction(m.get(((SIFMiner) this).getSourceLabel(), getPattern()),
					m.get(((SIFMiner) this).getTargetLabel(), getPattern()),
					((SIFMiner) this).getSIFType(m), harvestPMIDs(m), fetcher);
		}

		return null;
	}

	protected static final PathAccessor xrefAcc = new PathAccessor("XReferrable/xref:PublicationXref");
	protected static final PathAccessor evidAcc = new PathAccessor("Observable/evidence/xref:PublicationXref");

	/**
	 * Collects publication xrefs of the given elements.
	 * @param ele element array
	 * @return publication xrefs
	 */
	protected Set<PublicationXref> harvestPublicationXrefs(BioPAXElement... ele)
	{
		Set<PublicationXref> set = new HashSet<PublicationXref>();

		for (Object o : xrefAcc.getValueFromBeans(Arrays.asList(ele)))
		{
			set.add((PublicationXref) o);
		}
		for (Object o : evidAcc.getValueFromBeans(Arrays.asList(ele)))
		{
			set.add((PublicationXref) o);
		}
		return set;
	}

	/**
	 * Collects PubMed IDs fromt the given publication xrefs.
	 * @param xrefs publication xrefs
	 * @return PMIDs
	 */
	protected Set<String> harvestPMIDs(Set<PublicationXref> xrefs)
	{
		Set<String> set = new HashSet<String>();

		for (PublicationXref xref : xrefs)
		{
			if (xref.getDb() != null && xref.getDb().equalsIgnoreCase("pubmed"))
				if (xref.getId() != null && !xref.getId().isEmpty())
					set.add(xref.getId());
		}
		return set;
	}

	/**
	 * If a SIF miner wants to add PubMed IDs to the mined SIF interactions, then they need to
	 * override this method and pass the labels of elements to collect the PMIDs.
	 * @return labels of elements to collect publication refs
	 */
	public String[] getPubmedHarvestableLabels()
	{
		return null;
	}

	/**
	 * Collects PMIDs for the given Match
	 * @param m the match
	 * @return PMIDs
	 */
	protected Set<String> harvestPMIDs(Match m)
	{
		String[] labels = getPubmedHarvestableLabels();

		if (labels == null) return null;

		BioPAXElement[] ele = new BioPAXElement[labels.length];
		for (int i = 0; i < ele.length; i++)
		{
			ele[i] =  m.get(labels[i], getPattern());
		}
		return harvestPMIDs(harvestPublicationXrefs(ele));
	}

	/**
	 * Uses uniprot name as identifier.
	 * @param m current match
	 * @param label label of the related EntityReference in the pattern
	 * @return identifier
	 */
	public String getIdentifier(Match m, String label)
	{
		return getGeneSymbol(m, label);
//		return getUniprotNameForHuman(m, label);
	}
}
