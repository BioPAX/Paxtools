package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.HGNC;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Searches a model and generates SIF network using the pattern matches.
 *
 * @author Ozgun Babur
 */
public class SIFSearcher
{
	/**
	 * SIF miners to use.
	 */
	private List<SIFMiner> miners;

	/**
	 * SIF types to mine.
	 */
	private Set<SIFType> types;

	/**
	 * Used for getting an ID out of BioPAX elements.
	 */
	private IDFetcher idFetcher;

	/**
	 * Blacklist used for detecting ubiquitous molecules.
	 */
	private Blacklist blacklist;

	/**
	 * Constructor with binary interaction types.
	 * @param types sif types
	 */
	public SIFSearcher(SIFType... types)
	{
		this(null, types);
	}

	/**
	 * Constructor with miners.
	 * @param miners sif miners
	 */
	public SIFSearcher(SIFMiner... miners)
	{
		this(null, miners);
	}

	/**
	 * Constructor with ID fetcher and binary interaction types.
	 * @param types sif types
	 */
	public SIFSearcher(IDFetcher idFetcher, SIFType... types)
	{
		this.idFetcher = idFetcher;
		this.types = new HashSet<SIFType>(Arrays.asList(types));

		if (idFetcher == null) this.idFetcher = new CommonIDFetcher();
	}

	/**
	 * Constructor with ID fetcher and miners.
	 * @param miners sif miners
	 */
	public SIFSearcher(IDFetcher idFetcher, SIFMiner... miners)
	{
		this.idFetcher = idFetcher;
		this.miners = Arrays.asList(miners);

		if (idFetcher == null) this.idFetcher = new CommonIDFetcher();
	}

	private void initMiners()
	{
		this.miners = new ArrayList<SIFMiner>();

		for (SIFType type : types)
		{
			switch (type)
			{
				case CONTROLS_STATE_CHANGE_OF:
					miners.add(new ControlsStateChangeOfMiner());
					miners.add(new CSCOButIsParticipantMiner());
					miners.add(new CSCOBothControllerAndParticipantMiner());
					miners.add(new CSCOThroughControllingSmallMoleculeMiner(blacklist));
					miners.add(new CSCOThroughBindingSmallMoleculeMiner(blacklist));
					break;
				case CONTROLS_EXPRESSION_OF:
					miners.add(new ControlsExpressionMiner());
					miners.add(new ControlsExpressionWithConvMiner());
					break;
				case CONSUMPTION_CONTROLLED_BY:
					miners.add(new ConsumptionControlledByMiner(blacklist));
					break;
				case CONTROLS_PRODUCTION_OF:
					miners.add(new ControlsProductionOfMiner(blacklist));
					break;
				case CONTROLS_TRANSPORT_OF_CHEMICAL:
					miners.add(new ControlsTransportOfChemicalMiner(blacklist));
					break;
				case CONTROLS_TRANSPORT_OF:
					miners.add(new ControlsTransportMiner());
					break;
				case CONTROLS_DEGRADATION_OF:
					miners.add(new ControlsDegradationMiner());
					break;
				case CATALYSIS_PRECEDES:
					miners.add(new CatalysisPrecedesMiner(blacklist));
					break;
				case CHEMICAL_AFFECTS:
					miners.add(new ChemicalAffectsThroughBindingMiner(blacklist));
					miners.add(new ChemicalAffectsThroughControlMiner());
					break;
				case IN_COMPLEX_WITH:
					miners.add(new InComplexWithMiner());
					break;
				case NEIGHBOR_OF:
					miners.add(new NeighborOfMiner());
					break;
				case INTERACTS_WITH:
					miners.add(new InteractsWithMiner());
					break;
				case REACTS_WITH:
					miners.add(new ReactsWithMiner(blacklist));
					break;
				case USED_TO_PRODUCE:
					miners.add(new UsedToProduceMiner(blacklist));
					break;
				default: throw new RuntimeException("There is an unhandled sif type: " + type);
			}
		}
	}

	/**
	 * Sets the blacklist that manages IDs of ubique molecules. This is not mandatory but need if
	 * ubiquitous small molecules are needed to be handled.
	 * @param blacklist for identifying ubiquitous small molecules
	 */
	public void setBlacklist(Blacklist blacklist)
	{
		this.blacklist = blacklist;
	}

	/**
	 * Searches the given model with the contained miners.
	 * @param model model to search
	 * @return sif interactions
	 */
	public Set<SIFInteraction> searchSIF(Model model)
	{
		if (miners == null) initMiners();

		Map<SIFInteraction, SIFInteraction> map = new HashMap<SIFInteraction, SIFInteraction>();

		for (SIFMiner miner : miners)
		{
			Map<BioPAXElement,List<Match>> matches = Searcher.search(model, miner.getPattern());

			for (List<Match> matchList : matches.values())
			{
				for (Match m : matchList)
				{
					SIFInteraction sif = miner.createSIFInteraction(m, idFetcher);

					if (sif != null && sif.hasIDs() && (types== null || types.contains(sif.type)))
					{
						if (map.containsKey(sif))
						{
							SIFInteraction existing = map.get(sif);
							existing.mergeWith(sif);
						}
						else map.put(sif, sif);
					}
				}
			}
		}
		return new HashSet<SIFInteraction>(map.values());
	}

	/**
	 * Searches the given model with the contained miners. Writes the textual result to the given
	 * output stream. Closes the stream at the end. Produces simplest version of SIF format.
	 * @param model model to search
	 * @param out stream to write
	 * @return true if any output produced successfully
	 */
	public boolean searchSIF(Model model, OutputStream out)
	{
		return searchSIF(model, out, false);
	}

	/**
	 * Searches the given model with the contained miners. Writes the textual result to the given
	 * output stream. Closes the stream at the end.
	 * @param model model to search
	 * @param out stream to write
	 * @param withMediators whether to write the IDs of the mediator elements to the output
	 * @return true if any output produced successfully
	 */
	public boolean searchSIF(Model model, OutputStream out, final boolean withMediators)
	{
		return searchSIF(model, out, new SIFToText()
		{
			@Override
			public String convert(SIFInteraction inter)
			{
				return inter.toString(withMediators);
			}
		});
	}

	/**
	 * Searches the given model with the contained miners. Writes the textual result to the given
	 * output stream. Closes the stream at the end.
	 * @param model model to search
	 * @param out stream to write
	 * @param stt sif to text converter
	 * @return true if any output produced successfully
	 */
	public boolean searchSIF(Model model, OutputStream out, SIFToText stt)
	{
		Set<SIFInteraction> inters = searchSIF(model);

		if (!inters.isEmpty())
		{
			List<SIFInteraction> interList = new ArrayList<SIFInteraction>(inters);
			Collections.sort(interList);
			try
			{
				boolean first = true;
				OutputStreamWriter writer = new OutputStreamWriter(out);
				for (SIFInteraction inter : interList)
				{
					if (first) first = false;
					else writer.write("\n");

					writer.write(stt.convert(inter));
				}
				writer.close();
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
