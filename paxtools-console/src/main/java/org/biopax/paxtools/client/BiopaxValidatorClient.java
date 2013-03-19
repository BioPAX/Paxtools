package org.biopax.paxtools.client;

/*
 * #%L
 * BioPAX Validator Client
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.validator.jaxb.Behavior;
import org.biopax.validator.jaxb.ValidatorResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;

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
     * @param profile validation profile name
     * @param retFormat xml, html, or owl (no errors, just modified owl, if autofix=true)
     * @param biopaxUrl check the BioPAX at the URL
     * @param biopaxFiles an array of BioPAX files to validate
     * @param out
     * @throws IOException
     */
    public void validate(boolean autofix, String profile, RetFormat retFormat, Behavior filterBy,
    		Integer maxErrs, String biopaxUrl, File[] biopaxFiles, OutputStream out) throws IOException 
    {
        Collection<Part> parts = new HashSet<Part>();
        
        if(autofix) {
        	parts.add(new StringPart("autofix", "true"));
        }
        
        //TODO add extra options (normalizer.fixDisplayName, normalizer.inferPropertyOrganism, normalizer.inferPropertyDataSource, normalizer.xmlBase)?
              
        if(profile != null && !profile.isEmpty()) {
        	parts.add(new StringPart("profile", profile));
        }
        
        // set result type
		if (retFormat != null) {
			parts.add(new StringPart("retDesired", retFormat.toString().toLowerCase()));
		}
        
		if(filterBy != null) {
			parts.add(new StringPart("filter", filterBy.toString()));
		}
		
		if(maxErrs != null && maxErrs > 0) {
			parts.add(new StringPart("maxErrors", maxErrs.toString()));
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
		
        log.info("HTTP Status Text>>>" + HttpStatus.getStatusText(status));
		
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