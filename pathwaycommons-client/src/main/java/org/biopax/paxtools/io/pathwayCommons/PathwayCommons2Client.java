package org.biopax.paxtools.io.pathwayCommons;

import cpath.service.Cmd;
import cpath.service.CmdArgs;
import cpath.service.jaxb.*;

import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.pathwayCommons.util.BioPAXHttpMessageConverter;
import org.biopax.paxtools.io.pathwayCommons.util.ErrorUtil;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.model.Model;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Pathway Commons 2 (PC2) Client. Please see
 *      http://www.pathwaycommons.org/pc2-demo/
 * for more information on the PC2 WEB API.
 * 
 * For "/get" and "/graph" queries, this client 
 * returns data in BioPAX L3 format only (or - error).
 * But BioPAX can be converted to other formats on the client side
 * (e.g., using converters provided by other Paxtools modules)
 * 
 * TODO shall we here support other output formats that '/get','/graph' can return? 
 */
public class PathwayCommons2Client
{
	public static final String JVM_PROPERTY_ENDPOINT_URL = "cPath2Url";
	public static final String DEFAULT_ENDPOINT_URL = "http://www.pathwaycommons.org/pc2/";
	private final String commandDelimiter = "?";
	
	private String endPointURL;
	private Integer page = 0;
    private Integer graphQueryLimit = 1;
    private Collection<String> organisms = new HashSet<String>();
    private Collection<String> dataSources = new HashSet<String>();
    private String type = null;
    private RestTemplate restTemplate;
    private String path = null;

    /**
     * Default constructor, initializes the class with
     * org.biopax.paxtools.io.SimpleIOHandler and
     * org.springframework.web.client.RestTemplate
     *
     */
    public PathwayCommons2Client() {
        this(new RestTemplate());
    }

    /**
     * @param restTemplate REST Template for making HTTP calls
     */
    public PathwayCommons2Client(RestTemplate restTemplate) {
        this(restTemplate, new SimpleIOHandler());
    }

    /**
     *
     * @param bioPAXIOHandler BioPAXIOHandler for reading BioPAX Models
     */
    public PathwayCommons2Client(BioPAXIOHandler bioPAXIOHandler) {
         this(new RestTemplate(), bioPAXIOHandler);
     }

    /**
     *
     * @param restTemplate REST Template for making HTTP calls
     * @param bioPAXIOHandler BioPAXIOHandler for reading BioPAX Models
     */
    public PathwayCommons2Client(RestTemplate restTemplate, BioPAXIOHandler bioPAXIOHandler) 
    {
        this.restTemplate = restTemplate;

        // set the service URL
        endPointURL = System.getProperty(JVM_PROPERTY_ENDPOINT_URL, DEFAULT_ENDPOINT_URL);
        assert endPointURL!= null :  "BUG: cpath2 URL is not defined!";
        
        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<HttpMessageConverter<?>>();
        httpMessageConverters.add(new BioPAXHttpMessageConverter(bioPAXIOHandler));

        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(Help.class, 
        		ServiceResponse.class, ErrorResponse.class,
        		SearchResponse.class, SearchHit.class, 
        		TraverseEntry.class, TraverseResponse.class
        );
        httpMessageConverters.add(new MarshallingHttpMessageConverter(jaxb2Marshaller, jaxb2Marshaller));
        
        restTemplate.setMessageConverters(httpMessageConverters);
    }

    private ServiceResponse findTemplate(Collection<String> keywords, boolean entitySearch) 
    		throws PathwayCommonsException 
    {
        String url = endPointURL +
        	(entitySearch ? Cmd.FIND_ENTITY : Cmd.FIND ) + commandDelimiter 
        	+ CmdArgs.q + "=" + join("", keywords, ",")
            + (getPage() > 0 ? "&" + CmdArgs.page + "=" + getPage() : "")
            + (getDataSources().isEmpty() ? "" : "&" + join(CmdArgs.datasource + "=", getDataSources(), "&"))
            + (getOrganisms().isEmpty() ? "" : "&" + join(CmdArgs.organism + "=", getOrganisms(), "&"))
            + (getType() != null ? "&" + CmdArgs.type + "=" + getType() : "");

        ServiceResponse resp = restTemplate.getForObject(url, ServiceResponse.class);
        if(resp instanceof ErrorResponse) {
            throw ErrorUtil.createException((ErrorResponse) resp);
        }
        return resp;
    }

