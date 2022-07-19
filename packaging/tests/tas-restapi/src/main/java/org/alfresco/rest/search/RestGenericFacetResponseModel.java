package org.alfresco.rest.search;

import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated by 'gethin' on '2017-03-17 12:44' from 'Alfresco Search REST API' swagger file 
 * Generated from 'Alfresco Search REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/search/versions/1}
 */
public class RestGenericFacetResponseModel extends TestModel implements IRestModel<RestGenericFacetResponseModel>
{
    @Override
    public ModelAssertion<RestGenericFacetResponseModel> assertThat()
    {
        return new ModelAssertion<RestGenericFacetResponseModel>(this);
    }

    @Override
    public ModelAssertion<RestGenericFacetResponseModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestGenericFacetResponseModel model;

    @Override
    public RestGenericFacetResponseModel onModel()
    {
        return model;
    }

    /**
    The facet type, eg. interval, range, field, query1
    */	        

    private String type;	    
    /**
    The field name or its explicit label, if provided on the request
    */	        

    private String label;	    
    /**
    An array of buckets and values
    */	        

    private List<RestGenericBucketModel> buckets;	    

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }				

    public String getLabel()
    {
        return this.label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }				

    public List<RestGenericBucketModel> getBuckets()
    {
        return this.buckets;
    }

    public void setBuckets(List<RestGenericBucketModel> buckets)
    {
        this.buckets = buckets;
    }				
}
 