package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.miner.*;
import org.biopax.paxtools.pattern.util.ChemicalNameNormalizer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * This class is for investigating the connections of small molecules in a BioPAX file. Those
 * methods may help in tuning the blacklist generation.
 *
 * @author Ozgun Babur
 */
public class UbiqueStatHelper
{
	private void generateHelperSIF(Model model, String outFile) throws FileNotFoundException
	{
		SIFSearcher searcher = new SIFSearcher(new Fetcher(new ChemicalNameNormalizer(model)),
			new ChemicalAffectsThroughControlMiner(), new UsedToProduceMiner());

		searcher.searchSIF(model, new FileOutputStream(outFile));
	}


	/**
	 * Class to fetch the ID of the small molecule.
	 */
	class Fetcher extends CommonIDFetcher implements IDFetcher
	{
		ChemicalNameNormalizer normalizer;

		Fetcher(ChemicalNameNormalizer normalizer)
		{
			this.normalizer = normalizer;
		}

		@Override
		public Set<String> fetchID(BioPAXElement ele)
		{
			if (ele instanceof SmallMoleculeReference)
			{
				return Collections.singleton(normalizer.getName((SmallMoleculeReference) ele));
			}

			return super.fetchID(ele);
		}
	}

	private void generateStats(String helperSIF, String statFile) throws IOException
	{
		final Map<String, Set<String>> affectMap = new HashMap<String, Set<String>>();
		final Map<String, Set<String>> dwMap = new HashMap<String, Set<String>>();
		final Map<String, Set<String>> upMap = new HashMap<String, Set<String>>();

		Scanner sc = new Scanner(new File(helperSIF));
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");

			if (token[1].equals(SIFEnum.USED_TO_PRODUCE.getTag()))
			{
				if (!dwMap.containsKey(token[0])) dwMap.put(token[0], new HashSet<String>());
				if (!upMap.containsKey(token[2])) upMap.put(token[2], new HashSet<String>());
				dwMap.get(token[0]).add(token[2]);
				upMap.get(token[2]).add(token[0]);
			}
			else if (token[1].equals(SIFEnum.CHEMICAL_AFFECTS.getTag()))
			{
				if (!affectMap.containsKey(token[0])) affectMap.put(token[0], new HashSet<String>());
				affectMap.get(token[0]).add(token[2]);
			}
		}

		Set<String> degreeSet = new HashSet<String>(dwMap.keySet());
		degreeSet.addAll(upMap.keySet());
		List<String> degreeList = new ArrayList<String>(degreeSet);
		List<String> ctrlList = new ArrayList<String>(affectMap.keySet());

		Collections.sort(degreeList, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				return getDegree(o2, upMap, dwMap).compareTo(getDegree(o1, upMap, dwMap));
			}
		});

		Collections.sort(ctrlList, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				Integer i1 = affectMap.containsKey(o1) ? affectMap.get(o1).size() : 0;
				Integer i2 = affectMap.containsKey(o2) ? affectMap.get(o2).size() : 0;
				return i2.compareTo(i1);
			}
		});

		BufferedWriter writer = new BufferedWriter(new FileWriter(statFile));

		for (String gene : degreeList)
		{
			writer.write(gene + "\t" + getDegree(gene, upMap, dwMap) + "\t" +
				getUpOnly(gene, upMap, dwMap) + "\t" + getDwOnly(gene, upMap, dwMap) + "\t" +
				(affectMap.containsKey(gene) ? affectMap.get(gene).size() : 0) + "\n");
		}

		writer.close();

	}

	private Integer getDegree(String name, Map<String, Set<String>> upMap, Map<String, Set<String>> dwMap)
	{
		int d = 0;
		if (upMap.containsKey(name)) d += upMap.get(name).size();
		if (dwMap.containsKey(name)) d += dwMap.get(name).size();
		return d;
	}

	private int getUpOnly(String name, Map<String, Set<String>> upMap, Map<String, Set<String>> dwMap)
	{
		if (!upMap.containsKey(name)) return 0;
		Set<String> up = new HashSet<String>(upMap.get(name));
		if (dwMap.containsKey(name)) up.removeAll(dwMap.get(name));
		return up.size();
	}

	private int getDwOnly(String name, Map<String, Set<String>> upMap, Map<String, Set<String>> dwMap)
	{
		if (!dwMap.containsKey(name)) return 0;
		Set<String> dw = new HashSet<String>(dwMap.get(name));
		if (upMap.containsKey(name)) dw.removeAll(upMap.get(name));
		return dw.size();
	}

	public static void main(String[] args) throws IOException
	{
		UbiqueStatHelper ush = new UbiqueStatHelper();

		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new GZIPInputStream(new URL("http://pathwaycommons.baderlab.org/downloads/Pathway%20Commons.8.Detailed.BIOPAX.owl.gz").openStream()));
		Model model = h.convertFromOWL(new FileInputStream("/media/babur/6TB1/REACH-cards/REACH.owl"));

		String helper = "/media/babur/6TB1/REACH-cards/ubique-helper.sif";
		ush.generateHelperSIF(model, helper);
		ush.generateStats(helper, "ubique-stat.txt");
	}
}
