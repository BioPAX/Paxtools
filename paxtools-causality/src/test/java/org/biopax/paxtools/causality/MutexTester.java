package org.biopax.paxtools.causality;

import org.apache.lucene.analysis.Tokenizer;
import org.biopax.paxtools.causality.data.CBioPortalAccessor;
import org.biopax.paxtools.causality.data.CancerStudy;
import org.biopax.paxtools.causality.data.CaseList;
import org.biopax.paxtools.causality.data.GeneticProfile;
import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Change;
import org.biopax.paxtools.causality.util.HGNCUtil;
import org.biopax.paxtools.causality.util.Overlap;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class MutexTester
{
	@Test
	@Ignore
	public void exploreCBioPortalMutex() throws IOException, CloneNotSupportedException
	{
		// Load network

//		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2.owl"));

		Map<String, AlterationPack> map = readAlterations();

		double thr = 0.01;
		List<AltBundle> mutex = formBundles(map, true, thr);
		for (AltBundle bundle : mutex) bundle.sortToMostAltered();

		Map<AlterationPack, List<AlterationPack>> coocMap = getCoocMap(map, Alteration.ANY, 0.01);
		addAlternatives(mutex, coocMap, thr);
		clearRedundancies(mutex);
		System.out.println("bundles size = " + mutex.size());

//		List<AltBundle> coocu = formBundles(map, false, thr);

//		AltBundle b = mutex.get(1);
//		List<AlterationPack> sorted = b.sortToMostAltered(b.alts);
//		List<Integer> order = b.getPrintOrdering(sorted);

		int a = 0;
		System.out.println("Mutex bundles\n--------\n");
		for (AltBundle bundle : mutex)
		{
			if (bundle.alts.size() < 3) continue;

//			if (a++ > 10) break;
			System.out.println(bundle);
			System.out.println(bundle.getPrint());
			System.out.println();
		}
//		a = 0;
//		System.out.println("\nCo-occurred bundles\n--------\n");
//		for (AltBundle bundle : coocu)
//		{
//			if (a++ > 100) break;
//			System.out.println(bundle);
//			System.out.println(bundle.getPrint());
//			System.out.println();
//		}
	}

	private Map<AlterationPack, List<AlterationPack>> getCoocMap(Map<String, AlterationPack> map, 
		Alteration key, double thr)
	{
		Map<AlterationPack, List<AlterationPack>> cooc = 
			new HashMap<AlterationPack, List<AlterationPack>>();

		for (AlterationPack pack1 : map.values())
		{
			for (AlterationPack pack2 : map.values())
			{
				if (pack1 == pack2) continue;

				double pv = Overlap.calcAlterationOverlapPval(pack1.get(key), pack2.get(key));
				if (pv >= +0 && pv < thr)
				{
					if (!cooc.containsKey(pack1)) cooc.put(pack1, new ArrayList<AlterationPack>());
					cooc.get(pack1).add(pack2);
				}
			}
		}
		return cooc;
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

	private List<AltBundle> formBundles(Map<String, AlterationPack> map, boolean mutex, double thr)
		throws CloneNotSupportedException
	{
		System.out.println("Forming bundles with " + map.size() + " genes");
		List<AltBundle> bundles = new ArrayList<AltBundle>();

		int i = 0;
		for (String s1 : map.keySet())
		{
			i++;
			for (String s2 : map.keySet())
			{
				if (s1.compareTo(s2) < 0)
				{
					AltBundle bun = new AltBundle(map.get(s1), map.get(s2), Alteration.ANY, mutex);
					if (bun.absPVal() < thr)
					{
						bundles.add(bun);
					}
				}
			}
		}

		System.out.println("bundles.size() = " + bundles.size());

		boolean loop = true;
		Set<AltBundle> processed = new HashSet<AltBundle>();

		while (loop)
		{
			loop = false;

			Set<AltBundle> toAdd = new HashSet<AltBundle>();
			Set<AltBundle> toRem = new HashSet<AltBundle>();

			for (AltBundle bundle : bundles)
			{
				if (processed.contains(bundle)) continue;

				for (String sym : map.keySet())
				{
					if (bundle.contains(map.get(sym))) continue;

					AltBundle b = (AltBundle) bundle.clone();
					b.add(map.get(sym));
					
					if (b.absPVal() < thr)
					{
						toAdd.add(b);
						toRem.add(bundle);
					}
				}
				processed.add(bundle);
				if (!toAdd.isEmpty()) loop = true;
			}
			bundles.addAll(toAdd);
			bundles.removeAll(toRem);
			System.out.println("added = " + toAdd.size());
		}

		Collections.sort(bundles);
		System.out.println("bundles.size() = " + bundles.size());
		return bundles;
	}

	private void clearRedundancies(List<AltBundle> bundles)
	{
		Set<AltBundle> toRem = new HashSet<AltBundle>();
		int i = 0;
		while (i < bundles.size() - 1)
		{
			AltBundle bun = bundles.get(i);
			Set<AlterationPack> set = new HashSet<AlterationPack>(bun.alts);
			if (bun.other != null) 
			{
				for (int k = 0; k < bun.other.length; k++)
				{
					if (bun.other[k] != null)
					{
						set.addAll(bun.other[k]);
					}
				}
			}
			for (int j = i + 1; j < bundles.size(); j++)
			{
				if (set.containsAll(bundles.get(j).alts)) toRem.add(bundles.get(j));
			}

			bundles.removeAll(toRem);
			i++;
		}
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

		public AltBundle(AlterationPack alt1, AlterationPack alt2, Alteration key, boolean mutex)
		{
			id = alt1.getId() + "  " + alt2.getId();
			this.mutex = mutex;

			this.alts = new ArrayList<AlterationPack>();
			this.alts.add(alt1);
			this.alts.add(alt2);
			this.key = key;

			pval = calcPVal();
		}

		public void add(AlterationPack pack)
		{
			assert other == null;

			alts.add(pack);
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
			this.id = alts.get(0).getId();
			for (int i = 1; i < alts.size(); i++)
			{
				this.id += "  " + alts.get(i).getId();
			}
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
			if (alts.size() == 2)
			{
				double pval = Overlap.calcAlterationOverlapPval(
					alts.get(0).get(key), alts.get(1).get(key));

				if ((isMutex() && pval > 0) || (!isMutex() && pval < 0)) pval = -1;
				
				return pval;
			}
			
			double pval = 0;
			boolean[] use = new boolean[alts.get(0).getSize()];

			for (int i = 0; i < alts.size() - 1; i++)
			{
				for (int j = i + 1; j < alts.size(); j++)
				{
					if (mutex)
					{
						// Update use array with other alterations
	
						for (int k = 0; k < use.length; k++)
						{
							use[k] = true;
	
							for (int l = 0; l < alts.size(); l++)
							{
								if (l == i || l == j) continue;
								if (alts.get(l).get(key)[k].isAltered()) 
								{
									use[k] = false;
									break;
								}
							}
						}
	
						// Calc pval for the pair
						
						double pv = Overlap.calcAlterationOverlapPval(
							alts.get(i).get(key), alts.get(j).get(key), use);
						
						if (pv > 0) return 1;
						
						if (pv < pval) pval = pv;
					}
					else
					{
						// Calc pval for the pair

						double pv = Overlap.calcAlterationOverlapPval(
							alts.get(i).get(key), alts.get(j).get(key));

						if (pv < 0) return 1;
						if (pv > pval) pval = pv;
					}
				}
			}
			return pval;
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
			return id + "\t" + pval;
		}

		public String getPrint()
		{
			return getPrint(getPrintOrdering(alts));
		}
		
		public String getPrint(List<Integer> order)
		{
			int i = 0;
			StringBuilder s = new StringBuilder();
			for (AlterationPack alt : alts)
			{
				if (s.length() > 0) s.append("\n");
				s.append(alt.getPrint(key, order));
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
					return new Integer(alt2.countAltered(key)).compareTo(alt1.countAltered(key));
				}
			});
		}
		
		private List<Integer> getPrintOrdering(List<AlterationPack> alts)
		{
			List<Integer> order = new ArrayList<Integer>();

			for (AlterationPack alt : alts)
			{
				Change[] ch = alt.get(key);

				for (int i = 0; i < ch.length; i++)
				{
					if (ch[i].isAltered() && !order.contains(i)) order.add(i);
				}
			}
			return order;
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
			return b;
		}
		
		public boolean contains(AlterationPack pack)
		{
			return this.alts.contains(pack);
		}
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
		"Breast.txt", 3, 0, new int[]{0, 7, 10});
}
