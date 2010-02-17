/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.branding.desktop.reporter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
public class ReportControllerTest {

    public ReportControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSend() {
        ReportController reportController = new ReportController();
        Report report = new Report();
        report.setUserDescription("test & more < >");
        Document doc = reportController.buildReportDocument(report);
        reportController.sendDocument(doc);
    }

}