package org.biopax.paxtools.causality.analysis;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Traverse
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
	
	public List<CommPoint> getCommonDownstream(Set<String> seed, int limit)
	{
		Map<String, Set<String>> reachMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> breadthMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> visitedMap = new HashMap<String, Set<String>>();

		Set<CommPoint> points = new HashSet<CommPoint>();
		
		for (String s : seed)
		{
			reachMap.put(s, new HashSet<String>(Arrays.asList(s)));
			breadthMap.put(s, new HashSet<String>(Arrays.asList(s)));
			visitedMap.put(s, new HashSet<String>(Arrays.asList(s)));
		}

		for (int i = 1; i < limit; i++)
		{
			for (String s : seed)
			{
				Set<String> neigh = goBFS(breadthMap.get(s), visitedMap.get(s), true);
				for (String n : neigh)
				{
					if (!reachMap.containsKey(n))
						reachMap.put(n, new HashSet<String>(Arrays.asList(s)));
					else reachMap.get(n).add(s);
				}
				breadthMap.put(s, neigh);
				visitedMap.get(s).addAll(neigh);
			}

			for (String r : reachMap.keySet())
			{
				if (reachMap.get(r).size() > 1)
				{
					CommPoint p = new CommPoint(r, reachMap.get(r), i);
					if (!containsBetter(points, p)) points.add(p);
				}
			}
		}

		List<CommPoint> list = new ArrayList<CommPoint>(points);
		Collections.sort(list);
		return list;
	}
	
	private boolean containsBetter(Set<CommPoint> set, CommPoint p)
	{
		if (set.contains(p)) return true;
		for (CommPoint cp : set)
		{
			if (cp.dist < p.dist && cp.upstr.containsAll(p.upstr)) return true;
		}
		return false;
	}
	
	public Set<String> getSymbols()
	{
		Set<String> merge = new HashSet<String>(upMap.keySet());
		merge.addAll(dwMap.keySet());
		return merge;
	}
	
	class CommPoint implements Comparable
	{
		String s;
		Set<String> upstr;
		int dist;

		CommPoint(String s, Set<String> upstr, int dist)
		{
			this.s = s;
			this.upstr = upstr;
			this.dist = dist;
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof CommPoint)
			{
				CommPoint p = (CommPoint) o;
				if (p.s.equals(s) && p.upstr.containsAll(upstr) && upstr.containsAll(p.upstr) && 
					p.dist == dist) return true;
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return s.hashCode();
		}

		@Override
		public int compareTo(Object o)
		{
			if (o instanceof CommPoint)
			{
				CommPoint p = (CommPoint) o;
				new Integer(p.upstr.size()).compareTo(upstr.size());
			}
			return 0;
		}
	}
	
	public static void main(String[] args)
	{
		Traverse traverse = new Traverse();
		traverse.load("/home/ozgun/Desktop/SIF.txt", new HashSet<String>(Arrays.asList("BINDS_TO")),
			new HashSet<String>(Arrays.asList("STATE_CHANGE", "TRANSCRIPTION", "DEGRADATION")));
		System.out.println("traverse.upMap.size() = " + traverse.upMap.size());
		System.out.println("traverse.dwMap.size() = " + traverse.dwMap.size());
		Set<String> merge = new HashSet<String>(traverse.upMap.keySet());
		merge.addAll(traverse.dwMap.keySet());
		System.out.println("merge.size() = " + merge.size());
		System.out.println("traverse.ppMap.size() = " + traverse.ppMap.size());

		for (String n : traverse.dwMap.get("EP300"))
		{
			System.out.println(n);
		}
	}
}
