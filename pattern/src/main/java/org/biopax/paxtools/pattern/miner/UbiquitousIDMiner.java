package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.constraint.PathConstraint;
import org.biopax.paxtools.pattern.constraint.Size;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for getting ubiquitous small molecules in a model.
 * @author Ozgun Babur
 */
public class UbiquitousIDMiner extends MinerAdapter
{
	/**
	 * Constructor that sets name and description.
	 */
	public UbiquitousIDMiner()
	{
		super("ubiquitous-molecule-lister", "Finds small molecules that participate in at least " +
			"50 Conversions. Writes down IDs of these molecules to the output file, one ID per " +
			"line.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "SMR");
		p.add(new Size(
			new PathConstraint("SmallMoleculeReference/entityReferenceOf/participantOf:Conversion"),
			50, Size.Type.GREATER_OR_EQUAL), "SMR");
		return p;
	}

	/**
	 * Writes the result as "A B", where A and B are gene symbols, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultDetailed(matches, out, 1);
	}

	/**
	 * Gets header of the result file.
	 * @return header
	 */
	@Override
	public String getHeader()
	{
		return "IDs of ubiquitous elements";
	}

	/**
	 * Gets the ids of the small molecule reference and its physical entities.
	 * @param m current match
	 * @param col current column
	 * @return ubique IDs
	 */
	@Override
	public String getValue(Match m, int col)
	{
		assert col == 0;

		return getRelatedIDs((SmallMoleculeReference) m.get("SMR", getPattern()));
	}

	/**
	 * Gets IDs of the small molecule reference and its physical entities.
	 * @param smr small molecule reference
	 * @return related IDs
	 */
	private String getRelatedIDs(SmallMoleculeReference smr)
	{
		String ids = smr.getUri();

		for (Object o : new PathAccessor(
			"SmallMoleculeReference/entityReferenceOf").getValueFromBean(smr))
		{
			SimplePhysicalEntity spe = (SimplePhysicalEntity) o;
			ids += "\n" + spe.getUri();
		}
		return ids;
	}
}
