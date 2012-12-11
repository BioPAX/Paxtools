package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.TermCounter;
import org.biopax.paxtools.conversion.HGNC;
import org.biopax.paxtools.impl.level3.ProvenanceImpl;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
@Ignore
public class FeatureCollectorTest
{
	@Test
	@Ignore
	public void testCollection() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/all.owl"));

		FeatureCollector fc = new FeatureCollector();
		Map<EntityReference,Set<ModificationFeature>> all = fc.collectFeatures(model, false);
		clearEmpty(all);
		Map<EntityReference, Set<ModificationFeature>> act = fc.collectFeatures(model, true);
		clearEmpty(act);

		Map<EntityReference, Set<String>> typesAll = convertToTypes(all);
		Map<EntityReference, Set<String>> typesAct = convertToTypes(act);

		for (EntityReference er : typesAct.keySet())
		{
			Set<String> actSet = typesAct.get(er);
			Set<String> allSet = typesAll.get(er);
			assert allSet.size() >= actSet.size();
			if (allSet.size() == actSet.size()) continue;
			System.out.println("-------\n" + er.getDisplayName());
			for (String s : actSet)
			{
				System.out.print(s + "\t");
			}
			System.out.println();
			for (String s : allSet)
			{
				if (!actSet.contains(s)) System.out.print(s + "\t");
			}
			System.out.println();
		}
		
		System.out.println();
	}
	
	private void clearEmpty(Map<EntityReference,Set<ModificationFeature>> map)
	{
		for (EntityReference er : new HashSet<EntityReference> (map.keySet()))
		{
			if (map.get(er).isEmpty()) map.remove(er);
		}
	}
	
	private Map<EntityReference, Set<String>> convertToTypes(
		Map<EntityReference, Set<ModificationFeature>> map) 
	{
		Map<EntityReference, Set<String>> types = new HashMap<EntityReference, Set<String>>();

		for (EntityReference er : map.keySet())
		{
			types.put(er, new HashSet<String>());
			for (ModificationFeature mf : map.get(er))
			{
				if (mf.getModificationType() != null)
					types.get(er).add(mf.getModificationType().toString());
			}
		}
		return types;
	}

	@Test
	@Ignore
	public void matchFeatures() throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));

		Set<String> overlaps = new HashSet<String>();

		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			List<Feature> feats = extractFeatures(pr);
			collectFeatureOverlaps(feats, overlaps);
		}
		for (String overlap : overlaps)
		{
			System.out.println(overlap);
		}
	}

	private void collectFeatureOverlaps(List<Feature> feats, Set<String> overlaps)
	{
		for (Feature f1 : feats)
		{
			for (Feature f2 : feats)
			{
				if (f1 == f2) continue;

				if (f1.hasLocation() && f1.getLocation() == f2.getLocation() &&
					!f1.getTerm().equals(f2.getTerm()) && !f1.getSource().equals(f2.getSource()))
				{
					String s = f1.getTerm() + "\t" + f2.getTerm();
					String r = f2.getTerm() + "\t" + f1.getTerm();

					if (!overlaps.contains(s) && !overlaps.contains(r))
					{
						overlaps.add(s);
					}
				}
			}
		}
	}

	@Test
	@Ignore
	public void writeFeatures() throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));

		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ozgun/Desktop/features.txt"));

		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			String s = getFeaturesString(pr);
			if (s != null) writer.write("\n\n" + s);
		}

		writer.close();
	}

	@Test
	@Ignore
	public void reportAccuracy() throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));
		
		Map<String, Integer> noresidue = new HashMap<String, Integer>();
		Map<String, Integer> match = new HashMap<String, Integer>();
		Map<String, Integer> nomatch = new HashMap<String, Integer>();
		Map<String, Integer> noseq = new HashMap<String, Integer>();
		Map<String, Integer> prots = new HashMap<String, Integer>();
		Map<String, Integer> resProts = new HashMap<String, Integer>();
		Map<String, Integer> overlaps = new HashMap<String, Integer>();

		Map<String, String> resMatch = getResidueMatching(model);
		Map<String, Set<String>> matching = loadFeatureTermMatching();


		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			if (!isHuman(pr) || getUniProtID(pr) == null) continue;

			Set<String> provs = new HashSet<String>();
			Set<String> res_provs = new HashSet<String>();

			List<Feature> feats = extractFeatures(pr);
