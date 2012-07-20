package org.biopax.paxtools.causality.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class SimpleTraverse
{
	protected Map<String, Set<String>> dwMap;
	protected Map<String, Set<String>> upMap;
	protected Map<String, Set<String>> ppMap;

	public boolean load(String filename, Set<String> ppiTypes, Set<String> signalTypes)
	{
		dwMap = new HashMap<String, Set<String>>();
		upMap = new HashMap<String, Set<String>>();
		ppMap = new HashMap<String, Set<String>>();

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length != 3) continue;

				if (ppiTypes.contains(token[1]))
				{
					if (!ppMap.containsKey(token[0])) ppMap.put(token[0], new HashSet<String>());
					if (!ppMap.containsKey(token[2])) ppMap.put(token[2], new HashSet<String>());
					ppMap.get(token[0]).add(token[2]);
					ppMap.get(token[2]).add(token[0]);
				}
				else if (signalTypes.contains(token[1]))
				{
					if (!dwMap.containsKey(token[0])) dwMap.put(token[0], new HashSet<String>());
					if (!upMap.containsKey(token[2])) upMap.put(token[2], new HashSet<String>());
					dwMap.get(token[0]).add(token[2]);
					upMap.get(token[2]).add(token[0]);
				}
			}

			reader.close();
		}
		catch (IOException e) { e.printStackTrace(); return false; } return true;
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited, boolean downstream)
	{
		return goBFS(seed, visited, downstream ? dwMap : upMap);
	}

	public Set<String> goBFS(Set<String> seed, Set<String> visited)
	{
		return goBFS(seed, visited, ppMap);
	}

	private Set<String> goBFS(Set<String> seed, Set<String> visited, Map<String, Set<String>> map)
	{
		Set<String> neigh = new HashSet<String>();
		for (String s : seed)
		{
			if (map.containsKey(s))
			{
				for (String n : map.get(s))
				{
					if (!visited.contains(n))
					{
						neigh.add(n);
					}
				}
			}
		}
		return neigh;
	}

	public Set<String> getPathElements(String from, Set<String> to, int limit)
	{
		Set<String> result = new HashSet<String>();
		getPathElements(from, to, limit, 0, result);
		return result;
	}

	private void getPathElements(String from, Set<String> to, int limit, int i, Set<String> result)
	{
		Set<String> set = Collections.singleton(from);
		Set<String> neigh = goBFS(set, set, true);
		for (String n : neigh)
		{
			if (to.contains(n)) result.add(n);
			else if (i < limit)
			{
				int prevSize = result.size();
				getPathElements(n, to, limit, i+1, result);
				if (result.size() > prevSize) result.add(n);
			}
		}
	}
	
	public static void main(String[] args)
	{
		SimpleTraverse traverse = new SimpleTraverse();
		traverse.load("/home/ozgun/Desktop/SIF.txt", new HashSet<String>(Arrays.asList("BINDS_TO")),
			new HashSet<String>(Arrays.asList("STATE_CHANGE", "TRANSCRIPTION", "DEGRADATION")));
		System.out.println("traverse.upMap.size() = " + traverse.upMap.size());
		System.out.println("traverse.dwMap.size() = " + traverse.dwMap.size());
		System.out.println("traverse.ppMap.size() = " + traverse.ppMap.size());
	}
}
