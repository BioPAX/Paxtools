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
	SimpleTraverse traverse;
	
	public boolean load(String filename)
	{
		traverse = new SimpleTraverse();
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

	public List<String> link(Set<String> from, Set<String> to, int limit)
	{
		List<String> rels = new ArrayList<String>();

		for (String s : from)
		{
			Set<String> eles = traverse.getPathElements(s, to, limit);
			eles.add(s);
			for (String ele : eles)
			{
				for (String type : sif.get(ele).keySet())
				{
					for (String tar : sif.get(ele).get(type))
					{
						if (eles.contains(tar) && !tar.equals(ele))
						{
							rels.add(ele + "\t" + type + "\t" + tar);
						}
					}
				}
			}
		}
		return rels;
	}

	public static void main(String[] args)
	{
		SIFLinker linker = new SIFLinker();
//		linker.load("/home/ozgun/Desktop/SIF.txt");
		linker.load("C:/Users/ozgun/Downloads/SIF.txt");
		HashSet<String> set = new HashSet<String>(Arrays.asList("CDKN2A", "RB1", "CDK2", "PPARG"));
		List<String> rels = linker.link(set, set, 2);
		for (String rel : rels)
		{
			System.out.println(rel.replaceAll("STATE_CHANGE","-->").replaceAll("BINDS_TO","---").replaceAll("TRANSCRIPTION","-t>").replaceAll("DEGRADATION","-d>"));
		}
	}
}