//			List<Feature> nomatch_feats = new ArrayList<Feature>();

			for (Feature f : feats)
			{
				if (f.hasLocation())
				{
					String res = f.getResidue();
					String term = f.getTerm();
					
					if (res != null && res.equals(resMatch.get(term))) count(f.getSource(), match);
					else if (f.seq != null)
					{
						count(f.getSource(), nomatch);
//						nomatch_feats.add(f);
					}
					else count(f.getSource(), noseq);
				}
				else count(f.getSource(), noresidue);
				
				if (f.hasLocation()) res_provs.add(f.getSource());
				provs.add(f.getSource());
			}
			
			countOverlaps(feats, overlaps, matching);

			count(provs, prots);
			count(res_provs, resProts);
		}
		printCounts("Number of proteins", prots);
		printCounts("Number of residue specified proteins", resProts);
		printCounts("No residue", noresidue);
		printCounts("Match", match);
		printCounts("No match", nomatch);
		printCounts("No sequence", noseq);
		printCounts("Overlaps", overlaps);
	}

	private Map<String, Set<String>> loadFeatureTermMatching() throws IOException
	{
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/feature-match.txt"));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");
			if (token.length == 2)
			{
				if (!map.containsKey(token[0])) map.put(token[0], new HashSet<String>());
				if (!map.containsKey(token[1])) map.put(token[1], new HashSet<String>());
				map.get(token[0]).add(token[1]);
				map.get(token[1]).add(token[0]);
			}
		}

		reader.close();

		return map;
	}
	
	private void countOverlaps(List<Feature> features, Map<String, Integer> cnt,
		Map<String, Set<String>> match)
	{
		List<Feature> subset = new ArrayList<Feature>();
		int cursor = 1;

		for (Feature f : features)
		{
			int loc = f.getLocation();
			if (loc > 0)
			{
				if (loc == cursor) subset.add(f);
				else
				{
					// Process the current subset
					if (!subset.isEmpty()) processSubset(cnt, match, subset);

					// Initiate a new subset

					cursor = loc;
					subset.clear();
					subset.add(f);
				}
			}
		}
		if (!subset.isEmpty()) processSubset(cnt, match, subset);
	}

	private void processSubset(Map<String, Integer> cnt, Map<String, Set<String>> match,
		List<Feature> subset)
	{
		List<Set<Feature>> groups = groupFeatures(subset, match);
		Set<String> provsInGroups = getProvsInGroups(groups);

		for (String s : provsInGroups)
		{
			if (!cnt.containsKey(s)) cnt.put(s, 0);
			cnt.put(s, cnt.get(s) + 1);
		}
	}

	private List<Set<Feature>> groupFeatures(List<Feature> features, Map<String, Set<String>> match)
	{
		List<Set<Feature>> groups = new ArrayList<Set<Feature>>();

		for (Feature f : features)
		{
			boolean added = false;
			for (Set<Feature> g : groups)
			{
				String term = g.iterator().next().getTerm();
				if (term.equals(f.getTerm()) ||
					(match.get(term) != null && match.get(term).contains(f.getTerm())))
				{
					g.add(f);
					added = true;
					break;
				}
			}
			if (!added)
			{
				HashSet<Feature> newG = new HashSet<Feature>();
				newG.add(f);
				groups.add(newG);
			}
		}
		return groups;
	}

	private Set<String> getProvsInGroups(List<Set<Feature>> groups)
	{
		Set<String> set = new HashSet<String>();
		for (Set<Feature> group : groups)
		{
			Set<String> provs = new HashSet<String>();
			for (Feature f : group)
			{
				provs.add(f.getSource());
			}
			List<String> list = new ArrayList<String>(provs);
			Collections.sort(list);
			String s = "";
			for (String prov : list)
			{
				s += prov + " - ";
			}
			set.add(s);
		}
		return set;
	}
	
	private void count(String prov, Map<String, Integer> map)
	{
		if (!map.containsKey(prov)) map.put(prov, 0);
		map.put(prov, map.get(prov) + 1);
	}
	private void count(Set<String> prov, Map<String, Integer> map)
	{
		for (String p : prov)
		{
			count(p, map);
		}
	}

	private void printCounts(String name, Map<String, Integer> map)
	{
		System.out.println("\nCounts for " + name);
		for (String prov : map.keySet())
		{
			System.out.println(prov + "\t" + map.get(prov));
		}
	}
	
	public Map<String, String> getResidueMatching(Model model) throws IOException
	{
		Map<String, TermCounter> cnt = new HashMap<String, TermCounter>();
		
		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			if (!isHuman(pr)) continue;

			List<Feature> feats = extractFeatures(pr);

			for (Feature f : feats)
			{
				if (f.hasLocation())
				{
					String res = f.getResidue();
					String term = f.getTerm();
					if (!cnt.containsKey(term)) cnt.put(term, new TermCounter());
					cnt.get(term).addTerm(res);
				}
			}
		}

