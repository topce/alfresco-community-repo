/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.security.permissions.dynamic.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.dynamic.CircabcDAO;
import org.alfresco.repo.security.permissions.dynamic.CircabcPermission;
import org.mybatis.spring.SqlSessionTemplate;

public class CircabcDAOImpl implements CircabcDAO {

  private static final String SELECT_GROUP_PERMISSION =
    "circabc.select_GroupPermission";

  private SqlSessionTemplate template;

  public final void setSqlSessionTemplate(
    SqlSessionTemplate sqlSessionTemplate
  ) {
    this.template = sqlSessionTemplate;
  }

  @Override
  public List<CircabcPermission> getGroupPerrmission(
    String groupNodeRef,
    String userName
  ) {
    Map<String, Object> params = new HashMap<String, Object>(1);

    params.put("nodeRef", groupNodeRef);

    params.put("userName", userName);

    return template.selectOne(SELECT_GROUP_PERMISSION, params);
  }
}
