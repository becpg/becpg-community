/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Resource;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import fr.becpg.repo.entity.remote.RemoteSchemaGenerator;
import fr.becpg.test.PLMBaseTestCase;

/**
 * @author matthieu
 */
public class RemoteSchemaGeneratorTest extends PLMBaseTestCase {

	@Resource
	private RemoteSchemaGenerator remoteSchemaGenerator;
	


	@Test
	public void testGenerateSchema() throws XMLStreamException, IOException  {
		
		File tempFile = File.createTempFile("schema", "xsd");
		
		remoteSchemaGenerator.generateSchema(new FileOutputStream(tempFile));
	}
}
