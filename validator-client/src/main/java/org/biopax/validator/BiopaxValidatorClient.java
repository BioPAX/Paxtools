package org.biopax.validator;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple BioPAX Validator client 
 * to upload and check BioPAX OWL files.
 * 
 * @author rodch
 *
 */
public class BiopaxValidatorClient {
	private static final Log log = LogFactory.getLog(BiopaxValidatorClient.class);
	
	/**
	 * Default BioPAX Validator's URL
	 */
	public static final String 
		DEFAULT_VALIDATOR_URL = "http://www.biopax.org/biopax-validator/validator/checkFile.html";
	
	private static HttpClient httpClient = new HttpClient();
	private String url;
    private boolean asHtml;
    
    /**
     * Main Constructor
     * 
     * It configures for the validator's URL
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * and result format ().
     * 
     * @param url - validator's file-upload form address
     * @param asHtml - return HTML or XML result (true/false)
     */
    public BiopaxValidatorClient(String url, boolean asHtml) {
		this.url = (url != null) ? url : DEFAULT_VALIDATOR_URL;
		this.asHtml = asHtml;
	}
    
    /**
     * The Second Constructor
     * 
     * It configures for the default validator
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * to return XML result.
     */
    public BiopaxValidatorClient(String url) {
    	this(url, false);
	}
    
    /**
     * Default Constructor
     * 
     * It configures for the default validator
     * (defined by DEFAULT_VALIDATOR_URL constant)
     * to return XML result.
     */
    public BiopaxValidatorClient() {
    	this(null);
	}
       
    /**
     * Checks several BioPAX OWL files using the 
     * remote BioPAX validator and prints the 
     * results.
     * 
     * @param biopaxFiles files (File[]) to check  
     * @param out validation results output stream
     * @throws IOException
     */
    public void validate(File[] biopaxFiles, OutputStream out) throws IOException {
        Collection<Part> parts = new HashSet<Part>();
        
        // set result type either to HTML or XML
        StringPart resultTypePart = (asHtml == true) 
        	? new StringPart("retDesired", "html") 
        	: new StringPart("retDesired", "xml");
        parts.add(resultTypePart);
        
        // add files to check
        for(File f : biopaxFiles) {
        	parts.add(new FilePart(f.getName(), f));
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

    public void setAsHtml(boolean asHtml) {
		this.asHtml = asHtml;
	}
    
    public boolean isAsHtml() {
		return asHtml;
	}
}