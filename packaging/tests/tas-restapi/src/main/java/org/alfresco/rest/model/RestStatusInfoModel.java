package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generated by 'tsalvado' on '2021-08-18 21:15' from 'Alfresco Content Services REST API' swagger file 
 * Generated from 'Alfresco Content Services REST API' swagger file
 * Base Path {@linkplain /alfresco/api}
 */
public class RestStatusInfoModel extends TestModel implements IRestModel<RestStatusInfoModel>
{
    @Override
    public ModelAssertion<RestStatusInfoModel> assertThat()
    {
        return new ModelAssertion<RestStatusInfoModel>(this);
    }

    @Override
    public ModelAssertion<RestStatusInfoModel> and()
    {
        return assertThat();
    }

    @JsonProperty(value = "entry")
    RestStatusInfoModel model;

    @Override
    public RestStatusInfoModel onModel()
    {
        return model;
    }


    @JsonProperty(required = true)    
    private boolean isReadOnly;	    

    @JsonProperty(required = true)    
    private boolean isAuditEnabled;	    

    @JsonProperty(required = true)    
    private boolean isQuickShareEnabled;	    

    @JsonProperty(required = true)    
    private boolean isThumbnailGenerationEnabled;	    

    @JsonProperty(required = true)    
    private boolean isDirectAccessUrlEnabled;	    

    public boolean getIsReadOnly()
    {
        return this.isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }				

    public boolean getIsAuditEnabled()
    {
        return this.isAuditEnabled;
    }

    public void setIsAuditEnabled(boolean isAuditEnabled)
    {
        this.isAuditEnabled = isAuditEnabled;
    }				

    public boolean getIsQuickShareEnabled()
    {
        return this.isQuickShareEnabled;
    }

    public void setIsQuickShareEnabled(boolean isQuickShareEnabled)
    {
        this.isQuickShareEnabled = isQuickShareEnabled;
    }				

    public boolean getIsThumbnailGenerationEnabled()
    {
        return this.isThumbnailGenerationEnabled;
    }

    public void setIsThumbnailGenerationEnabled(boolean isThumbnailGenerationEnabled)
    {
        this.isThumbnailGenerationEnabled = isThumbnailGenerationEnabled;
    }				

    public boolean getIsDirectAccessUrlEnabled()
    {
        return this.isDirectAccessUrlEnabled;
    }

    public void setIsDirectAccessUrlEnabled(boolean isDirectAccessUrlEnabled)
    {
        this.isDirectAccessUrlEnabled = isDirectAccessUrlEnabled;
    }				
}
 