package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.DifferentialModificationUtil;

import java.io.*;
import java.util.*;

/**
 * This class goes over the state change pattern results and writes down the gained and lost
 * modifications through these directed relations.
 *
 * @author Ozgun Babur
 */
public class DeltaFeatureExtractor
{
	private Map<String, Map<String, Set<String>>> gains;
	private Map<String, Map<String, Set<String>>> losses;
	private Map<String, Map<String, Set<String>>> mediators;

	private AbstractMiner[] miners = new AbstractMiner[]{
		new CSCO(), new CSCO_ButPart(), new CSCO_CtrlAndPart(), new CSCO_ThrContSmMol()};

	abstract class AbstractMiner extends AbstractSIFMiner
	{
		public AbstractMiner()
		{
			super(SIFEnum.CONTROLS_STATE_CHANGE_OF);
		}

		@Override
		public abstract Pattern constructPattern();

		@Override
		public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out) throws IOException
		{
			for (List<Match> matchList : matches.values())
			{
				for (Match m : matchList)
				{
					String s1 = getIdentifier(m, getSourceLabel());
					String s2 = getIdentifier(m, getTargetLabel());

					if (s1 == null || s2 == null) continue;

					SimplePhysicalEntity speIn = (SimplePhysicalEntity)
						m.get(getInputSimplePELabel(), getPattern());

					SimplePhysicalEntity speOut = (SimplePhysicalEntity)
						m.get(getOutputSimplePELabel(), getPattern());

					Set<ModificationFeature>[] modif =
						DifferentialModificationUtil.getChangedModifications(speIn, speOut);

					if (!modif[0].isEmpty()) collect(s1, s2, modif[0], gains);
					if (!modif[1].isEmpty()) collect(s1, s2, modif[1], losses);

					if (!modif[0].isEmpty() || !modif[1].isEmpty())
					{
						if (!mediators.containsKey(s1)) mediators.put(s1, new HashMap<String, Set<String>>());
						if (!mediators.get(s1).containsKey(s2)) mediators.get(s1).put(s2, new HashSet<String>());

						List<BioPAXElement> meds = m.get(getMediatorLabels(), getPattern());
						for (BioPAXElement med : meds)
						{
							mediators.get(s1).get(s2).add(med.getRDFId());
						}
					}
				}
			}
		}

		private void collect(String s1, String s2, Set<ModificationFeature> modificationFeatures,
			Map<String, Map<String, Set<String>>> map)
		{
			if (!map.containsKey(s1)) map.put(s1, new HashMap<String, Set<String>>());
			if (!map.get(s1).containsKey(s2)) map.get(s1).put(s2, new HashSet<String>());

			for (ModificationFeature mf : modificationFeatures)
			{
				String s = toString(mf);
				if (s != null) map.get(s1).get(s2).add(s);
			}
		}

		String getInputSimplePELabel()
		{
			return "input simple PE";
		}

		String getOutputSimplePELabel()
		{
			return "output simple PE";
		}

		@Override
		public String getSourceLabel()
		{
			return "controller PR";
		}

		@Override
		public String getTargetLabel()
		{
			return "changed PR";
		}

		@Override
		public String[] getMediatorLabels()
		{
			return new String[]{"Control", "Conversion"};
		}

		protected String toString(ModificationFeature mf)
		{
			String term = getModificationTerm(mf);
			if (term != null)
			{
				String loc = getPositionInString(mf);
				return term + loc;
			}
			return null;
		}
	}

	class CSCO extends AbstractMiner
	{
		@Override
		public Pattern constructPattern()
		{
			return PatternBox.controlsStateChange();
		}
	}

	class CSCO_CtrlAndPart extends AbstractMiner
	{
		@Override
		public Pattern constructPattern()
		{
			return PatternBox.controlsStateChangeBothControlAndPart();
		}

		@Override
		public String getTargetLabel()
		{
			return "changed ER";
		}
	}

	class CSCO_ButPart extends AbstractMiner
	{
		@Override
		public Pattern constructPattern()
		{
			return PatternBox.controlsStateChangeButIsParticipant();
		}

		@Override
		public String getSourceLabel()
		{
			return "controller ER";
		}

		@Override
		public String getTargetLabel()
		{
			return "changed ER";
		}

		@Override
		public String[] getMediatorLabels()
		{
			return new String[]{"Conversion"};
		}
	}

	class CSCO_ThrContSmMol extends AbstractMiner
	{
		@Override
		public Pattern constructPattern()
		{
			return PatternBox.controlsStateChangeThroughControllerSmallMolecule(blacklist);
		}

		@Override
		public String getSourceLabel()
		{
			return "upper controller PR";
		}

		@Override
		public String getTargetLabel()
		{
			return "changed ER";
		}

		@Override
		public String[] getMediatorLabels()
		{
			return new String[]{"upper Control", "upper Conversion", "Control", "Conversion"};
		}
	}

	public DeltaFeatureExtractor()
	{
		gains = new HashMap<String, Map<String, Set<String>>>();
		losses = new HashMap<String, Map<String, Set<String>>>();
		mediators = new HashMap<String, Map<String, Set<String>>>();
	}

	public void setBlacklist(Blacklist blacklist)
	{
		for (AbstractMiner miner : miners)
		{
			miner.setBlacklist(blacklist);
		}
	}

	public void mineAndCollect(Model model)
	{
		for (AbstractMiner miner : miners)
		{
			Map<BioPAXElement, List<Match>> matches = Searcher.search(model, miner.getPattern());

			try { miner.writeResult(matches, null);
			} catch (IOException e){e.printStackTrace();}
		}
	}

	public void writeResults(String filename) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write("Source\tType\tTarget\tGained\tLost\tMediators");

		Set<String> s1s = new HashSet<String>(gains.keySet());
		s1s.addAll(losses.keySet());

		for (String s1 : s1s)
		{
			Set<String> s2s = new HashSet<String>();
			if (gains.containsKey(s1)) s2s.addAll(gains.get(s1).keySet());
			if (losses.containsKey(s1)) s2s.addAll(losses.get(s1).keySet());

			for (String s2 : s2s)
			{
				writer.write("\n" + s1 + "\t" + SIFEnum.CONTROLS_STATE_CHANGE_OF.getTag() +
					"\t" + s2 + "\t");

				if (gains.containsKey(s1) && gains.get(s1).containsKey(s2))
				{
					writer.write(gains.get(s1).get(s2).toString());
				}
				writer.write("\t");
				if (losses.containsKey(s1) && losses.get(s1).containsKey(s2))
				{
					writer.write(losses.get(s1).get(s2).toString());
				}
				writer.write("\t" + toString(mediators.get(s1).get(s2)));
			}
		}

		writer.close();
	}

	private String toString(Set<String> set)
	{
		String s = "";
		for (String s1 : set)
		{
			s += " " + s1;
		}
		return s.substring(1);
	}

	public static void main(String[] args) throws IOException
	{
		Blacklist black = new Blacklist("blacklist.txt");
		DeltaFeatureExtractor dfe = new DeltaFeatureExtractor();
		dfe.setBlacklist(black);
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(new FileInputStream(
			"Pathway Commons.5.Detailed_Process_Data.BIOPAX.owl"));
//			"Pathway Commons.5.NCI_Nature.BIOPAX.owl"));
		dfe.mineAndCollect(model);
		dfe.writeResults("DeltaFeatures.txt");
	}
}
