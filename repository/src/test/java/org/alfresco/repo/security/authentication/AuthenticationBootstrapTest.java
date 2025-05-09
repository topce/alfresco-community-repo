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
package org.alfresco.repo.security.authentication;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;

/**
 * Checks that no residual authentications are left over after bootstrap. It is important that this test run on its own and not part of a suite.
 * 
 * @author Derek Hulley
 * @since 3.0.1
 */
@Category(OwnJVMTestsCategory.class)
public class AuthenticationBootstrapTest extends TestCase
{
    /**
     * Creates the application context in the context of the test (not statically) and checks that no residual authentication is left hanging around.
     */
    public void testBootstrap()
    {
        // Start the context
        ApplicationContextHelper.getApplicationContext();

        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        assertNull(
                "Found user '" + user + "' still authenticated after bootstrap.\n" +
                        "Use AuthenticationUtil.runAs or AuthenticationUtil.pushAuthentication " +
                        "and AuthenticationUtil.popAuthentication to keep the thread clean of unwanted authentication tokens.",
                user);
    }
}
