package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.HGNC;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class DBParsers
{
	@Test
	@Disabled
	public void extractSignalink() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/signal-db/signalink-raw.txt"));

		Set<String> set = new HashSet<>();
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split(";");

			if (token.length < 14)
			{
				System.out.println("token.length = " + token.length);
				System.out.println(line);
				continue;
			}

			String type = token[13];
			String source = token[0];
			String target = token[6];
			String direct = token[14];
			String pubs = (token.length > 16) ? token[16] : null;
			String algo = (token.length > 17) ? token[17] : null;

			if (type.contains("undirected")) continue;
			if (!type.contains("directed")) continue;

			Set<String> refs = new HashSet<>();
			if (pubs != null)
			{
				for (String s : pubs.split("\\|"))
				{
					refs.add(s);
//					if (!refCnt.containsKey(s)) refCnt.put(s, 0);
//					refCnt.put(s, refCnt.get(s) + 1);
				}
			}

			if (refs.isEmpty() ||
				(refs.size() == 1 && PRED_REF.contains(refs.iterator().next())))
				continue;

			if (!source.isEmpty()) source = HGNC.getSymbolByHgncIdOrSym(source);
			if (!target.isEmpty()) target = HGNC.getSymbolByHgncIdOrSym(target);

			if (source != null && !source.isEmpty() && target != null && !target.isEmpty())
			{
				String s = source + "\t" + target;
				if (!set.contains(s))
				{
					set.add(s);
				}
			}
		}

		reader.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter(
			"/home/ozgun/Desktop/signal-db/signalink-extracted-from-raw-nopred.txt"));

		for (String s : set)
		{
			writer.write(s + "\n");
		}

		writer.close();

		System.out.println("set.size() = " + set.size());
	}

	private static final Set<String> PRED_REF = new HashSet<>(Arrays.asList(("22110040\t" +
		"14681366\t" +
		"15652477\t" +
		"22900683\t" +
		"22955619\t" +
		"18988627\t" +
		"23331499\t" +
		"18971253\t" +
		"20005715\t" +
		"21071413\t" +
		"16413481\t" +
		"18006570\t" +
		"18996891").split("\t")));

	private void printOrdered(final Map<String, Integer> map, int min)
	{
		List<String> list = new ArrayList<>(map.keySet());
		Collections.sort(list, (o1, o2) -> new Integer(map.get(o2)).compareTo(map.get(o1)));

		for (String s : list)
		{
			Integer cnt = map.get(s);
			if (cnt < min) break;
			System.out.println(s + "\t" + cnt);
		}
	}

	@Test
	@Disabled
	public void extractSignalink2() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/signal-db/signalink-sql-produced.txt"));

		Set<String> set = new HashSet<>();
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");

			if (token.length < 2) continue;

			String source = token[0];
			String target = token[1];

			if (!source.isEmpty()) source = HGNC.getSymbolByHgncIdOrSym(source);
			if (!target.isEmpty()) target = HGNC.getSymbolByHgncIdOrSym(target);

			if (source != null && !source.isEmpty() && target != null && !target.isEmpty())
			{
				String s = source + "\t" + target;
				if (!set.contains(s))
				{
					set.add(s);
				}
			}
		}

		reader.close();

		BufferedWriter writer = new BufferedWriter(new FileWriter(
			"/home/ozgun/Desktop/signal-db/signalink2.txt"));

		for (String s : set)
		{
			writer.write(s + "\n");
		}

		writer.close();

		System.out.println("set.size() = " + set.size());
	}

	@Test
	@Disabled
	public void printVenn() throws IOException
	{
		Set<String> s1 = read("/home/ozgun/Projects/biopax-pattern/directed-relations.txt", true);
//		Set<String> s2 = readSpikeFromOwl();
		Set<String> s2 = readSpike();
//		Set<String> s3 = read("/home/ozgun/Desktop/signal-db/signalink2.txt", false);
		Set<String> s3 = read("/home/ozgun/Desktop/signal-db/signalink-extracted-from-raw.txt", false);

		System.out.println("pattern = " + s1.size());
		System.out.println("spike = " + s2.size());
		System.out.println("signalink = " + s3.size());

		System.out.println("\npattern and spike = " + intersect(s1, s2));
		System.out.println("spike and signalink = " + intersect(s2, s3));
		System.out.println("pattern and signalink = " + intersect(s1, s3));

		System.out.println("\nall 3 = " + intersect(s1, s2, s3));

		System.out.println("\npattern and spike negative = " + intersect(s1, negate(s2)));
		System.out.println("spike and signalink negative = " + intersect(s2, negate(s3)));
		System.out.println("pattern and signalink negative = " + intersect(s1, negate(s3)));

		System.out.println("\npattern conf spike = " + conflict(s1, s2));
		System.out.println("spike conf signalink = " + conflict(s2, s3));
		System.out.println("pattern conf signalink = " + conflict(s1, s3));

//		Set inter = new HashSet(s1);
//		inter.retainAll(s2);
//		inter.retainAll(s3);
//
//		System.out.println("inter.size() = " + inter.size());
//
//		for (Object o : inter)
//		{
//			System.out.println(o.toString());
//		}

//		for (String s : s3)
//		{
//			if (s.startsWith("ZDHHC21"))
////			if (s.startsWith("IL12A"))
////			if (s.startsWith("NTRK1"))
//			{
//				System.out.println(s);
//			}
//		}
	}

	private Set<String> negate(Set<String> normal)
	{
		Set<String> set = new HashSet<>();
		for (String s : normal)
		{
			String[] tok = s.split("\t");
			set.add(tok[1] + "\t" + tok[0]);
		}
		return set;
	}

	private int intersect(Set<String>... set)
	{
		Set<String> s = new HashSet<>(set[0]);
		for (Set<String> ss : set)
		{
			s.retainAll(ss);
		}
		return s.size();
	}

	private int conflict(Set<String> set1, Set<String> set2)
	{
		int cnt  = 0;
		for (String s : set1)
		{
			String r = s.substring(s.indexOf("\t") + 1) + "\t" + s.substring(0, s.indexOf("\t"));
			if (!set1.contains(r) && set2.contains(r) && !set2.contains(s)) cnt ++;
		}
		return cnt;
	}

	private int reflectiveCnt(Set<String> set)
	{
		int cnt = 0;
		for (String s : set)
		{
			String[] tok = s.split("\t");
			String a = tok[1] + "\t" + tok[0];
			if (set.contains(a)) cnt ++;
		}
		return cnt / 2;
	}

	@Test
	@Disabled
	public void printNeighborsInSpike() throws IOException
	{
		String s = "DUSP1";
		Set<String> rels = readSpike();
		for (String rel : rels)
		{
			if (rel.startsWith(s + "\t") || rel.endsWith("\t" + s)) System.out.println(rel);
		}
	}

	private Set<String> read(String file, boolean skipFirstLine) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));

		Set<String> set = new HashSet<>();

		if (skipFirstLine) reader.readLine();

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");

			if (token.length < 2) continue;

			set.add(line);
		}
		reader.close();
		return set;
	}

	private Set<String> readSpike() throws IOException
	{
		Set<String> set = new HashSet<>();
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/signal-db/SPIKE-ParsedFromXML.txt"));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");

			set.add(token[0] + "\t" + token[1]);
		}

		reader.close();
		return set;
	}

	public Set<String> readSpikeFromOwl() throws IOException
	{
		SimpleIOHandler handler = new SimpleIOHandler();
		Model model = handler.convertFromOWL(new FileInputStream(
			"/home/ozgun/Desktop/signal-db/Spike2_LatestSpikeDB.owl"));

		Set<String> set = new HashSet<>();

		for (Control ctrl : model.getObjects(Control.class))
		{
			System.out.println(ctrl.getUri());
			String id = ctrl.getUri().substring(ctrl.getUri().indexOf("#") + 1);
			Set<String>[] sets = parseSpikeControl(id);

			if (sets[0].isEmpty() || sets[1].isEmpty())
			{
				System.err.println("no symbol = " + id);
				continue;
			}

			for (String up : sets[0])
			{
				for (String down : sets[1])
				{
					set.add(up + "\t" + down);
				}
			}
		}

		return set;
	}

	private Set<String>[] parseSpikeControl(String id)
	{
		int ind = id.indexOf("PROMOTES");
		if (ind < 0) ind = id.indexOf("INHIBITS");
		if (ind < 0) ind = id.indexOf("UNKNOWN");

		if (ind < 0)
		{
			System.err.println("no kywd = " + id);
			return null;
		}

		String up = id.substring(0, ind);
		String down = id.substring(id.indexOf(".", ind));

		return new Set[]{collectSymbols(up), collectSymbols(down)};
	}

	private Set<String> collectSymbols(String s)
	{
		Set<String> set = new HashSet<>();

		s = s.replaceAll("\\.", " ");
		s = s.replaceAll(",", " ");

		for (String x : s.split(" "))
		{
			x = HGNC.getSymbolByHgncIdOrSym(x);
			if (x != null) set.add(x);
		}
		return set;
	}

	private Map<String, String> readSpikeID2Symbol() throws IOException
	{
		Map<String, String> id2sym = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/signal-db/LatestSpikeDB.xml"));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (line.trim().startsWith("<Gene ") || line.trim().startsWith("<Group "))
			{
				int i = line.indexOf(" id=\"");
				String id = line.substring(i + 5, line.indexOf("\"", i+ 5));

				i = line.indexOf(" name=\"");
				String name = line.substring(i + 7, line.indexOf("\"", i+ 7));

				id2sym.put(id, name);
			}
		}

		reader.close();
		return id2sym;
	}

	public Map<String, String> getEG2HGNC() throws IOException
	{
		Map<String, String> map = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/hgnc2eg.txt"));

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");

			if (token.length < 2) continue;

			map.put(token[1], token[0]);
		}
		reader.close();
		return map;
	}

	@Test
	@Disabled
	public void printLines() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/signal-db/LatestSpikeDB.xml"));

		int from = 233571;
		int to = from + 50;

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (i++ < from) continue;
			System.out.println(line);

			if (i == to) break;
		}
		reader.close();
	}

	@Test
	@Disabled
	public void printLinesContaining() throws IOException
	{
		String query = "1509498";

		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/signal-db/LatestSpikeDB.xml"));

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (line.contains(query)) System.out.println(i + line);

			i++;
		}
		reader.close();
	}

	@Test
	@Disabled
	public void searchFirstOcc() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(
			"/home/ozgun/Desktop/signal-db/LatestSpikeDB.xml"));

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (line.contains("<Group"))
			{
				break;
			}
			i++;
		}
		reader.close();
		System.out.println("i = " + i);
	}

	@Test
	@Disabled
	public void readXML() throws ParserConfigurationException, IOException, SAXException
	{
		File f = new File("/home/ozgun/Desktop/signal-db/LatestSpikeDB.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(f);
		doc.normalizeDocument();

		NodeList nlist = doc.getElementsByTagName("RegulationBlock");

		System.out.println(nlist);
	}
}
