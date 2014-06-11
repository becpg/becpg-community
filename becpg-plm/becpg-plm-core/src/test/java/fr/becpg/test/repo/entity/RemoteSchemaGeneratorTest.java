/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Resource;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import fr.becpg.repo.entity.remote.RemoteSchemaGenerator;
import fr.becpg.test.PLMBaseTestCase;

/**
 * @author matthieu
 */
public class RemoteSchemaGeneratorTest extends PLMBaseTestCase {

	@Resource
	private RemoteSchemaGenerator remoteSchemaGenerator;
	


	@Test
	public void testGenerateSchema() throws XMLStreamException, IOException, SAXException  {
		
		File tempFile = File.createTempFile("schema", "xsd");
		
		remoteSchemaGenerator.generateSchema(new FileOutputStream(tempFile));
		
		
		org.springframework.core.io.Resource res = new ClassPathResource("beCPG/remote/entity_excel.xml");
        SchemaFactory factory = 
            SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        javax.xml.validation.Schema schema = factory.newSchema(new StreamSource(new FileInputStream(tempFile)));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(res.getInputStream()));
        
        
	}
}
