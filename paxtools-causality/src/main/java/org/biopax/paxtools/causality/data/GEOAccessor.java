package org.biopax.paxtools.causality.data;

import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.Change;
import org.biopax.paxtools.causality.model.Node;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
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
	
	protected static String dataDirectory = "data";
	
	protected final static String SERIES_URL_PREFIX =
		"ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SeriesMatrix/";

	protected final static String SERIES_URL_SUFFIX = "_series_matrix.txt.gz";

	protected final static String PLATFORM_URL_PREFIX =
		"http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?targ=self&form=text&view=data&acc=";

	protected final static String PLATFORM_LINE_INDICATOR = "!Series_platform_id";

	public GEOAccessor(String gseID, int[] testIndex, int[] controlIndex)
	{
		this.gseID = gseID;
		this.testIndex = testIndex;
		this.controlIndex = controlIndex;
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

	
	@Override
	public Map<Alteration, Change[]> getAlterations(Node node)
	{
		String id = getEntrezGeneID(node);

		double[] value = dataMap.get(id);

		return null;
	}
}
