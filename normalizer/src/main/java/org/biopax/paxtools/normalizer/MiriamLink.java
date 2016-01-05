package org.biopax.paxtools.normalizer;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.biomodels.miriam.*;
import net.biomodels.miriam.Miriam.Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton local MIRIAM data resolver with all methods static.
 * 
 * @author rodche
 */
public class MiriamLink
{
	private static final Logger log = LoggerFactory.getLogger(MiriamLink.class);
	private static final String[] ARRAY_OF_STRINGS = {}; // a template to convert a Collection<T> to String[]
	
	
	/** default address to access to the services */
	public static final String XML_LOCATION = "http://www.ebi.ac.uk/miriam/main/export/xml/";
		//"http://www.ebi.ac.uk/miriam/main/XMLExport";
	/** package name for jaxb to use */
	public static final String BINDING = "net.biomodels.miriam";
	public static final String SCHEMA_LOCATION = "http://www.ebi.ac.uk/compneur-srv/miriam/static/main/xml/MiriamXML.xsd";
   
    /** object of the generated from the Miriam schema type */
    private static final Miriam miriam;
    
    /** */
    private static final Map<String,Datatype> datatypesHash = new HashMap<String, Miriam.Datatype>();
    
    
    public static boolean useObsoleteDatatypes = true;
    public static boolean useObsoleteResources = true;
	
	/**
	 * Default constructor: initialization of some parameters
	 */
	protected MiriamLink() {
    }
	
