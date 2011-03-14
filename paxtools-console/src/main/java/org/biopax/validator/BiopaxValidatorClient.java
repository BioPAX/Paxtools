package org.biopax.validator;

import java.io.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.biopax.validator.jaxb.Behavior;
import org.biopax.validator.jaxb.ValidatorResponse;

/**
 * Simple (example) BioPAX Validator client 
 * to upload and check BioPAX OWL files.
 * 
 * @author rodche
 *
 */
public class BiopaxValidatorClient {
	private static final Log log = LogFactory.getLog(BiopaxValidatorClient.class);
	
	/**
	 * Default BioPAX Validator's URL
	 */
	public static final String 
		DEFAULT_VALIDATOR_URL = "http://www.biopax.org/biopax-validator/check.html";
	
	public static enum RetFormat {
		HTML,// errors as HTML/Javascript 
		XML, // errors as XML
		OWL; // modified BioPAX only (when 'autofix' or 'normalize' is true)
	}
	
	private static HttpClient httpClient = new HttpClient();
	private String url;

	
    /**
     * Main Constructor
     * 
     * It configures for the validator's URL
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * and result format ().
     * 
     * @param url - validator's file-upload form address
     */
    public BiopaxValidatorClient(String url) {
		this.url = (url != null) ? url : DEFAULT_VALIDATOR_URL;
	}
    
    
    /**
     * Default Constructor
     * 
     * It configures for the default validator
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * to return XML result.
     */
    public BiopaxValidatorClient() {
    	this(DEFAULT_VALIDATOR_URL);
	}
       

    /**
     * Checks a BioPAX OWL file(s) or resource 
     * using the online BioPAX Validator 
     * and prints the results to the output stream.
     * 
     * @param autofix true/false (experimental)
     * @param normalize true/false (experimental)
     * @param retFormat xml, html, or owl (no errors, just modified owl, - when 'autofix' or 'normalize' is true); see also {@link RetFormat}
     * @param biopaxUrl check the BioPAX at the URL
     * @param biopaxFiles an array of BioPAX files to validate
     * @param out
     * @throws IOException
     */
    public void validate(boolean autofix, boolean normalize, RetFormat retFormat, Behavior filterBy,
    		String biopaxUrl, File[] biopaxFiles, OutputStream out) throws IOException 
    {
        Collection<Part> parts = new HashSet<Part>();
        
        if(autofix) {
        	parts.add(new StringPart("autofix", "true"));
        }
        
        if(normalize) {
        	parts.add(new StringPart("normalize", "true"));
        }
        
        // set result type
		if (retFormat != null) {
			parts.add(new StringPart("retDesired", retFormat.toString().toLowerCase()));
		}
        
		if(filterBy != null) {
			parts.add(new StringPart("retDesired", filterBy.toString()));
		}
		
        // add data
		if (biopaxFiles != null && biopaxFiles.length > 0) {
			for (File f : biopaxFiles) {
				parts.add(new FilePart(f.getName(), f));
			}
		} else if(biopaxUrl != null) {
        	parts.add(new StringPart("url", biopaxUrl));
        } else {
        	log.error("Nothing to do (no BioPAX data specified)!");
        	return;
        }
        
        PostMethod post = new PostMethod(url);
        post.setRequestEntity(
        		new MultipartRequestEntity(
        				parts.toArray(new Part[]{}), post.getParams()
        			)
        		);
        int status = httpClient.executeMethod(post);
		
        if(log.isInfoEnabled()) log.info("HTTP Status Text>>>" 
				+ HttpStatus.getStatusText(status));
		
		BufferedReader res = new BufferedReader(
				new InputStreamReader(post.getResponseBodyAsStream())
			);
		String line;
		PrintWriter writer = new PrintWriter(out);
		while((line = res.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		res.close();
		post.releaseConnection();
    }
    
    public void setUrl(String url) {
		this.url = url;
	}
    
    public String getUrl() {
		return url;
	}
    
    /**
     * 
     * 
     * @param xml
     * @return
     * @throws JAXBException
     */
    public static ValidatorResponse unmarshal(String xml) throws JAXBException {
    	JAXBContext jaxbContext = JAXBContext.newInstance("org.biopax.validator.jaxb");
		Unmarshaller un = jaxbContext.createUnmarshaller();
		Source src = new StreamSource(new StringReader(xml));
		ValidatorResponse resp = un.unmarshal(src, ValidatorResponse.class).getValue();
		return resp;
    }
    
}