    /**
     * Full text search of Pathway Commons. For example, retrieve a list of all records that contain the word, "BRCA2".
     * This command returns BioPAX Entity classes only.
     *
     * See http://www.pathwaycommons.org/pc2-demo/#find
     *
     * @param keyword a keyword, name or external identifier
     * @return see http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public ServiceResponse findEntity(String keyword) throws PathwayCommonsException {
        return findEntity(Collections.singleton(keyword));
    }

    /**
     * Full text search of Pathway Commons. For example, retrieve a list of all records that contain the word, "BRCA2".
     * This command returns BioPAX Entity classes only.
     *
     * See http://www.pathwaycommons.org/pc2-demo/#find
     *
     * @param keywords set of keywords, names or external identifiers
     * @return see http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public ServiceResponse findEntity(Collection<String> keywords) throws PathwayCommonsException {
        return findTemplate(keywords, true);
    }

    /**
     * Full text search of Pathway Commons. For example, retrieve a list of all records that contain the word, "BRCA2".
     * See http://www.pathwaycommons.org/pc2-demo/#find
     *
     * @param keyword a keyword, name or external identifier
     * @return see http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public ServiceResponse find(String keyword) throws PathwayCommonsException {
        return find(Collections.singleton(keyword));
    }

    /**
     * Full text search of Pathway Commons. For example, retrieve a list of all records that contain the word, "BRCA2".
     * See http://www.pathwaycommons.org/pc2-demo/#find
     *
     * @param keywords set of keywords, names or external identifiers
     * @return see http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public ServiceResponse find(Collection<String> keywords) throws PathwayCommonsException {
        return findTemplate(keywords, false);
    }

    /**
     * Retrieves details regarding one or more records, such as pathway,
     * interaction or physical entity. For example, get the complete
     * Apoptosis pathway from Reactome.
     * See http://www.pathwaycommons.org/pc2-demo/#get
     *
     * @param id a BioPAX element ID
     * @return BioPAX model containing the requested element
     */
    public Model get(String id) {
        return get(Collections.singleton(id));
    }

    /**
     * Retrieves details regarding one or more records, such as pathway,
     * interaction or physical entity. For example, get the complete
     * Apoptosis pathway from Reactome.
     * See http://www.pathwaycommons.org/pc2-demo/#get
     *
     * @param ids a set of BioPAX element IDs
     * @return BioPAX model containing the requested element
     */
    public Model get(Collection<String> ids) {
        String url = endPointURL + Cmd.GET + commandDelimiter 
        	+ join(CmdArgs.uri + "=" , ids, "&");
        return restTemplate.getForObject(url, Model.class);
    }

    /**
     *  Finds paths between a given source set of objects. The source set may contain Xref,
     *  EntityReference, and/or PhysicalEntity objects.
     *  See http://www.pathwaycommons.org/pc2-demo/#graph
     *
     * @param sourceSet set of xrefs, entity references, or physical entities
     * @return a BioPAX model that contains the path(s).
     */
    public Model getPathsBetween(Collection<String> sourceSet) 
    {
        String url = endPointURL + Cmd.GRAPH + commandDelimiter + CmdArgs.kind + "=pathsbetween&"
                        + join(CmdArgs.source + "=", sourceSet, "&") + "&"
                        + CmdArgs.limit + "=" + graphQueryLimit;

        return restTemplate.getForObject(url, Model.class);
    }

    //TODO implement public Model getPOI(Collection<String> sourceSet, Collection<String> targetSet)  ??
    
    /**
     * Searches directed paths from and/or to the given source set of entities, in the specified search limit.
     * See http://www.pathwaycommons.org/pc2-demo/#graph
     *
     * @param sourceSet Set of source physical entities
     * @return BioPAX model representing the neighborhood.
     */
    public Model getNeighborhood(Collection<String> sourceSet) {
        String url = endPointURL + Cmd.GRAPH + commandDelimiter + CmdArgs.kind + "=neighborhood&"
                        + join(CmdArgs.source + "=", sourceSet, "&") + "&"
                        + CmdArgs.limit + "=" + graphQueryLimit;

        return restTemplate.getForObject(url, Model.class);
    }

    /**
     * This query searches for the common upstream (common regulators) or
     * common downstream (common targets) objects of the given source set.
     * See http://www.pathwaycommons.org/pc2-demo/#graph
     *
     * @see STREAM_DIRECTION
     *
     * @param sourceSet set of physical entities
     * @param direction upstream or downstream
     * @return a BioPAX model that contains the common stream
     */
    public Model getCommonStream(Collection<String> sourceSet, STREAM_DIRECTION direction) 
    {
        String url = endPointURL + Cmd.GRAPH + commandDelimiter 
        	+ CmdArgs.kind + "=commonstream&"
            + join(CmdArgs.source + "=", sourceSet, "&") + "&"
            + CmdArgs.direction + "=" + direction + "&"
            + CmdArgs.limit + "=" + graphQueryLimit;

        return restTemplate.getForObject(url, Model.class);
    }

    
    /**
     * Gets the list of top (root) pathways 
     * (in the same xml format used by the full-text search commands)
     * 
     * @return
     */
    public SearchResponse getTopPathways() {
    	return restTemplate.getForObject(endPointURL 
    		+ Cmd.TOP_PATHWAYS, SearchResponse.class);
    }    
    
