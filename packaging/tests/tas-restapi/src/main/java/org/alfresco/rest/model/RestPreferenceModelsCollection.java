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

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestPreferenceModel> { "list": { "pagination": { "count": 2, "hasMoreItems": false, "totalItems": 2, "skipCount": 0, "maxItems": 100 }, "entries": [ { "entry": { "id": "org.alfresco.ext.sites.favourites.site-lwdxYDQFIi.createdAt", "value": "2016-09-30T16:31:05.085Z" } }, { "entry": { "id": "org.alfresco.share.sites.favourites.site-lwdxYDQFIi", "value": true } } ] } }
 *
 * @author Cristina Axinte
 */
public class RestPreferenceModelsCollection extends RestModels<RestPreferenceModel, RestPreferenceModelsCollection>
{}
