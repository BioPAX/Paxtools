package org.biopax.paxtools.io.pathwayCommons;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.pathwayCommons.model.SearchResponse;
import org.biopax.paxtools.io.pathwayCommons.util.BioPAXHttpMessageConverter;
import org.biopax.paxtools.io.pathwayCommons.util.ErrorUtil;
import org.biopax.paxtools.io.pathwayCommons.util.PathwayCommonsException;
import org.biopax.paxtools.io.pathwayCommons.util.SearchResponseHttpMessageConverter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Pathway Commons 2 (PC2) Client. Please see
 *      http://www.pathwaycommons.org/pc2-demo/
 * for more information on the PC2 WEB API.
 */
public class PathwayCommons2Client
{
    private String endPointURL = "http://www.pathwaycommons.org/pc2/";
    private Integer page = 0;
    private String outputFormat = "biopax";
    private Integer graphQueryLimit = 1;
    private Collection<String> organisms = new HashSet<String>();
    private Collection<String> dataSources = new HashSet<String>();
    private Class<? extends BioPAXElement> type = BioPAXElement.class;

    private final String graphCommand = "graph";
    private final String findCommand = "find";
    private final String getCommand = "get";

    private final String graphSubCommand = "kind";

    private final String formatString = "format";
    private final String sourceString = "source";
    private final String directionString = "direction";
    private final String limitString = "limit";
    private final String uriString = "uri";
    private final String queryString = "q";
    private final String dataSourceString = "datasource";
    private final String typeString = "type";
    private final String organismString = "organism";
    private final String pageString = "page";

    private final String commandDelimiter = "?";
    private final String graphURL = endPointURL + graphCommand + commandDelimiter;
    private final String getURL = endPointURL + getCommand + commandDelimiter;
    private final String findURL = endPointURL + findCommand + commandDelimiter;

    private RestTemplate restTemplate;

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
    public PathwayCommons2Client(RestTemplate restTemplate, BioPAXIOHandler bioPAXIOHandler) {
        this.restTemplate = restTemplate;

        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<HttpMessageConverter<?>>();
        httpMessageConverters.add(new BioPAXHttpMessageConverter(bioPAXIOHandler));
        httpMessageConverters.add(new SearchResponseHttpMessageConverter());

        restTemplate.setMessageConverters(httpMessageConverters);
    }

    /**
     * Full text search of Pathway Commons. For example, retrieve a list of all records that contain the word, "BRCA2".
     * See http://www.pathwaycommons.org/pc2-demo/#find
     *
     * @param keyword a keyword, name or external identifier
     * @return see http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public SearchResponse find(String keyword) throws PathwayCommonsException {
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
    public SearchResponse find(Collection<String> keywords) throws PathwayCommonsException {
        String url = findURL + queryString + "=" + joinStrings(keywords, ",") + "&"
                     + pageString + "=" + getPage() + "&"
                     + (getDataSources().isEmpty() ? "" : dataSourceString + "=" + joinStrings(getDataSources(), ",") + "&")
                     + (getOrganisms().isEmpty() ? "" : organismString + "=" + joinStrings(getOrganisms(), ",") + "&")
                     + typeString + "=" + bioPAXClassToTypeString(getType());

        SearchResponse searchResponse = restTemplate.getForObject(url, SearchResponse.class);
        if(searchResponse.getError() != null) {
            throw ErrorUtil.createException(searchResponse.getError());
        }
        return searchResponse;
    }

    /**
     * Converts BioPAX classes to PC2 compatible names.
     * See http://www.pathwaycommons.org/pc2-demo/#additional_parameters
     *
     * @param type BioPAX L3 model class
     * @return a string compatible with PC2 WEB API
     */
    public static String bioPAXClassToTypeString(Class<? extends BioPAXElement> type) {
        return type.getSimpleName(); 
    }

    /**
     * Retrieves details regarding one or more records, such as pathway,
     * interaction or physical entity. For example, get the complete
     * Apoptosis pathway from Reactome.
     * See http://www.pathwaycommons.org/pc2-demo/#get
     *
     * @param id a BioPAX element ID
     * @return BioPAX model containing the requested element
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public Model get(String id) throws PathwayCommonsException {
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
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public Model get(Collection<String> ids) throws PathwayCommonsException {
        String url = getURL + uriString + "=" + joinStrings(ids, ",");
        return restTemplate.getForObject(url, Model.class);
    }

    /**
     *  Finds paths between a given source set of objects. The source set may contain Xref,
     *  EntityReference, and/or PhysicalEntity objects.
     *  See http://www.pathwaycommons.org/pc2-demo/#graph
     *
     * @param sourceSet set of xrefs, entity references, or physical entities
     * @return a BioPAX model that contains the path(s).
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public Model getPathsBetween(Collection<String> sourceSet) throws PathwayCommonsException {
        String url = graphURL + graphSubCommand + "=pathsbetween&"
                        + sourceString + "=" + joinStrings(sourceSet, ",") + "&"
                        + limitString + "=" + graphQueryLimit + "&"
                        + formatString + "=" + outputFormat;

        return restTemplate.getForObject(url, Model.class);
    }

    /**
     * Searches directed paths from and/or to the given source set of entities, in the specified search limit.
     * See http://www.pathwaycommons.org/pc2-demo/#graph
     *
     * @param sourceSet Set of source physical entities
     * @return BioPAX model representing the neighborhood.
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public Model getNeighborhood(Collection<String> sourceSet) throws PathwayCommonsException {
        String url = graphURL + graphSubCommand + "=neighborhood&"
                        + sourceString + "=" + joinStrings(sourceSet, ",") + "&"
                        + limitString + "=" + graphQueryLimit + "&"
                        + formatString + "=" + outputFormat;

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
     * @throws PathwayCommonsException when the WEB API gives an error
     */
    public Model getCommonStream(Collection<String> sourceSet, STREAM_DIRECTION direction) throws PathwayCommonsException {
        String url = graphURL + graphSubCommand + "=commonstream&"
                        + sourceString + "=" + joinStrings(sourceSet, ",") + "&"
                        + directionString + "=" + direction + "&"
                        + limitString + "=" + graphQueryLimit + "&"
                        + formatString + "=" + outputFormat;

        return restTemplate.getForObject(url, Model.class);
    }

    private String joinStrings(Collection strings, String delimiter) {
        String finalString = "";

        for(Object s: strings)
            finalString += s + delimiter;

        return finalString.substring(0, finalString.length() - delimiter.length());
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
        this.page = page;
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
     *
     * @return BioPAX L3 Class
     */
    public Class<? extends BioPAXElement> getType() {
        return type;
    }

    /**
     * @see #getType()
     *
     * @param type a BioPAX L3 Class
     */
    public void setType(Class<? extends BioPAXElement> type) {
        this.type = type;
    }

    /**
     * Organism filter for find(). Multiple organism filters are allowed per query.
     * See http://www.pathwaycommons.org/pc2-demo/#valid_biopax_parameter
     *
     * @see #find(String)
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

}
