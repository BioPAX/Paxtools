package org.biopax.paxtools.causality;

import org.apache.lucene.analysis.Tokenizer;
import org.biopax.paxtools.causality.analysis.SIFLinker;
import org.biopax.paxtools.causality.analysis.Traverse;
import org.biopax.paxtools.causality.data.CBioPortalAccessor;
import org.biopax.paxtools.causality.data.CancerStudy;
import org.biopax.paxtools.causality.data.CaseList;
import org.biopax.paxtools.causality.data.GeneticProfile;
import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Change;
import org.biopax.paxtools.causality.util.HGNCUtil;
import org.biopax.paxtools.causality.util.Overlap;
import org.biopax.paxtools.causality.util.Summary;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class MutexTester
{
	public static final DecimalFormat fmt = new DecimalFormat("0.0000");

	@Test
	@Ignore
	public void exploreCBioPortalMutex() throws IOException, CloneNotSupportedException
	{
		// Load network

//		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2.owl"));

		Map<String, AlterationPack> map = readAlterations();
		SIFLinker linker = new SIFLinker();
		linker.load("/home/ozgun/Desktop/SIF.txt");

		Traverse trav = linker.traverse;
		
		double thr = -0.05;

		List<AltBundle> pairs = getMutexPairs(map, trav, thr);
		List<AltBundle> modules = new ArrayList<AltBundle>();

		Set<String> encountered = new HashSet<String>();
		for (AltBundle bun : pairs)
		{
			growGroup(bun, map, trav, thr, 1);
			if (bun.alts.size() < 3) continue;
			if (bun.calcAveragePValScore() > 0.05) continue;
//			if (bun.calcCoverage() < 0.5) continue;

			if (encountered.contains(bun.id)) continue;
			else encountered.add(bun.id);

			modules.add(bun);
		}

		Collections.sort(modules, new Comparator<AltBundle>()
		{
			@Override
			public int compare(AltBundle b1, AltBundle b2)
			{
				return new Double(b2.calcCoverage()).compareTo(b1.calcCoverage());
//				return new Double(b2.calcAveragePValScore()).compareTo(b1.calcAveragePValScore());
			}
		});
		
		for (AltBundle bun : modules)
		{				
			Set<String> genes = new HashSet<String>(bun.getAllGenes());
//			genes.remove(bun.seed);
//			List<String> rels = linker.linkProgressive(genes, Collections.singleton(bun.seed), 3);
			List<String> rels = linker.linkProgressive(genes, genes, 0);
			if (rels.isEmpty()) continue;

			bun.sortToMostAltered();
			System.out.println(bun.getGeneNamesInString());
			System.out.println(bun);
			System.out.println(bun.getPrint());

			for (double v : bun.calcPVals()) System.out.println(fmt.format(v));

			for (String rel : rels) if (!rel.contains("TRANSCRIPTION")) System.out.println(rel);
			System.out.println();
		}
	}

	private Map<String, AlterationPack> readAlterations() throws IOException
	{
		// cBio portal configuration
		
		Dataset data = breast;
		
		if (new File(data.filename).exists())
		{
			return AlterationPack.readFromFile(data.filename);
		}
		
		CBioPortalAccessor cBioPortalAccessor = new CBioPortalAccessor();
		CancerStudy cancerStudy = cBioPortalAccessor.getCancerStudies().get(data.study); // GBM
		cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);

		List<GeneticProfile> geneticProfilesForCurrentStudy =
			cBioPortalAccessor.getGeneticProfilesForCurrentStudy();
		List<GeneticProfile> gp = new ArrayList<GeneticProfile>();
		for (int prof : data.profile)
		{
			gp.add(geneticProfilesForCurrentStudy.get(prof));
		}
		cBioPortalAccessor.setCurrentGeneticProfiles(gp);

		List<CaseList> caseLists = cBioPortalAccessor.getCaseListsForCurrentStudy();
		cBioPortalAccessor.setCurrentCaseList(caseLists.get(data.caseList));

		Set<String> syms = readSymbols();
//		Set<String> syms = readSymbolsTemp();

		System.out.println("syms.size() = " + syms.size());
		long time = System.currentTimeMillis();

		Map<String, AlterationPack> map = new HashMap<String, AlterationPack>();

		for (String sym : syms)
		{
			AlterationPack alt = cBioPortalAccessor.getAlterations(sym);
			if (alt.isAltered())
			{
				map.put(sym, alt);
			}
		}
		System.out.println("map.size() = " + map.size());
		time = System.currentTimeMillis() - time;
		System.out.println("read in " + (time / 1000D) + " seconds");
		
		AlterationPack.writeToFile(map, data.filename);
		
		return map;
	}

	private Set<String> readSymbols() throws IOException
	{
		Set<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/dist.txt"));

		reader.readLine();
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");
			set.add(token[0]);
			set.add(token[1]);
		}

		reader.close();
		return set;
	}
	
	private Set<String> readSymbolsTemp() throws IOException
	{
		Set<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/temp.txt"));

		reader.readLine();
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			StringTokenizer tokenizer = new StringTokenizer(line);
			while(tokenizer.hasMoreTokens())
			{
				String token = tokenizer.nextToken();
				if (!token.startsWith("0"))
				{
					set.add(token);
				}
			}
		}

		reader.close();
		return set;
	}

	private String getSymbol(ProteinReference pr)
	{
		for (Xref xref : pr.getXref())
		{
			if (xref.getDb().equals("HGNC"))
			{
				String id = xref.getId().substring(xref.getId().indexOf(":") + 1);
				return HGNCUtil.getSymbol(Integer.parseInt(id));
			}
		}
		return null;
	}

	public void printAlterationCountsPerSample(Map<String, AlterationPack> map)
	{
		AlterationPack aPack = map.values().iterator().next();
		int size = aPack.getSize();
		Set<Alteration> types = aPack.getAlterationTypes();
		
		int[][] cnt = new int[types.size()][size];

		int i = 0;
		for (Alteration type : types)
		{
			for (int j = 0; j < cnt[i].length; j++)
			{
				for (String sm : map.keySet())
				{
					AlterationPack pack = map.get(sm);
					if (pack.getChange(type, j).isAltered()) cnt[i][j]++;
				}
			}

			i++;
		}

		System.out.print("Sample");
		for (Alteration type : types)
		{
			System.out.print("\t" + type);
		}
		for (int j = 0; j < cnt[0].length; j++)
		{
			System.out.print("\n" + j);
			for (i = 0; i < cnt.length; i++)
			{
				System.out.print("\t" + cnt[i][j]);
			}
			System.out.println();
		}
	}
	
	private void addAlternatives(List<AltBundle> bundles, 
		Map<AlterationPack, List<AlterationPack>> cooc, double thr)
	{
		for (AltBundle bundle : bundles)
		{
			int i = 0;
			for (AlterationPack pack : new ArrayList<AlterationPack>(bundle.alts))
			{
				if (cooc.containsKey(pack))
				{
					for (AlterationPack other : cooc.get(pack))
					{
						double pv = bundle.calcPVal(i, other);
						if (pv <= -0 && pv > -thr)
						{
							if (bundle.other == null) bundle.other = new List[bundle.alts.size()];
							if (bundle.other[i] == null) 
								bundle.other[i] = new ArrayList<AlterationPack>();
							bundle.other[i].add(other);
						}
					}
				}
				i++;
			}
		}
	}
	
	class AltBundle implements Comparable, Cloneable
	{
		String id;
		List<AlterationPack> alts;
		List<AlterationPack>[] other;
		Alteration key;
		double pval;
		boolean mutex;
		String seed;
		Map<AlterationPack, Alteration> useMap;

		public AltBundle(AlterationPack alt1, AlterationPack alt2, Alteration key1, Alteration key2, Alteration key, boolean mutex)
		{
			useMap = new HashMap<AlterationPack, Alteration>();
			useMap.put(alt1, key1);
			useMap.put(alt2, key2);
			this.mutex = mutex;

			this.alts = new ArrayList<AlterationPack>();
			this.alts.add(alt1);
			this.alts.add(alt2);
			this.key = key;
			this.seed = alt1.getId();

			updateID();
			pval = calcPVal();
		}

		public void add(AlterationPack pack, Alteration addKey)
		{
			assert other == null;

			alts.add(pack);
			useMap.put(pack, addKey);
			Collections.sort(alts, new Comparator<AlterationPack>()
			{
				@Override
				public int compare(AlterationPack alt1, AlterationPack alt2)
				{
					return alt1.getId().compareTo(alt2.getId());
				}
			});

			pval = calcPVal();

			// Update ID
			updateID();
		}

		private void updateID()
		{
			id = "";
			for (int i = 0; i < alts.size(); i++)
			{
				id += "  " + alts.get(i).getId();
				id += (useMap.get(alts.get(i)) == Alteration.ACTIVATING) ? "+" : "-";
			}
			id = id.trim();
		}

		public void setSeed(String seed)
		{
			this.seed = seed;
		}

		private double calcPVal(int replace, AlterationPack with)
		{
			AlterationPack orig = alts.remove(replace);
			alts.add(replace, with);
			double pval = calcPVal();
			alts.remove(replace);
			alts.add(replace, orig);
			return pval;
		}
		
		private double calcPVal()
		{
			return getWorstPval(calcPVals());
		}
		
//		public double[] getPValScores()
//		{
//			double[] pval = calcPVals();
//			for (int i = 0; i < pval.length; i++)
//			{
//				pval[i] = -Math.log(-pval[i]);
//			}
//			return pval;
//		}
		
		public double calcAveragePValScore()
		{
			return Summary.geometricMean(calcPVals());
		}
		
		private double[] calcPVals()
		{
			if (alts.size() == 2)
			{
				double pval = Overlap.calcAlterationOverlapPval(
					alts.get(0).get(useMap.get(alts.get(0))), 
					alts.get(1).get(useMap.get(alts.get(1))));

				if ((isMutex() && pval > 0) || (!isMutex() && pval < 0)) pval = -1;
				
				return new double[]{pval};
			}
			
			double[] pval = new double[alts.size()];
//			double[] pval = new double[(alts.size() * (alts.size() - 1)) / 2];
//			boolean[] use = new boolean[alts.get(0).getSize()];

			int x = 0;
			
			for (int i = 0; i < alts.size(); i++)
			{
				Change[] others = new Change[alts.get(0).getSize()];

				for (int k = 0; k < others.length; k++)
				{
					others[k] = Change.NO_CHANGE;

					for (int j = 0; j < alts.size(); j++)
					{
						if (j == i) continue;

						if (alts.get(j).get(useMap.get(alts.get(j)))[k].isAltered())
						{
							others[k] = Change.ACTIVATING;
							break;
						}
					}
				}

				pval[x++] = Overlap.calcAlterationOverlapPval(
					alts.get(i).get(useMap.get(alts.get(i))), others);

				
//				for (int j = i + 1; j < alts.size(); j++)
//				{
//					if (mutex)
//					{
//						// Update use array with other alterations
//	
//						for (int k = 0; k < use.length; k++)
//						{
//							use[k] = true;
//							
//							for (int l = 0; l < alts.size(); l++)
//							{
//								if (l == i || l == j) continue;
//								if (alts.get(l).get(useMap.get(alts.get(l)))[k].isAltered())
//								{
//									use[k] = false;
//									break;
//								}
//							}
//						}
//	
//						// Calc pval for the pair
//						
//						double pv = Overlap.calcAlterationOverlapPval(
//							alts.get(i).get(useMap.get(alts.get(i))), 
//							alts.get(j).get(useMap.get(alts.get(j))), use);
////							alts.get(i).get(key), alts.get(j).get(key));
//
//						pval[x++] = pv;
//					}
//					else
//					{
//						// Calc pval for the pair
//
//						double pv = Overlap.calcAlterationOverlapPval(
//							alts.get(i).get(key), alts.get(j).get(key));
//
//						pval[x++] = pv;
//					}
//				}
			}

			assert x == pval.length;

			return pval;
		}

		public double getWorstPval(double[] pval)
		{
			if (pval.length == 1) return pval[0];

			double w = 0;

			for (double pv : pval)
			{
				if (mutex)
				{
					if (pv > 0) return 1;
					if (w > pv) w = pv;
				}
				else
				{
					if (pv < 0) return 1;
					if (w < pv) w = pv;
				}
			}
			return w;
		}

		public double calcCoverage()
		{
			int total = alts.get(0).get(key).length;
			int altered = 0;
			
			for (int i = 0; i < total; i++)
			{
				for (AlterationPack alt : alts)
				{
					if (alt.get(key)[i].isAltered())
					{
						altered++;
						break;
					}
				}
			}
			return altered / (double) total;
		}
		
		@Override
		public int compareTo(Object o)
		{
			if (o instanceof AltBundle)
			{
				AltBundle bundle = (AltBundle) o;
				if (alts.size() == bundle.alts.size())
					return new Double(Math.abs(pval)).compareTo(Math.abs(bundle.pval));
				return new Integer(bundle.alts.size()).compareTo(alts.size());
			}
			else return 0;
		}
		
		public double absPVal()
		{
			return Math.abs(pval);
		}
		
		public boolean isMutex()
		{
			return mutex;
		}

		@Override
		public String toString()
		{
			return id + "\t" + fmt.format(calcAveragePValScore()) + "\tseed: " + seed + "\t coverage: " +
				fmt.format(getSeed().calcAlteredRatio(key)) + " --> " +  fmt.format(calcCoverage());
		}

		public String getPrint()
		{
			return getPrint(getPrintOrdering(alts));
		}
		
		public AlterationPack getSeed()
		{
			for (AlterationPack alt : alts)
			{
				if (alt.getId().equals(seed)) return alt;
			}
			return null;
		}
		
		public String getPrint(List<Integer> order)
		{
			int i = 0;
			StringBuilder s = new StringBuilder();
			for (AlterationPack alt : alts)
			{
				if (s.length() > 0) s.append("\n");
				s.append(alt.getPrint(useMap.get(alt), order));
				if (other != null && other[i] != null)
				{
					for (AlterationPack pack : other[i])
					{
						s.append("\n").append(pack.getPrint(key, order)).append(" *");
					}
				}
				i++;
			}
			return s.toString();
		}

		protected void sortToMostAltered()
		{
			Collections.sort(alts, new Comparator<AlterationPack>()
			{
				@Override
				public int compare(AlterationPack alt1, AlterationPack alt2)
				{
					return new Integer(alt2.countAltered(useMap.get(alt2))).compareTo(alt1.countAltered(useMap.get(alt1)));
				}
			});

			updateID();
		}
		
		private List<Integer> getPrintOrdering(List<AlterationPack> alts)
		{
			List<Integer> order = new ArrayList<Integer>();

			for (AlterationPack alt : alts)
			{
				Change[] ch = alt.get(useMap.get(alt));

				for (int i = 0; i < ch.length; i++)
				{
					if (ch[i].isAltered() && !order.contains(i)) order.add(i);
				}
			}
			return order;
		}

		public List<String> getAllGenes()
		{
			List<String> list = new ArrayList<String>(alts.size());
			int i = 0;
			for (AlterationPack alt : alts)
			{
				list.add(alt.getId());

				if (other != null && other[i] != null)
				for (AlterationPack ot : other[i])
				{
					list.add(ot.getId());
				}
				
				i++;
			}
			return list;
		}
		
		public String getGeneNamesInString()
		{
			String s = "";
			for (String g : getAllGenes())
			{
				s += " " + g;
			}
			return s;
		}
		
		@Override
		public int hashCode()
		{
			int h = 0;
			for (AlterationPack alt : alts)
			{
				h += alt.hashCode();
			}
			return h;
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof AltBundle)
			{
				AltBundle bundle = (AltBundle) o;
				
				if (bundle.isMutex() == isMutex())
				{
					if (alts.size() == bundle.alts.size())
					{
						if (alts.containsAll(bundle.alts)) return true;
					}
				}
			}
			return false;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException
		{
			AltBundle b = (AltBundle) super.clone();
			b.alts = new ArrayList<AlterationPack>(alts);
			b.useMap = new HashMap<AlterationPack, Alteration>(useMap);
			return b;
		}
		
		public boolean contains(AlterationPack pack)
		{
			return this.alts.contains(pack);
		}
	}

	private List<AltBundle> getMutexPairs(Map<String, AlterationPack> altMap, Traverse trav, 
		double thr)
	{
		List<AltBundle> pairs = new ArrayList<AltBundle>();

		for (String seed : altMap.keySet())
		{
			Set<String> neigh = 
				trav.goBFS(Collections.singleton(seed), Collections.EMPTY_SET, false);

			AltBundle best = null;
			
			for (String n : neigh)
			{
				if (altMap.containsKey(n))
				{
					best = tryAndPutPair(altMap, thr, seed, best, n, Alteration.ACTIVATING, Alteration.ACTIVATING);
					best = tryAndPutPair(altMap, thr, seed, best, n, Alteration.INHIBITING, Alteration.ACTIVATING);
					best = tryAndPutPair(altMap, thr, seed, best, n, Alteration.INHIBITING, Alteration.INHIBITING);
					best = tryAndPutPair(altMap, thr, seed, best, n, Alteration.ACTIVATING, Alteration.INHIBITING);
				}
			}

			if (best != null) pairs.add(best);

		}
		Collections.sort(pairs);
		return pairs;
	}

	private AltBundle tryAndPutPair(Map<String, AlterationPack> altMap, double thr, String seed,
		AltBundle best, String n, Alteration seedKey, Alteration neighKey)
	{
		AltBundle bun =
			new AltBundle(altMap.get(seed), altMap.get(n), seedKey, neighKey, Alteration.ANY, true);

		if (bun.pval < 0 && bun.pval > thr)
		{
			if (best == null || best.pval < bun.pval) best = bun;
		}
		return best;
	}

	private void growGroup(AltBundle bun, Map<String, AlterationPack> altMap,
		Traverse trav, double thr, int limit)
	{
		Set<String> breadth = new HashSet<String>();
		breadth.add(bun.seed);
		Set<String> visited = new HashSet<String>();
		for (AlterationPack alt : bun.alts)
		{
			visited.add(alt.getId());
		}

		for (int i = 1; i <= limit; i++)
		{
			breadth = trav.goBFS(breadth, visited, false);
			
			Set<AlterationPack> alts = new HashSet<AlterationPack>();
			for (String n : breadth)
			{
				if (altMap.containsKey(n))
				{
					alts.add(altMap.get(n));
				}
			}

			Set<String> newBreadth = new HashSet<String>();

			boolean loop = true;
			while (loop)
			{
				AlterationPack ap = enlarge(bun, alts, thr);
				if (ap != null)
				{
					alts.remove(ap);
					newBreadth.add(ap.getId());
				}
				else loop = false;
			}
			visited.addAll(breadth);
			breadth = newBreadth;
		}
	}
	
	private AlterationPack enlarge(AltBundle bun, Collection<AlterationPack> candidates, double thr)
	{
		AlterationPack bestPack = null;
		double bestPval = -1;
		Alteration bestKey = null;
		
		for (AlterationPack can : candidates)
		{
			AltBundle cp = null;
			try	{
				cp = (AltBundle) bun.clone();
			} catch (CloneNotSupportedException e) { e.printStackTrace(); }

			cp.add(can, Alteration.ACTIVATING);
			if (cp.pval < 0)
			{
				if (cp.pval > bestPval)
				{
					bestPval = cp.pval;
					bestPack = can;
					bestKey = Alteration.ACTIVATING;
				}
			}
			try	{
				cp = (AltBundle) bun.clone();
			} catch (CloneNotSupportedException e) { e.printStackTrace(); }

			cp.add(can, Alteration.INHIBITING);
			if (cp.pval < 0)
			{
				if (cp.pval > bestPval)
				{
					bestPval = cp.pval;
					bestPack = can;
					bestKey = Alteration.INHIBITING;
				}
			}
		}
		if (bestPval > thr)
		{
			bun.add(bestPack, bestKey);
			return bestPack;
		}
		else return null;
	}

	static class Dataset
	{
		public Dataset(String filename, int study, int caseList, int[] profile)
		{
			this.filename = filename;
			this.study = study;
			this.caseList = caseList;
			this.profile = profile;
		}

		String filename;
		int study;
		int caseList;
		int[] profile;
	}
	
	public static final Dataset glioblastoma = new Dataset(
		"Glioblastoma.txt", 5, 0, new int[]{0, 7, 10});
	public static final Dataset ovarian = new Dataset(
		"Ovarian.txt", 16, 0, new int[]{0, 7, 12});
	public static final Dataset breast = new Dataset(
		"Breast.txt", 2, 0, new int[]{0, 7, 10});
	public static final Dataset colon = new Dataset(
		"Colon.txt", 4, 0, new int[]{0, 7, 10});
}