//		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ozgun/Desktop/residues.txt"));
//		for (String term : cnt.keySet())
//		{
//			writer.write(term + "\n");
//			writer.write(cnt.get(term).toString() + "\n\n");
//		}
//		writer.close();

		Map<String, String> map = new HashMap<String, String>();

		for (String term : cnt.keySet())
		{
			map.put(term, cnt.get(term).getMostFrequentTerm());
		}
		return map;
	}

//	private Map<String, Integer> getFeatureCounts(List<Feature> features)
//	{
//		Map<Integer, Set<Feature>>
//	}

	
	
	private String getFeaturesString(ProteinReference pr)
	{
		String sym = getSymbol(pr);
		if (sym == null) return null;

		String s = sym;

		String uni = getUniProtID(pr);
		s += "\t" + uni;
		
		
		List<Feature> features = extractFeatures(pr);

		for (Feature f : features)
		{
			s += "\n" + f;
		}
		return s;
	}

	private String getSymbol(ProteinReference pr)
	{
		for (Xref xref : pr.getXref())
		{
			if (xref.getDb().equals("HGNC"))
			{
				String id = xref.getId().substring(5);
				return HGNC.getSymbol(id);
			}
		}
		return null;
	}
	
	private String getUniProtID(ProteinReference pr)
	{
		if (pr.getRDFId().contains("uniprot"))
		{
			return pr.getRDFId().substring(pr.getRDFId().lastIndexOf("/") + 1);
		}
		return null;
	}

	private boolean isHuman(ProteinReference pr)
	{
		return pr.getOrganism() != null && pr.getOrganism().getDisplayName().equals("Homo sapiens");
	}

	private static Provenance UNIPROT = new ProvenanceImpl();
	static
	{
		UNIPROT.setDisplayName("UniProt");
	}
	
	
	private List<Feature> extractFeatures(ProteinReference pr)
	{
		Set<Feature> features = new HashSet<Feature>();

//		for (EntityReference parent : pr.getMemberEntityReferenceOf())
//		{
//			if (parent instanceof ProteinReference)
//			{
//				features.addAll(extractFeatures((ProteinReference) parent));
//			}
//			else System.err.println("Type mismatch in generic. Pr:" + pr.getRDFId() + "\t" + "parent:" + parent.getRDFId());
//		}
//
//		if (pr.getMemberEntityReferenceOf().size() > 1)
//		{
////			System.err.println(pr.getMemberEntityReferenceOf().size() + " parents: " + pr.getDisplayName() + "\t" + pr.getRDFId());
//			Set<Feature> set = new HashSet<Feature>(features);
//			features.clear();
//			features.addAll(set);
//		}

		for (EntityFeature ef : pr.getEntityFeature())
		{
			if (ef instanceof ModificationFeature)
			{
				ModificationFeature mf = (ModificationFeature) ef;
				Feature f = new Feature(mf, UNIPROT, pr.getSequence());
				features.add(f);
			}
		}

		for (SimplePhysicalEntity spe : pr.getEntityReferenceOf())
		{
			if (!(spe instanceof Protein)) continue;
			
			Protein prot = (Protein) spe;
			Provenance prov = selectProvenance(prot);
			
			for (EntityFeature feat : prot.getFeature())
			{
				if (feat instanceof ModificationFeature)
				{
					ModificationFeature mf = (ModificationFeature) feat;
					
					Feature f = new Feature(mf, prov, pr.getSequence());
					if (!f.getTerm().endsWith("active")) features.add(f);
				}
			}
		}
		List<Feature> list = new ArrayList<Feature>(features);
		Collections.sort(list);
		return list;
	}

	private List<String> provPriority = Arrays.asList("PhosphoSitePlus", "pid", "Reactome", "panther", "HumanCyc");

	private Provenance selectProvenance(PhysicalEntity pe)
	{
		Provenance select = null;
		for (Provenance prov : pe.getDataSource())
		{
			if (prov.getDisplayName() == null) prov.setDisplayName("NCI - null display name");
			
			if (provPriority.contains(prov.getDisplayName()))
			{
				select = prov;
				break;
			}
		}
		
		return select != null ? select : pe.getDataSource().iterator().next();
	}
	

	// Displaying modifications

	private class Feature implements Comparable
	{
		ModificationFeature mf;
		Provenance prov;
		String seq;

		private Feature(ModificationFeature mf, Provenance prov, String seq)
		{
			this.mf = mf;
			this.prov = prov;
			this.seq = seq;
		}

		@Override
		public int compareTo(Object o)
		{
			Feature feat = (Feature) o;
			
			Integer loc1 = getLocation();
			Integer loc2 = feat.getLocation();

			if (loc1.equals(loc2))
				return prov.getDisplayName().compareTo(feat.prov.getDisplayName());

			return loc1.compareTo(loc2);
		}

		public int getLocation()
		{
			return mf.getFeatureLocation() == null ? -1 :
				((SequenceSite) mf.getFeatureLocation()).getSequencePosition();
		}

		public String getTerm()
		{
			String term = mf.getModificationType() != null &&
				!mf.getModificationType().getTerm().isEmpty() ?
					mf.getModificationType().getTerm().iterator().next() : "null-term";

			if (term.startsWith("MOD_RES ")) term = term.substring(8);
			return term;
		}

		public boolean hasLocation()
		{
			return getLocation() > 0;
		}

		public String getSource()
		{
			return prov.getDisplayName();
		}

		public String getResidue()
		{
			int loc = getLocation();
			if (hasLocation() && seq != null && seq.length() >= loc)
			{
				return seq.substring(loc-1, loc);
			}
			else
			{
				return null;
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof Feature)
			{
				Feature feat = (Feature) o;
				return getLocation() == feat.getLocation() && getTerm().equals(feat.getTerm()) &&
					prov.getDisplayName().equals(feat.prov.getDisplayName());
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return getLocation() + getTerm().hashCode() + prov.getDisplayName().hashCode();
		}

		@Override
		public String toString()
		{
			int loc = getLocation();
			String aa = (seq != null && loc > 0 && loc <= seq.length()) ? 
				seq.substring(loc-1, loc) : " ";

			return (loc <= 0 ? "  " : loc) + "\t" + aa + "\t" + getTerm() + "\t" +
				prov.getDisplayName();
		}
	}


}
