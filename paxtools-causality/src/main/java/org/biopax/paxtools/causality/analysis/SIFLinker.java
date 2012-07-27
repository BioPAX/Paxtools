package org.biopax.paxtools.causality.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class SIFLinker
{
	protected Map<String, Map<String, Set<String>>> sif;
	public Traverse traverse;
	
	public boolean load(String filename)
	{
		traverse = new Traverse();
		traverse.load(filename, new HashSet<String>(Arrays.asList("BINDS_TO")),
			new HashSet<String>(Arrays.asList("STATE_CHANGE", "TRANSCRIPTION", "DEGRADATION")));
		try
		{
			sif = new HashMap<String, Map<String, Set<String>>>();
	
			BufferedReader reader = new BufferedReader(new FileReader(filename));
	
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				
				if (!sif.containsKey(token[0])) 
					sif.put(token[0], new HashMap<String, Set<String>>());
				
				if (!sif.get(token[0]).containsKey(token[1]))
					sif.get(token[0]).put(token[1], new HashSet<String>());
				
				sif.get(token[0]).get(token[1]).add(token[2]);
			}
	
			reader.close();
		}
		catch (IOException e) { e.printStackTrace(); return false; } return true;
	}

	public List<String> linkMinimal(Set<String> set, int limit)
	{
		List<String> rels = new ArrayList<String>();
		Set<String> linked = new HashSet<String>();

		for (int i = 0; i <= limit && linked.size() < set.size(); i++)
		{
			for (String s1 : set)
			{
				for (String s2 : set)
				{
					if (s1.equals(s2)) continue;
					if (linked.contains(s1) && linked.contains(s2)) continue;

					List<String> rel = link(Collections.singleton(s1), Collections.singleton(s2), i);
					
					if (!rel.isEmpty())
					{
						rels.addAll(rel);
						linked.add(s1);
						linked.add(s2);
					}
				}
			}
		}
		return rels;
	}
	
	public List<String> linkProgressive(Set<String> from, Set<String> to, int limit)
	{
		List<String> rels = new ArrayList<String>();

		for (String s1 : from)
		{
			for (String s2 : to)
			{
				for (int i = 0; i <= limit; i++)
				{
					if (s1.equals(s2)) continue;

					List<String> rel = link(Collections.singleton(s1), Collections.singleton(s2), i);

					if (!rel.isEmpty())
					{
						rels.addAll(rel);
						break;
					}
				}
			}
		}
		return rels;
	}

	public List<String> link(Set<String> from, Set<String> to, int limit)
	{
		List<String> rels = new ArrayList<String>();

		for (String s : from)
		{
			Set<String> eles = traverse.getPathElements(s, to, limit);
			eles.add(s);
			for (String ele : eles)
			{
				if (sif.containsKey(ele))
				for (String type : sif.get(ele).keySet())
				{
					for (String tar : sif.get(ele).get(type))
					{
						if (eles.contains(tar) && !tar.equals(ele))
						{
							String rel = ele + "\t" + type + "\t" + tar;
							if (!rels.contains(rel)) rels.add(rel);
						}
					}
				}
			}
		}
		return rels;
	}

	public List<String> linkCommonDownstream(Set<String> seed, int limit)
	{
		List<String> rels = new ArrayList<String>();

		List<Traverse.CommPoint> points = traverse.getCommonDownstream(seed, limit);
		
		Set<Traverse.CommPoint> select = new HashSet<Traverse.CommPoint>();
		Set<String> covered = new HashSet<String>();

//		while(covered.size() < seed.size())
		{
			for (Traverse.CommPoint p : points)
			{
//				if (!covered.containsAll(p.upstr))
				{
					select.add(p);
					covered.addAll(p.upstr);
				}
			}
		}


		for (Traverse.CommPoint p : select)
		{
			List<String> link = linkProgressive(p.upstr, Collections.singleton(p.s), p.dist);
			for (String l : link)
			{
				if (!rels.contains(l)) rels.add(l);
			}
		}

		return rels;
	}
	
	public static void main(String[] args)
	{
		SIFLinker linker = new SIFLinker();
		linker.load("/home/ozgun/Desktop/SIF.txt");
//		linker.load("C:/Users/ozgun/Downloads/SIF.txt");
		HashSet<String> set = new HashSet<String>(Arrays.asList("IFNA2  TP53".split("  ")));
		List<String> rels = linker.link(Collections.singleton("IFNA2"), Collections.singleton("TP53"), 3);
		for (String rel : rels)
		{
//			System.out.println(rel.replaceAll("STATE_CHANGE","-->").replaceAll("BINDS_TO","---").replaceAll("TRANSCRIPTION","-t>").replaceAll("DEGRADATION","-d>"));
			System.out.println(rel);
		}
	}
}
