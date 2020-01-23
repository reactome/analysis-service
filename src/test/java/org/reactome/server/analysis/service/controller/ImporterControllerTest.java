package org.reactome.server.analysis.service.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactome.server.analysis.AppTests;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/resources/mvc-dispatcher-servlet-test.xml"})
@WebAppConfiguration
public class ImporterControllerTest extends AppTests {

    @Test
    public void getPostText() {

        // /
    }

    @Test
    public void getPostFile() {
    }

    @Test
    public void getPostURL() {
    }
}