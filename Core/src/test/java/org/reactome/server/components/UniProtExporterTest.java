package org.reactome.server.components;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.components.exporter.UniProtExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-config-test.xml")
public class UniProtExporterTest {
    @Autowired
    private UniProtExporter uniProtExporter;

    @Test
    public void testAlgo(){

    }
}