	/**
	 * Initialization
	 */
	static
	{
		try
	    {
			URL url = new URL(XML_LOCATION);
            JAXBContext jc = JAXBContext.newInstance(BINDING);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            
            Miriam mir = null;
            //mir = (Miriam) unmarshaller.unmarshal(conn.getInputStream());
            try {
            	mir = (Miriam) unmarshaller.unmarshal(url.openStream());
       			log.info("Got the latest Miriam XML db from " + XML_LOCATION);
            } catch (IOException e) {
            	// fall-back (to using local Miriam.xml)
            	log.warn("Cannot connect to Miriam resource: " + e
            		+ "; now trying to find/use Miriam.xml from classpath...");
            	InputStream is = MiriamLink.class.getResourceAsStream("/Miriam.xml");
            	if(is != null) {
            		mir = (Miriam) unmarshaller.unmarshal(is);
            	} else {
            		throw new RuntimeException("Miriam.xml is neither available online " 
            			+ " at " + XML_LOCATION +
            			" nor it can be found in the root of classpath!");
            	}
			}
            
            miriam = mir;
            
            log.info("MIRIAM XML imported, version: "
	                + miriam.getDataVersion() + ", datatypes: "
	                + miriam.getDatatype().size());
	    }
	    catch (JAXBException e) {
	        throw new RuntimeException(e);
	    }
	    catch (MalformedURLException e) {
	    	throw new RuntimeException(e);
		}
	    
	    // build the name-datatype static hash (once!)
		for(Datatype dt : miriam.getDatatype()) {
			// index by name
			datatypesHash.put(dt.getName().toUpperCase(), dt);
			
			// by identifier
			datatypesHash.put(dt.getId().toUpperCase(), dt);
			
			// index by each synonym
			// (Miriam must guarantee: different datatypes cannot have the same synonym!)
			Synonyms synonyms = dt.getSynonyms();
			if (synonyms != null) {
				for (String syn : synonyms.getSynonym()) {
					datatypesHash.put(syn.toUpperCase(), dt);
				}
			}
			
			// index by each URI
			for(Uris uris : dt.getUris()) {
				for(Uri uri : uris.getUri()) {
					datatypesHash.put(uri.getValue(), dt);
				}
			}
		}
	}

	
	/**
     * Retrieves the current version of MIRIAM Web Services.  
     * 
     * @return Current version of the Web Services
	 */
    public static String getServicesVersion()
    {
        return miriam.getDate().toString() 
        	+ "; " +  miriam.getDataVersion().toString();
    }
       
     
    /**
     * Retrieves the unique (official) URI of a data type (example: "urn:miriam:uniprot").
     * 
     * @param datatypeKey - ID, name, synonym, or (incl. deprecated) URI (URN or URL) of a data type (examples: "UniProt")
     * @return unique URI of the data type
     * 
     * @throws IllegalArgumentException when datatype not found
     */
    public static String getDataTypeURI(String datatypeKey)
    {
    	Datatype datatype = getDatatype(datatypeKey);
    	return getOfficialDataTypeURI(datatype); 
    }
     
     
    /**
     * Retrieves all the URIs of a data type, including all the deprecated ones 
     * (examples: "urn:miriam:uniprot", "http://www.uniprot.org/", "urn:lsid:uniprot.org:uniprot", ...).
     * 
     * @param datatypeKey name (or synonym), ID, or URI (URN or URL) of the data type (examples: "ChEBI", "UniProt")
     * @return all the URIs of a data type (including the deprecated ones)
     * 
     * @throws IllegalArgumentException when datatype not found
     */
    public static String[] getDataTypeURIs(String datatypeKey)
    {
       	Set<String> alluris = new HashSet<String>();
    	Datatype datatype = getDatatype(datatypeKey);
    	for(Uris uris : datatype.getUris()) {
    		for(Uri uri : uris.getUri()) {
    			alluris.add(uri.getValue());
    		}
    	}
    	return alluris.toArray(ARRAY_OF_STRINGS);
    }
	
	
	/**
	 * Retrieves the location (or country) of a resource (example: "United Kingdom").
	 * 
	 * @param resourceId identifier of a resource (example: "MIR:00100009")
	 * @return the location (the country) where the resource is managed
	 */
	public static String getResourceLocation(String resourceId)
	{
		Resource resource = getResource(resourceId);
    	return resource.getDataLocation();
	}
	
	
	/**
	 * Retrieves the institution which manages a resource (example: "European Bioinformatics Institute").
	 * 
	 * @param resourceId identifier of a resource (example: "MIR:00100009")
	 * @return the institution managing the resource
	 */
	public static String getResourceInstitution(String resourceId)
	{
		Resource resource = getResource(resourceId);
    	return resource.getDataInstitution();
	}
	  
    
    /**
     * Retrieves the unique MIRIAM URI of a specific entity (example: "urn:miriam:obo.go:GO%3A0045202").
     * 
     * @param name - name, URI/URL, or ID of a data type (examples: "ChEBI", "MIR:00000005")
     * @param id identifier of an entity within the data type (examples: "GO:0045202", "P62158")
     * @return unique standard MIRIAM URI of a given entity
     * 
     * @throws IllegalArgumentException when datatype not found
     */
    public static String getURI(String name, String id)
    {
    	Datatype datatype = getDatatype(name);
    	String db = datatype.getName();
    	if(checkRegExp(id, db)) {
    		try {
				return getOfficialDataTypeURI(datatype)	+ ":" + URLEncoder.encode(id, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("UTF-8 encoding error of id=" + id, e);
			}
    	} else 
    		throw new IllegalArgumentException(
				"ID pattern mismatch. db=" + db + ", id=" + id
				+ ", regexp: " + datatype.getPattern());
    }
    
    
    /**
	 * Retrieves the definition of a data type.
	 * 
	 * @param datatypeKey - ID, name or URI (URN or URL) of a data type
	 * @return definition of the data type
	 * 
	 * @throws IllegalArgumentException when datatype not found
	 */
    public static String getDataTypeDef(String datatypeKey)
    {
    	Datatype datatype = getDatatype(datatypeKey);
    	return datatype.getDefinition();
    }
       
     
    /**
     * Retrieves the physical locationS (URLs) of web pageS providing knowledge about an entity.
     * 
     * @param datatypeKey name (can be a synonym), ID, or URI of a data type (examples: "Gene Ontology", "UniProt")
     * @param entityId identifier of an entity within the given data type (examples: "GO:0045202", "P62158")
     * @return physical locationS (URL templates) of web pageS providing knowledge about the given entity
     * 
     * @throws IllegalArgumentException when datatype not found
     */
    public static String[] getLocations(String datatypeKey, String entityId)
    {
       	Set<String> locations = new HashSet<String>();
		Datatype datatype = getDatatype(datatypeKey);
		for (Resource resource : getResources(datatype)) {
			String link = resource.getDataEntry();
			try {
				link = link.replaceFirst("\\$id", URLEncoder.encode(entityId, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			locations.add(link);
		}
    	return locations.toArray(ARRAY_OF_STRINGS);
    }
       
    
    /**
     * Retrieves home page URLs of a datatype.
     * 
     * @param datatypeKey - name (can be a synonym), ID, or URI (URL or URN) of a data type
     * @return array of strings containing all the address of the main page of the resources of the data type
     * 
     * @throws IllegalArgumentException when datatype not found
	 */
    public static String[] getDataResources(String datatypeKey)
    {
       	Set<String> locations = new HashSet<String>();
    	Datatype datatype = getDatatype(datatypeKey);
		for (Resource resource : getResources(datatype)) {
			String link = resource.getDataResource();
			locations.add(link);
		}
    	return locations.toArray(ARRAY_OF_STRINGS);
    }
    
    
    /**
	 * To know if a URI of a data type is deprecated.
	 * 
	 * @param uri (URN or URL) of a data type
	 * @return answer ("true" or "false") to the question: is this URI deprecated?
	 */
    public static boolean isDeprecated(String uri)
    {
    	Datatype datatype = datatypesHash.get(uri);
    	String urn = getOfficialDataTypeURI(datatype);
    	return !uri.equalsIgnoreCase(urn);
    }
    
   
    /**
     * Retrieves the pattern (regular expression) used by the identifiers within a data type.
     * 
     * @param datatypeKey data type ID, name (or synonym), or URI (URL or URN)
     * @return pattern of the data type
     * 
     * @throws IllegalArgumentException when datatype not found
	 */
    public static String getDataTypePattern(String datatypeKey)
    {
    	Datatype datatype = getDatatype(datatypeKey);
    	return datatype.getPattern();
    }
    
    
    /**
	 * Retrieves the preferred name of a data type.
	 * 
	 * @param datatypeKey URI (URL or URN), ID, or nickname of a data type
	 * @return the common name of the data type
	 * 
	 * @throws IllegalArgumentException when not found
	 */
    public static String getName(String datatypeKey)
    {
    	Datatype datatype = getDatatype(datatypeKey);
    	return datatype.getName();
    }
    
    
    /**
	 * Retrieves all the synonyms (incl. the preferred name) of a data type.
	 * 
	 * @param datatypeKey ID, any name, or URI (URL or URN) of a data type
	 * @return all the data type's synonyms (incl. preferred name)
	 */
    public static String[] getNames(String datatypeKey)
    {
    	Set<String> names = new HashSet<String>();
    	Datatype datatype = getDatatype(datatypeKey);
    	names.add(datatype.getName());
    	Synonyms synonyms = datatype.getSynonyms();
    	if(synonyms != null)
    	  for(String name : synonyms.getSynonym()) {
    		names.add(name);
    	  }
    	return names.toArray(ARRAY_OF_STRINGS);
    }

    
    /**
     * Retrieves the list of preferred names of all the data types available.
     * 
     * @return list of names of all the data types
     */
    public static String[] getDataTypesName()
    {
        Set<String> dataTypeNames = new HashSet<String>();
        for(Datatype datatype : miriam.getDatatype()) {
        	dataTypeNames.add(datatype.getName());
        }
        return dataTypeNames.toArray(ARRAY_OF_STRINGS);
    }
    
    
    /**
     * Retrieves the internal identifier (stable and perennial) of 
     * all the data types (for example: "MIR:00000005").
     * 
     * @return list of the identifier of all the data types
     */
    public static String[] getDataTypesId()
    {
        Set<String> dataTypeIds = new HashSet<String>();
        for(Datatype datatype : miriam.getDatatype()) {
        	dataTypeIds.add(datatype.getId());
        }
        return dataTypeIds.toArray(new String[] {});
    }
    
    
    /**
     * Retrieves the official URI (it will always be URN) of 
     * a data type corresponding to the deprecated one.
     * 
     * @param uri deprecated URI (URN or URL) of a data type 
     * @return the official URI of a data type corresponding to the deprecated one
     * @deprecated use getDataTypeURI instead
     */
    public static String getOfficialDataTypeURI(String uri)
    {
        return getDataTypeURI(uri);
    }
    
    
    /**
     * Checks if the identifier given follows the regular expression 
     * of its data type (also provided).
     * 
     * @param identifier internal identifier used by the data type
     * @param datatype name, synonym or URI of a data type
     * @return "true" if the identifier follows the regular expression, "false" otherwise
     * 
     * @throws IllegalArgumentException when datatype not found
     */
    public static boolean checkRegExp(String identifier, String datatype)
    {
    	Datatype dt = getDatatype(datatype);
    	return Pattern.compile(dt.getPattern()).matcher(identifier).find();
    } 
        

    /**
     * Retrieves the unique (official) URI of a data type (example: "urn:miriam:uniprot").
     * 
     * @param datatype net.biomodels.miriam.Miriam.Datatype
     * @return
     */
    public static String getOfficialDataTypeURI(Datatype datatype) {
    	for(Uris uris : datatype.getUris()) {
    		for(Uri uri : uris.getUri()) {
    			if(!isDeprecated(uri) && uri.getType() == UriType.URN) {
    				return uri.getValue();
    			}
    		}
    	}
    	
    	return null;
    }
    
    
    private static boolean isDeprecated(Uri uri) {
		if(uri.isDeprecated()!=null && uri.isDeprecated()) {
			return true;
		} else {
			return false;
		}
	}


	/**
     * Gets Miriam Datatype by its ID, Name, Synonym, or URI (URN/URL) 
     * 
     * @param datatypeKey - a datatype ID, name, synonym, or URI
     * @return
     * 
	 * @throws IllegalArgumentException when not found
     */
	public static Datatype getDatatype(String datatypeKey) 
	{	
		Datatype dt = null;
		if(containsIdOrName(datatypeKey))
			dt = datatypesHash.get(datatypeKey.toUpperCase());
		else if(containsUri(datatypeKey)) 
			dt = datatypesHash.get(datatypeKey);
		else
			throw new IllegalArgumentException("Datatype not found : " + datatypeKey);
		
		if(!useObsoleteDatatypes 
				&& dt.isObsolete() != null 
				&& dt.isObsolete().booleanValue() == true)
			throw new IllegalArgumentException("Datatype " +
				datatypeKey + "(" + dt.getName() + ") is obsolete" +
					" (and useObsoleteDatatypes=false)");
		
		// return 
		return dt;
	}


    /**
     * Retrieves the internal identifier (stable and perennial) of 
     * all the resources (for example: "MIR:00100008" (bind) ).
     * 
     * @return list of the identifiers of all data types
     */
    public static String[] getResourcesId()
    {
        Set<String> ids = new HashSet<String>();
        for(Datatype datatype : miriam.getDatatype()) {
			for (Resource resource : getResources(datatype)) {
				ids.add(resource.getId());
			}
        }
        return ids.toArray(new String[] {});
    }
	
    
    /**
     * Retrieves the resource by id (for example: "MIR:00100008" (bind) ).
     * 
     * @param resourceId - resource identifier (similar to, but not a data type identifier!)
     * @return
     */
    public static Resource getResource(String resourceId)
    {
        for(Datatype datatype : miriam.getDatatype()) {
			for (Resource resource : getResources(datatype)) {
				if (resource.getId().equalsIgnoreCase(resourceId))
					return resource;
			}
        }
        
        throw new IllegalArgumentException("Resource not found : " + resourceId);
    }

    
    /**
     * Check whether Miriam contains a data type record with this 
     * name (any synonym) or identifier (case insensitive)
     *  
     * @param searchKey  - ID, name, or synonym (case insensitive)
     * @return
     */
    public static boolean containsIdOrName(String searchKey) {
    	return datatypesHash.keySet().contains(searchKey.toUpperCase());
    }
 
    
    /**
     * Check whether Miriam contains a data type record with this URI
     * (case sensitive)
     * 
     * @param searchUri - URI (case sensitive)
     * @return
     */
    public static boolean containsUri(String searchUri) {
    	return datatypesHash.keySet().contains(searchUri);
    }
    
    
    /**
     * Gets the list of Miriam {@link Resource} from 
     * the datatype, also taking the 'obsolete' flag/state
     * into account.
     * 
     * @param datatype
     * @return
     */
    private static List<Resource> getResources(Datatype datatype) {
    	List<Resource> toReturn = new ArrayList<Resource>();
    	
    	Resources resources = datatype.getResources();
		if (resources != null) {
			for (Resource resource : resources.getResource()) {
				if(useObsoleteResources // ok to collect (- do not care about obsolete)
					|| resource.isObsolete() == null // undefined - here means non-obsolete (ok)
						|| !resource.isObsolete().booleanValue()) {
					toReturn.add(resource);
				}
			}
		}
		
		return toReturn;
    }
 
    
    /**
     * Converts a MIRIAM URN into its equivalent Identifiers.org URL.
     * 
     * @see #getURI(String, String) - use this to get the URN
     * @see #getIdentifiersOrgURI(String, String) - prefered URI
     * 
     * @param urn - an existing Miriam URN, e.g., "urn:miriam:obo.go:GO%3A0045202"
     * @return the Identifiers.org URL corresponding to the data URN, e.g., "http://identifiers.org/obo.go/GO:0045202"
     * 
     * @deprecated this method applies {@link URLDecoder#decode(String)} to the last part of the URN, which may not always work as expected (test yours!) 
     */
    public static String convertUrn(String urn) {
    	String[] tokens = urn.split(":");
    	return "http://identifiers.org/" + tokens[tokens.length-2] 
    		+ "/" + URLDecoder.decode(tokens[tokens.length-1]);
    }
    
    
    /**
     * Gets the Identifiers.org URI/URL of the entity (example: "http://identifiers.org/obo.go/GO:0045202").
     * 
     * @param name - name, URI/URL, or ID of a data type (examples: "ChEBI", "MIR:00000005")
     * @param id identifier of an entity within the data type (examples: "GO:0045202", "P62158")
     * @return Identifiers.org URL for the id
     * 
     * @throws IllegalArgumentException when datatype not found
     */
    public static String getIdentifiersOrgURI(String name, String id)
    {
    	String url = null;
    	Datatype datatype = getDatatype(name);
    	String db = datatype.getName();
    	if(checkRegExp(id, db)) {
   			uris: 
   			for(Uris uris : datatype.getUris()) {
   				for(Uri uri : uris.getUri()) {
   					if(uri.getValue().startsWith("http://identifiers.org/")) {
   						url = uri.getValue() + id;
   						break uris;
   					}
   				}
   			}
    	} else 
    		throw new IllegalArgumentException(
				"ID pattern mismatch. db=" + db + ", id=" + id
				+ ", regexp: " + datatype.getPattern());
    	
    	return url;
    }
}
