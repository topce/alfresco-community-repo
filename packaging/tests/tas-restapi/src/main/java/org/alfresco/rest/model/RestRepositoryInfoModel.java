/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * Generated by 'aforascu' on '2018-01-10 16:02' from 'Alfresco Content Services REST API' swagger file Generated from 'Alfresco Content Services REST API' swagger file Base Path {@linkplain /alfresco/api}
 */
public class RestRepositoryInfoModel extends TestModel implements IRestModel<RestRepositoryInfoModel>
{
    @JsonProperty(value = "entry")
    RestRepositoryInfoModel model;

    @Override
    public RestRepositoryInfoModel onModel()
    {
        return model;
    }

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String edition;

    @JsonProperty(required = true)
    private RestVersionInfoModel version;

    @JsonProperty(required = true)
    private RestStatusInfoModel status;

    private RestLicenseInfoModel license;

    private List<RestModuleInfoModel> modules;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getEdition()
    {
        return this.edition;
    }

    public void setEdition(String edition)
    {
        this.edition = edition;
    }

    public RestVersionInfoModel getVersion()
    {
        return this.version;
    }

    public void setVersion(RestVersionInfoModel version)
    {
        this.version = version;
    }

    public RestStatusInfoModel getStatus()
    {
        return this.status;
    }

    public void setStatus(RestStatusInfoModel status)
    {
        this.status = status;
    }

    public RestLicenseInfoModel getLicense()
    {
        return this.license;
    }

    public void setLicense(RestLicenseInfoModel license)
    {
        this.license = license;
    }

    public List<RestModuleInfoModel> getModules()
    {
        return this.modules;
    }

    public void setModules(List<RestModuleInfoModel> modules)
    {
        this.modules = modules;
    }
}