    public ServiceResponse traverse(Collection<String> uris) {
        String url = endPointURL + Cmd.GET + commandDelimiter 
        		+ join(CmdArgs.uri + "=", uris, "&")
        		+ "&" + CmdArgs.path + "=" + path;
        
        return restTemplate.getForObject(url, ServiceResponse.class);
    }
    
    
    /**
     * Can generate stings like 
     * "prefix=strings[1]&prefix=strings[2]..."
     * (if the delimiter is '&')
     * 
     * @param prefix
     * @param strings
     * @param delimiter
     * @return
     */
    private String join(String prefix, Collection<String> strings, String delimiter) {
        List<String> prefixed = new ArrayList<String>();

        for(String s: strings) {
            prefixed.add(prefix + s);
        }

        return StringUtils.join(prefixed, delimiter);
    }

    
    /**
     * The WEB Service API prefix. Default is http://www.pathwaycommons.org/pc2/
     * @return the end point URL as a string
     */
    public String getEndPointURL() {
        return endPointURL;
    }

    /**
     * @see #getEndPointURL()
     * @param endPointURL the end point URL as a string
     */
    public void setEndPointURL(String endPointURL) {
        this.endPointURL = endPointURL;
    }

    /**
     * Pathway Commons returns no more than 1000 search results per request.
     * You can request results beyond the first 1000 by using the page parameter.
     * Default is 0.
     *
     * See http://www.pathwaycommons.org/pc2-demo/#find
     *
     * @see #find(java.util.Collection)
     * @see #findEntity(java.util.Collection)
     *
     * @return the page number
     */
    public Integer getPage() {
        return page;
    }

    /**
     * @see #getPage()
     * @param page page number
     */
    public void setPage(Integer page) {
    	if(page >= 0)
    		this.page = page;
    	else 
    		throw new IllegalArgumentException("Negative page numbers are not supported!");
    }

    /**
     * Graph query search distance limit (default = 1).
     * See http://www.pathwaycommons.org/pc2-demo/#graph
     *
     * @see #getNeighborhood(java.util.Collection)
     * @see #getCommonStream(java.util.Collection, org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client.STREAM_DIRECTION)
     * @see #getPathsBetween(java.util.Collection)
     *
     * @return distance limit.
     */
    public Integer getGraphQueryLimit() {
        return graphQueryLimit;
    }

    /**
     * @see #getGraphQueryLimit()
     *
     * @param graphQueryLimit graph distance limit
     */
    public void setGraphQueryLimit(Integer graphQueryLimit) {
        this.graphQueryLimit = graphQueryLimit;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * BioPAX class filter for find() method.
     * See http://www.pathwaycommons.org/pc2-demo/#valid_biopax_parameter
     *
     * @see #find(String)
     * @see #findEntity(String)
     *
     * @return BioPAX L3 Class simple name
     */
    public String getType() {
        return type;
    }

    /**
     * @see #getType()
     *
     * @param type a BioPAX L3 Class
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Organism filter for find(). Multiple organism filters are allowed per query.
     * See http://www.pathwaycommons.org/pc2-demo/#valid_biopax_parameter
     *
     * @see #find(String)
     * @see #findEntity(String)
     *
     * @return set of strings representing organisms.
     */
    public Collection<String> getOrganisms() {
        return organisms;
    }

    /**
     * @see #getOrganisms()
     *
     * @param organisms set of strings representing organisms.
     */
    public void setOrganisms(Collection<String> organisms) {
        this.organisms = organisms;
    }

    /**
     * Data source filter for find(). Multiple data source filters are allowed per query.
     * See http://www.pathwaycommons.org/pc2-demo/#valid_datasource_parameter
     *
     * @see #find(String)
     * @see #findEntity(String)
     *
     * @return data sources as strings
     */
    public Collection<String> getDataSources() {
        return dataSources;
    }

    /**
     * @see #getDataSources()
     *
     * @param dataSources data sources as strings
     */
    public void setDataSources(Collection<String> dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * @see #getDataSources()
     * @see #setDataSources(java.util.Collection)
     * @return valid values for the datasource parameter as a Help object.
     */
    public Help getValidDataSources() {
        return getValidParameterValues(CmdArgs.datasource.toString());
    }

    /**
     * @see #getOrganisms()
     * @see #setOrganisms(java.util.Collection)
     * @return valid values for the organism parameter as a Help object.
     */
    public Help getValidOrganisms() {
        return getValidParameterValues(CmdArgs.organism.toString());
    }

    /**
     * @see #getType()
     * @see #setType(String)
     * @return valid values for the BioPAX type parameter.
     */
    public Collection<String> getValidTypes() {
        Set<String> types = new TreeSet<String>();
    	Help help = getValidParameterValues(CmdArgs.type.toString());
    	for(Help h : help.getMembers()) {
    		types.add(h.getId());
    	}
    	return types;
    }

    private Help getValidParameterValues(String parameter) {
        String url = endPointURL + "help/" + parameter + "s";
        return restTemplate.getForObject(url, Help.class);
    }

    public enum STREAM_DIRECTION {
        UPSTREAM("upstream"),
        DOWNSTREAM("downstream");

        private final String direction;

        STREAM_DIRECTION(String direction) {
            this.direction = direction;
        }

        public String toString() {
            return direction;
        }
    }

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
    
}
