package org.biopax.paxtools.causality;

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
		// cBio portal configuration

		CBioPortalAccessor cBioPortalAccessor = new CBioPortalAccessor();
		CancerStudy cancerStudy = cBioPortalAccessor.getCancerStudies().get(5);
		cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);

		List<GeneticProfile> geneticProfilesForCurrentStudy =
			cBioPortalAccessor.getGeneticProfilesForCurrentStudy();
		List<GeneticProfile> gp = new ArrayList<GeneticProfile>();
		gp.add(geneticProfilesForCurrentStudy.get(7));
		gp.add(geneticProfilesForCurrentStudy.get(6));
		gp.add(geneticProfilesForCurrentStudy.get(10));
		cBioPortalAccessor.setCurrentGeneticProfiles(gp);

		List<CaseList> caseLists = cBioPortalAccessor.getCaseListsForCurrentStudy();
		cBioPortalAccessor.setCurrentCaseList(caseLists.get(0));

		// Load network

//		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2.owl"));

//		Set<String> pass = new HashSet<String>();
//		
//		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/dist.txt"));
//
//		for (String line = reader.readLine(); line != null; line = reader.readLine())
//		{
//			String[] token = line.split("\t");
//			
//			String s1 = token[0];
//			String s2 = token[1];
//
//			if (pass.contains(s1) || pass.contains(s2)) continue;
//
//			AlterationPack alt1 = cBioPortalAccessor.getAlterations(s1);
//			AlterationPack alt2 = cBioPortalAccessor.getAlterations(s2);
//
//			if (!alt1.isAltered()) { pass.add(s1); continue;}
//			if (!alt2.isAltered()) { pass.add(s2); continue;}
//
//			Change[] ch1 = alt1.getChangesMissingRemoved(alt2, Alteration.ANY);
//			if (ch1.length == 0) continue;
//			Change[] ch2 = alt2.getChangesMissingRemoved(alt1, Alteration.ANY);
//
//			double pval = Overlap.calcAlterationOverlapPval(ch1, ch2);
//
//			if (pval < 0 && Math.abs(pval) < 0.05)
//			{
//				System.out.println(s1 + "\t" + s2 + "\t" + pval);
//			}
//		}
//
//		reader.close();
		
		Set<String> syms = readSymbols();

		System.out.println("syms.size() = " + syms.size());

		Map<String, AlterationPack> map = new HashMap<String, AlterationPack>();

		int a = 0;
		for (String sym : syms)
		{
//			if (a++ > 150) break;

			AlterationPack alt = cBioPortalAccessor.getAlterations(sym);
			if (alt.isAltered())
			{
				map.put(sym, alt);
			}
		}

		System.out.println("map.size() = " + map.size());

		double thr = 0.05;
		List<AltBundle> mutex = formBundles(map, true, thr);
		List<AltBundle> coocu = formBundles(map, false, thr);

		a = 0;
		System.out.println("Mutex bundles\n--------\n");
		for (AltBundle bundle : mutex)
		{
//			if (a++ > 10) break;
			System.out.println(bundle);
			System.out.println(bundle.getPrint());
			System.out.println();
		}
		a = 0;
		System.out.println("\nCo-occurred bundles\n--------\n");
		for (AltBundle bundle : coocu)
		{
//			if (a++ > 10) break;
			System.out.println(bundle);
			System.out.println(bundle.getPrint());
			System.out.println();
		}
	}

	private List<AltBundle> formBundles(Map<String, AlterationPack> map, boolean mutex, double thr)
		throws CloneNotSupportedException
	{
		List<AltBundle> bundles = new ArrayList<AltBundle>();

		for (String s1 : map.keySet())
		{
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

			for (AltBundle bundle : bundles)
			{
				if (processed.contains(bundle)) continue;

				for (String sym : map.keySet())
				{
					AltBundle b = (AltBundle) bundle.clone();
					b.add(map.get(sym));
					
					if (b.absPVal() < thr) toAdd.add(b);
				}
				processed.add(bundle);
				if (!toAdd.isEmpty()) loop = true;
			}
			bundles.addAll(toAdd);
			System.out.println("added = " + toAdd.size());
		}

		Collections.sort(bundles);
		System.out.println("bundles.size() = " + bundles.size());
		return bundles;
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

	class AltBundle implements Comparable, Cloneable
	{
		String id;
		List<AlterationPack> alts;
		Alteration key;
		double pval;
		boolean mutex;

		public AltBundle(AlterationPack alt1, AlterationPack alt2, Alteration key, boolean mutex)
		{
			id = alt1.getId() + " - " + alt2.getId();
			this.mutex = mutex;

			this.alts = new ArrayList<AlterationPack>();
			this.alts.add(alt1);
			this.alts.add(alt2);
			this.key = key;

			pval = calcPVal();
		}

		public void add(AlterationPack pack)
		{
			alts.add(pack);
			Collections.sort(alts, new Comparator<AlterationPack>()
			{
				@Override
				public int compare(AlterationPack alt1, AlterationPack alt2)
				{
					return alt1.getId().compareTo(alt2.getId());
				}
			});

			if (alts.size() == 8 && calcPVal() < 0.05)
			{
				System.out.println(getPrint());
				System.out.println();
			}
			pval = calcPVal();

			// Update ID
			this.id = alts.get(0).getId();
			for (int i = 1; i < alts.size(); i++)
			{
				this.id += " - " + alts.get(i).getId();
			}
		}
		
		private double calcPVal()
		{
			if (alts.size() == 2)
			{
				double pval = Overlap.calcAlterationOverlapPval(
					alts.get(0).get(key), alts.get(1).get(key));

				if ((isMutex() && pval > 0) || (!isMutex() && pval < 0)) pval = 1;
				
				return pval;
			}
			
			double pval = 0;
			boolean[] use = new boolean[alts.get(0).getSize()];

			for (int i = 0; i < alts.size() - 1; i++)
			{
				if (mutex)
				{
					for (int j = i + 1; j < alts.size(); j++)
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
				}
				else
				{
					// Get intersection of others
					
					for (int k = 0; k < use.length; k++)
					{
						use[k] = true;

						for (int l = 0; l < alts.size(); l++)
						{
							if (l == i) continue;
							if (!alts.get(l).get(key)[k].isAltered())
							{
								use[k] = false;
								break;
							}
						}
					}

					// Calc pval for the pair

					double pv = Overlap.calcAlterationOverlapPval(
						alts.get(i).get(key), convert(use));

					if (pv < 0) return 1;

					if (pv > pval) pval = pv;
				}
			}
			return pval;
		}

		private Change[] convert(boolean[] b)
		{
			Change[] c = new Change[b.length];
			for (int i = 0; i < c.length; i++)
			{
				c[i] = b[i] ? Change.ACTIVATING : Change.NO_CHANGE;
			}
			return c;
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
			List<AlterationPack> sorted = sortToMostAltered(alts);
			List<Integer> order = getPrintOrdering(sorted);
			StringBuilder s = new StringBuilder();
			for (AlterationPack alt : sorted)
			{
				if (s.length() > 0) s.append("\n");
				s.append(alt.getPrint(key, order));
			}
			return s.toString();
		}

		protected List<AlterationPack> sortToMostAltered(List<AlterationPack> alts)
		{
			List<AlterationPack> sorted = new ArrayList<AlterationPack>(alts);
			Collections.sort(sorted, new Comparator<AlterationPack>()
			{
				@Override
				public int compare(AlterationPack alt1, AlterationPack alt2)
				{
					return new Integer(alt2.countAltered(key)).compareTo(alt1.countAltered(key));
				}
			});
			return sorted;
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
	}
}
