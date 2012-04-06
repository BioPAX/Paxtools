package org.biopax.paxtools.causality.data;

import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Change;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.causality.util.EGUtil;
import org.biopax.paxtools.causality.util.Summary;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * This class downloads data from GEO database, parses it, and prepares for an analysis.
 *
 * @author Ozgun Babur
 */
public class GEOAccessor extends AlterationProviderAdaptor
{
	protected String gseID;
	protected String platformID;
	
	protected int[] testIndex;
	protected int[] controlIndex;

	protected Map<String, double[]> dataMap;

	protected Map<String, AlterationPack> memo;
	
	protected static String dataDirectory = "target/geo_data";
	
	protected final static String SERIES_URL_PREFIX =
		"ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SeriesMatrix/";

	protected final static String SERIES_URL_SUFFIX = "_series_matrix.txt.gz";

	protected final static String PLATFORM_URL_PREFIX =
		"http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?targ=self&form=text&view=data&acc=";

	protected final static String PLATFORM_LINE_INDICATOR = "!Series_platform_id";

	protected final static String[] EG_NAMES = new String[]{"ENTREZ_GENE_ID", "GENE"};
	protected final static String[] SYMBOL_NAMES = new String[]{"Gene Symbol", "GENE_SYMBOL"};
	
	static double devThr = 1;
	static double changeThr = 2;
	static double changeThrInverse = 1 / changeThr;

