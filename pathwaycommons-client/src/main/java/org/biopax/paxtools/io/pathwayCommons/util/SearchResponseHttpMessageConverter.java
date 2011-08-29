package org.biopax.paxtools.io.pathwayCommons.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.biopax.paxtools.io.pathwayCommons.model.*;
import org.biopax.paxtools.io.pathwayCommons.model.Error;
import org.biopax.paxtools.model.BioPAXElement;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Query the PC2 Web API find() output and parse the response XML:
 * http://www.pathwaycommons.org/pc2-demo/resources/schemas/SearchResponse.txt
 *
 * @see org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client
 * @see SearchResponse
 *
 */
public class SearchResponseHttpMessageConverter implements HttpMessageConverter<SearchResponse> {
    private ArrayList<MediaType> mediaList = null;

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return SearchResponse.class.equals(clazz);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        if(mediaList == null) {
            mediaList = new ArrayList<MediaType>();
            mediaList.add(MediaType.APPLICATION_XHTML_XML);
            mediaList.add(MediaType.APPLICATION_XML);
            mediaList.add(MediaType.TEXT_HTML);
            mediaList.add(MediaType.TEXT_XML);
            mediaList.add(MediaType.TEXT_PLAIN);
        }

        return mediaList;
    }

    @Override
    public SearchResponse read(Class<? extends SearchResponse> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        XStream xStream = new XStream();
        xStream.alias("searchResponseType", SearchResponse.class);
        xStream.alias("error", Error.class);
        xStream.alias("searchHit", SearchHit.class);
        xStream.alias("biopaxClass", Class.class);
        xStream.addImplicitCollection(SearchResponse.class, "searchHits", "searchHit", SearchHit.class);
        xStream.addImplicitCollection(SearchHit.class, "names", "name", String.class);
        xStream.addImplicitCollection(SearchHit.class, "organisms", "organism", String.class);
        xStream.addImplicitCollection(SearchHit.class, "dataSources", "dataSource", String.class);
        xStream.addImplicitCollection(SearchHit.class, "pathways", "pathway", String.class);
        xStream.addImplicitCollection(SearchHit.class, "excerpts", "excerpt", String.class);
        xStream.addImplicitCollection(SearchHit.class, "actualHitUris", "actualHitUri", String.class);

        // This is to convert BioPAX class filter value into the actual BioPAX class
        // See http://www.pathwaycommons.org/pc2-demo/#valid_biopax_parameter
        xStream.registerConverter(new SingleValueConverter() {
            @Override
            public String toString(Object o) {
                return o.toString();
            }

            @Override
            public Object fromString(String s) {
                try {
                    return Class.forName("org.biopax.paxtools.model.level3." + s);
                } catch (ClassNotFoundException e) {
                    return BioPAXElement.class;
                }
            }

            @Override
            public boolean canConvert(Class aClass) {
                return aClass.equals(Class.class);
            }
        });


        return (SearchResponse) xStream.fromXML(inputMessage.getBody());
    }

    @Override
    public void write(SearchResponse searchResponse, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        throw new UnsupportedOperationException("Not supported!");
    }
}
