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
package org.alfresco.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to collect all of our DatabaseMetaData interpretations in one place.
 * 
 * @author sfrensley
 *
 */
public class DatabaseMetaDataHelper
{

    private static Log logger = LogFactory.getLog(DatabaseMetaDataHelper.class);

    /**
     * Tries to determine the schema name from the DatabaseMetaData obtained from the Connection.
     * 
     * @param connection
     *            A database connection
     * @return String
     */
    public String getSchema(Connection connection)
    {
        if (connection == null)
        {
            logger.error("Unable to determine schema due to null connection.");
            return null;
        }

        ResultSet rs = null;
        PreparedStatement stmt = null;

        try
        {
            final DatabaseMetaData dbmd = connection.getMetaData();
            final String userName = dbmd.getUserName();
            String schema = null;

            // Direct query to INFORMATION_SCHEMA to bypass buggy getSchemas() in MySQL Connector/J
            String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next())
            {
                final String thisSchema = rs.getString("SCHEMA_NAME");
                if (thisSchema.equals(userName) || thisSchema.equalsIgnoreCase("dbo"))
                {
                    schema = thisSchema;
                    break;
                }
            }
            return schema;
        }
        catch (Exception e)
        {
            logger.error("Unable to determine current schema using direct query.", e);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {
                    // noop
                }
            }
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (Exception e)
                {
                    // noop
                }
            }
        }
        return null;
    }
}