	public GEOAccessor(String gseID, int[] testIndex, int[] controlIndex)
	{
		this.gseID = gseID;
		this.testIndex = testIndex;
		this.controlIndex = controlIndex;

		try
		{
			parse();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void parse() throws IOException
	{
		System.out.println("Getting data file");
		File seriesFile = getSeriesFile();
		System.out.println("Getting platform file");
		File platformFile = getPlatformFile();
		System.out.println("Reading data");
		Map<String, List<String>> refMap = parsePlatform(platformFile);
		dataMap = parseDataFile(seriesFile, refMap);
		memo = new HashMap<String, AlterationPack>();
	}

	protected File getSeriesFile()
	{
		File file = new File(dataDirectory + File.separator + gseID);
		
		if (!file.exists())
		{
			try
			{
				downloadSeries(gseID);
			} 
			catch (IOException e)
			{
				System.err.println(e);
				return null;
			}
		}
		
		return file;
	}

	protected File getPlatformFile()
	{
		if (platformID == null)
		{
			try
			{
				platformID = extractPlatformID(gseID);
			} 
			catch (IOException e)
			{
				System.err.println(e);
				return null;				
			}
		}
		
		File file = new File(dataDirectory + File.separator + platformID);
		
		if (!file.exists())
		{
			try
			{
				downloadPlatformFile(platformID);
			} 
			catch (IOException e)
			{
				System.err.println(e);
				return null;
			}
		}
		
		return file;
	}

	protected void downloadSeries(String gseID) throws IOException
	{
		String URLname = SERIES_URL_PREFIX + gseID + "/"+ gseID + SERIES_URL_SUFFIX;

		URL url = new URL(URLname);
		URLConnection con = url.openConnection();
		GZIPInputStream in = new GZIPInputStream(con.getInputStream());

		// Open the output file
		String target = dataDirectory + File.separator + gseID;
		OutputStream out = new FileOutputStream(target);
		// Transfer bytes from the compressed file to the output file
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}

		// Close the file and stream
		in.close();
		out.close();

	}

	protected void downloadPlatformFile(String platformID) throws IOException
	{
		String URLname = PLATFORM_URL_PREFIX + platformID;

		URL url = new URL(URLname);
		URLConnection con = url.openConnection();

		BufferedReader reader;
		BufferedWriter writer = new BufferedWriter(
			new FileWriter(dataDirectory + File.separator + platformID));

		reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

		String currentRead;

		while((currentRead = reader.readLine()) != null)
		{
			writer.write(currentRead + "\n");
		}

		reader.close();
		writer.close();
	}
	
	protected String extractPlatformID(String gseID) throws IOException
	{
		File gseFile = new File(dataDirectory + File.separator + gseID);
		
		if (!gseFile.exists())
		{
			downloadSeries(gseID);
		}

		BufferedReader br = new BufferedReader(new FileReader(gseFile));

		String currentLine;
		String platformName = null;

		// Find accession number of platform

		while((currentLine = br.readLine()) != null)
		{
			if(currentLine.contains(PLATFORM_LINE_INDICATOR))
			{
				platformName = currentLine.substring(currentLine.indexOf("G"),
					currentLine.lastIndexOf("\""));
				break;
			}
		}

		return platformName;
	}

	protected boolean ignoreLine(String line)
	{
		return line.startsWith("^") || line.startsWith("!") || line.startsWith("#") || line.trim().length() == 0;
	}
	
	protected int indexOf(String[] array, String s)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i].equals(s)) return i;
		}
		return -1;
	}
	
	protected int getColIndex(String[] cols, String[] possibleNames)
	{
		for (String name : possibleNames)
		{
			int ind = indexOf(cols, name);
			if (ind > 0) return ind;
		}
		return -1;
	}

	protected String[] parseToken(String token)
	{
		return token.split("[/ ]+");
	}

	protected Set<String> getEGIDs(String egs, String syms)
	{
		Set<String> ids = new HashSet<String>();

		if (egs != null) ids.addAll(Arrays.asList(parseToken(egs)));

		for (String sym : parseToken(syms))
		{
			String id = EGUtil.getEGID(sym);
			if (id != null) ids.add(id);
		}
		return ids;
	}
	
	protected Map<String, List<String>> parsePlatform(File platformFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(platformFile));
		
		String line = reader.readLine();

		while (ignoreLine(line)) line = reader.readLine();

		String[] col = line.split("\t");

		int egIndex = getColIndex(col, EG_NAMES);
		int symIndex = getColIndex(col, SYMBOL_NAMES);

		if (egIndex < 0 && symIndex < 0) throw new RuntimeException("Entrez Gene or Gene Symbol " +
			"column not recognized. Header = " + line);

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		for(line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (ignoreLine(line)) continue;
			
			String[] s = line.split("\t");
			if (s.length <= egIndex && s.length <= symIndex) continue;
			
			String id = s[0];
			String eg = egIndex > 0 ? s[egIndex].trim() : null;
			String sym = symIndex > 0 ? s[symIndex].trim() : null;

			if ((eg == null || eg.length() == 0) && (sym == null || sym.length() == 0))
			{
				continue;
			}

			map.put(id, new ArrayList<String>(getEGIDs(eg, sym)));
		}
		
		reader.close();
		return map;
	}
	
	protected double[] toNum(String[] valStr)
	{
		double[] v = new double[valStr.length];
		for (int i = 0; i < v.length; i++)
		{
			try
			{
				v[i] = Double.parseDouble(valStr[i]);
			}
			catch (NumberFormatException e) { v[i] = Double.NaN; }
		}
		return v;
	}
	
	protected Map<String, double[]> selectOne(Map<String, List<double[]>> eg2vals)
	{
		Map<String, double[]> eg2val = new HashMap<String, double[]>();

		for (String eg : eg2vals.keySet())
		{
			double max = -Double.MAX_VALUE;
			double[] maxV = null;
			
			for (double[] v : eg2vals.get(eg))
			{
				double mean = Summary.absoluteMean(v);
				
				if (mean > max)
				{
					max = mean;
					maxV = v;
				}
			}
			eg2val.put(eg, maxV);
		}
		return eg2val;
	}
	
	protected Map<String, double[]> parseDataFile(File dataFile, Map<String, List<String>> refMap)
		throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));

		String line = reader.readLine();

		while (ignoreLine(line)) line = reader.readLine();
		reader.readLine(); // Skip header

		Map<String, List<double[]>> eg2vals = new HashMap<String, List<double[]>>();

		for(line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (ignoreLine(line)) continue;

			int tabind = line.indexOf("\t");
			String id = line.substring(0, tabind);
			if (id.startsWith("\"")) id = id.substring(1, id.length()-1);

			String[] valStr = line.substring(tabind + 1).split("\t");

			if (refMap.containsKey(id))
			{
				for (String eg : refMap.get(id))
				{
					if (!eg2vals.containsKey(eg)) eg2vals.put(eg, new ArrayList<double[]>());
					eg2vals.get(eg).add(toNum(valStr));
				}
			}
		}

		return selectOne(eg2vals);
	}

	@Override
	public AlterationPack getAlterations(Node node)
	{
		String id = getEntrezGeneID(node);

		if (memo.containsKey(id)) return memo.get(id);

		AlterationPack alt = getAlterations(id);
		memo.put(id, alt);

		return alt;
	}

	public AlterationPack getAlterations(String id)
	{
		double[] value = dataMap.get(id);

		double ctrlMean = Summary.mean(value, controlIndex);
		double ctrlSD = Summary.stdev(value, controlIndex);

		Change[] ch = new Change[testIndex.length];

		int index = 0;
		for (int i : testIndex)
		{
			double ratio = value[i] / ctrlMean;
			double dif = Math.abs(value[i] - ctrlMean);

			ch[index] = Change.NO_CHANGE;
			if (dif > devThr * ctrlSD)
			{
				if (ratio > changeThr) ch[index] = Change.ACTIVATING;
				else if (ratio < changeThrInverse) ch[index] = Change.INHIBITING;
			}
			index++;
		}

		AlterationPack pack = new AlterationPack(id);
		pack.put(Alteration.EXPRESSION, ch);

		return pack;
	}

	static
	{
		File file = new File(dataDirectory);
		if (!file.exists()) file.mkdirs();
	}
}
