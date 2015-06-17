/*
Copyright (C) 2010-2015 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
*/
package fr.becpg.test.repo.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.action.VersionCleanerActionConfig;

/**
 * @author matthieu
 *
 */

public class VersionCleanerActionConfigTest {

	private static final Log logger = LogFactory.getLog(VersionCleanerActionConfig.class);
	
	
	private Collection<Version> getAllVersions(){
		List<Version> versions = new LinkedList<>();
		
		versions.add(createVersion(VersionType.MINOR, "6.2",0));
		versions.add(createVersion(VersionType.MINOR, "6.1",0));
		versions.add(createVersion(VersionType.MAJOR, "6.0",0));
		versions.add(createVersion(VersionType.MAJOR, "5.0",0));
		versions.add(createVersion(VersionType.MAJOR, "4.0",0));
		versions.add(createVersion(VersionType.MINOR, "3.4",1));
		versions.add(createVersion(VersionType.MINOR, "3.3",1));
		versions.add(createVersion(VersionType.MINOR, "3.2",1));
		versions.add(createVersion(VersionType.MINOR, "3.1",1));
		versions.add(createVersion(VersionType.MAJOR, "3.0",1));
		versions.add(createVersion(VersionType.MAJOR, "2.0",2));
//		for(int i=150;i>0;i--){
//			versions.add(createVersion(VersionType.MINOR, "1."+i,2));
//		}
		versions.add(createVersion(VersionType.MAJOR, "1.0",2));
		
		
		
		return versions;
	}
	
	
	@SuppressWarnings("serial")
	private Version createVersion(final VersionType versionType, final String label,final int day) {

		return new Version() {
			
			@Override
			public NodeRef getVersionedNodeRef() {
				return null;
			}
			
			@Override
			public VersionType getVersionType() {
				return versionType;
			}
			
			@Override
			public Serializable getVersionProperty(String name) {
				return null;
			}
			
			@Override
			public Map<String, Serializable> getVersionProperties() {
				return null;
			}
			
			@Override
			public String getVersionLabel() {
				return label;
			}
			
			@Override
			public NodeRef getFrozenStateNodeRef() {
				return null;
			}
			
			@Override
			public String getFrozenModifier() {
				return null;
			}
			
			@Override
			public Date getFrozenModifiedDate() {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -day);
				
				return calendar.getTime();
			}
			
			@Override
			public String getDescription() {
				return null;
			}
			
			@Override
			public String getCreator() {
				return null;
			}
			
			@Override
			public Date getCreatedDate() {
				return null;
			}
			
			public String toString() {
				return getVersionLabel();
			}
		};
	}


	@Test
	public void testVersionConfig() {
		
		VersionCleanerActionConfig versionConfig = new VersionCleanerActionConfig();
		
		Collection<Version> versions = getAllVersions();
		
		logVersions(versions);
		
		Collection<Version> toDelete = versionConfig.versionsToDelete(versions);
		
		
		Assert.assertEquals(0,toDelete.size());
		
		
//        versionConfig.setConfig("minor", 2, null, null);
//		
//		toDelete = versionConfig.versionsToDelete(versions);
//		
//		logger.info("Delete for config :"+versionConfig);
//		logVersions(toDelete);
//		
//		Assert.assertEquals(7,toDelete.size());
		
		versionConfig.setConfig("all", 3, null, null);
		
		toDelete = versionConfig.versionsToDelete(versions);
		
		logger.info("Delete for config :"+versionConfig);
		logVersions(toDelete);
		
		Assert.assertEquals(7,toDelete.size());
		
		versionConfig.setConfig("major", 4, null, null);
	    versionConfig.setConfig("minor", 2, null, null);
		
		toDelete = versionConfig.versionsToDelete(versions);
		
		logger.info("Delete for config :"+versionConfig);
		logVersions(toDelete);
		
		Assert.assertEquals(4,toDelete.size());
		
		versionConfig.setConfig("all", 5, 2, null);
		
		toDelete = versionConfig.versionsToDelete(versions);
		logger.info("Delete for config :"+versionConfig);
		logVersions(toDelete);
		
		Assert.assertEquals(2,toDelete.size());
		
		versionConfig.setConfig("all", 5, null, null);
		
		toDelete = versionConfig.versionsToDelete(versions);
		logger.info("Delete for config :"+versionConfig);
		logVersions(toDelete);
		
		Assert.assertEquals(1,toDelete.size());
		
		
	    versionConfig.setConfig("all", 5, 2, 1);
		
		toDelete = versionConfig.versionsToDelete(versions);
		logger.info("Delete for config :"+versionConfig);
		logVersions(toDelete);
		
		Assert.assertEquals(8,toDelete.size());
		
		versionConfig.setConfig("major", null, null, null);
			
		toDelete = versionConfig.versionsToDelete(versions);
		logger.info("Delete for config :"+versionConfig);
		logVersions(toDelete);
			
		Assert.assertEquals(4,toDelete.size());
		
	}


	/**
	 * @param versions
	 */
	private void logVersions(Collection<Version> versions) {
		for(Version version : versions) {
			logger.info((version.getVersionType().equals(VersionType.MINOR)?" +-- ":" | ")+version.getVersionLabel()+" ("+version.getFrozenModifiedDate()+")");
		}
		
	}
	
	
}
