package org.biopax.paxtools.io.pathwayCommons;

import org.biopax.paxtools.controller.Merger;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.model.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides querying and fetching relevant OWL model(s) from Pathway
 * Commons database (http://www.pathwaycommons.org) using the features
 * of Pathway Commons web service API:
 * (http://www.pathwaycommons.org/pc/webservice.do?cmd=help)
 *
 */
public class PathwayCommonsIOHandler
{
	private final BioPAXIOHandler ioHandler;

	Merger merger;

    /**
     * Prefix of the web service as a string.
     *
     * @see #setPrefix(String)
     */
    public static final String default_prefix =
		"http://www.pathwaycommons.org/pc/webservice.do?";
    /* PC web service constants */
    public static final String default_output_format = "biopax";
    public static final String version = "version=";
	public static final String command = "cmd=";
    public static final String input_id_type = "input_id_type=";
    public static final String output_id_type = "output_id_type=";
    public static final String data_source = "data_source=";
    public static final String output ="output=";
    public static final String default_version = "2.0";

	private String prefix = default_prefix;
	private final String output_format = default_output_format;
    private final String versionID = default_version;
    /* end of constants */

    /* default values defined in the web service help doc. */
    private ID_TYPE inputIdType = ID_TYPE.DEFAULT;
    private ID_TYPE outputIdType = ID_TYPE.DEFAULT;
    private DATA_SRC dataSource = DATA_SRC.DEFAULT;
    /* end of default values */

    public PathwayCommonsIOHandler(BioPAXIOHandler ioHandler) {
    	this.ioHandler = ioHandler;
    }
   
    /**
     * Sets the prefix of the webservice
     *
     * @param prefix    should contain the prefix "http://", and the postfix "webservice.do?". e.g. (http://www.new-address.com/webservice.do?)
     *
     * @see #default_prefix
     */
    public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

    /**
     * Sets input id type
     * @param newInputIdType    input id type to be set
     * @see                     org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.ID_TYPE
     */
    public void setInputIdType(ID_TYPE newInputIdType)
    {
       this.inputIdType = newInputIdType;
    }

    /**
     * Sets output id type
     * @param newOutputIdType   output id type to be set
     * @see                     org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.ID_TYPE
     */
    public void setOutputIdType(ID_TYPE newOutputIdType)
    {
       this.outputIdType = newOutputIdType;
    }

    /**
     * Sets the data source to obtain results only from a specific data source.
     * @param newDataSource     data source to be used
     * @see                     org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.DATA_SRC
     */
    public void setDataSource(DATA_SRC newDataSource)
    {
       this.dataSource = newDataSource;
    }

    private InputStream openURL(String urlName) throws IOException
	{
			URL url = new URL(urlName);
			URLConnection urlConnection = url.openConnection();
			return urlConnection.getInputStream();
	}

	private Model readBioPAXfromURL(String urlName) throws IOException
	{
		return ioHandler.convertFromOWL(openURL(urlName));
	}

    /**
     * Retrieves the records of an entity specified by its id, and returns
     * the results as a model.
     *
     * Pathway commons definition:
     * Retrieves details regarding one or more records, such as a pathway,
     * interaction or physical entity. For example, get the complete
     * Apoptosis pathway from Reactome.
     *
     * @param id    entity id
     * @throws IOException if connection fails for some reason.
     * @return      empty model in case of errors.
     *
     * @see #setPrefix(String)
     */
    public Model retrieveByID(String id) throws IOException
	{
		String urlName = prefix+command+"get_record_by_cpath_id"+
			"&"+
			version+versionID +
			"&"+
			"q="+id +
			"&"+
			output+ output_format;
		return readBioPAXfromURL(urlName);
	}

    /**
     * Get pathways to which the entity, specified by its id, is related
     * and returns a two-dimensional ArrayList containing all results as rows
     * and their subproperties as columns. First row contains the column types
     * (Database:ID, Pathway_Name, Pathway_Database_Name, CPATH_ID).
     *
     * Pathway Commons definition:
     * Retrieves all pathways involving a specified physical entity
     * (e.g. protein or small molecule).
     *
     * For example, get all pathways involving BRCA2.
     *
     * @param id    entity id
     * @return      An ArrayList containing "PHYSICAL_ENTITY_ID_NOT_FOUND" as the second column in case of no results
     *
     * @see #setDataSource(org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.DATA_SRC)
     * @see #setInputIdType(org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.ID_TYPE)
     * @see #setOutputIdType(org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.ID_TYPE)
     * @see #setPrefix(String)
     *
     */
    public List<List<String>> getPathways(String id) throws IOException
    {
        /* "get_pathway_list" command will probably be replaced with "get_pathways"
         * in the upcoming versions of the webservice. Please, see the comment below.
         *
         * String urlName = prefix+command+"get_pathways"+
         */
        String urlName = prefix+command+"get_pathways"+
			"&"+
			version+versionID +
			"&"+
			"q="+id +
			"&"+
			output+ output_format +
			"&"+
            input_id_type + inputIdType.getTag() +
			"&"+
            data_source + dataSource.getTag();

        BufferedReader reader = new BufferedReader( new InputStreamReader(openURL(urlName)) );
        String oneLine;
        List<List<String>> allLines = new ArrayList<List<String>>();

        while((oneLine = reader.readLine()) != null ) {
            List<String> lineArray = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(oneLine, "\t");
            while(tokenizer.hasMoreTokens())
            {
                lineArray.add(tokenizer.nextToken());
            }

			if (!lineArray.isEmpty())
			{
				allLines.add(lineArray);
			}
        }

        return allLines;
    }

    /**
     * Gets neighbors of an entity specified by an id in biopax format
     * and returns the results as a model.
     *
     * Pathway Commons definition:
     * Retrieves the nearest neighbors of a given physical entity
     * (e.g. gene, protein or small molecule).
     *
     * For example, get all the neighbors of BRCA2.
     *
     * @param id    entity id
     * @return      empty model in case of errors.
     *
     * @see #setDataSource(org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.DATA_SRC)
     * @see #setInputIdType(org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.ID_TYPE)
     * @see #setOutputIdType(org.biopax.paxtools.io.pathwayCommons.PathwayCommonsIOHandler.ID_TYPE)
     * @see #setPrefix(String)
     */
    public Model getNeighbors(String id) throws IOException
    {
        String urlName = prefix+command+"get_neighbors"+
			"&"+
			version+versionID +
			"&"+
			"q="+id +
			"&"+
			output+ output_format +
			"&"+
            input_id_type + inputIdType.getTag() +
			"&"+
            output_id_type + outputIdType.getTag() +
			"&"+
            data_source + dataSource.getTag();

        return readBioPAXfromURL(urlName);
	}

    /**
     * ID types that are supported by Pathway Commons web API
     */
    public enum ID_TYPE
	{
        DEFAULT("CPATH_ID"),
        UNIPROT("UNIPROT"),
		CPATH_ID("CPATH_ID"),
		ENTREZ_GENE("ENTREZ_GENE");

        private final String tag;

        ID_TYPE(String tag)
        {
           		this.tag = tag;
        }

        public String getTag()
        {
           		return tag;
        }
    }

    /**
     * Data source names that are supported by Pathway Commons web API
     */
    public enum DATA_SRC
    {
     	DEFAULT(""),
      	CELL_MAP("CELL_MAP"),
      	HUMANCYC("HUMANCYC"),
      	NCI_NATURE("NCI_NATURE"),
      	REACTOME("REACTOME");

      	private final String tag;

      	DATA_SRC(String tag)
      	{
          	this.tag = tag;
      	}

      	public String getTag()
      	{
          	return tag;
      	}
    }
}
