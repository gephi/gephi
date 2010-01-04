/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.branding.desktop.reporter;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
public class Report {

    private Date date;
    private Throwable throwable;
    private String summary = "";
    private String userDescription = "";
    private String userEmail = "";
    //IDELog
    private String log = "";
    //Version
    private String version = "";
    //Screen
    private Dimension screenSize;
    private int screenDevices;
    //Arch
    private int numberOfProcessors;
    private String os = "";
    //Memory
    private String heapMemoryUsage = "";
    private String nonHeapMemoryUsage = "";
    //Java
    private String vm = "";
    //OpenGL
    private String glVendor = "";
    private String glRenderer = "";
    private String glVersion = "";
    //Modules
    private List<String> enabledModules = new ArrayList<String>();
    private List<String> disabledModules = new ArrayList<String>();

    public Report() {
        Calendar cal = Calendar.getInstance();
        date = cal.getTime();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public int getNumberOfProcessors() {
        return numberOfProcessors;
    }

    public void setNumberOfProcessors(int numberOfProcessors) {
        this.numberOfProcessors = numberOfProcessors;
    }

    public int getScreenDevices() {
        return screenDevices;
    }

    public void setScreenDevices(int screenDevices) {
        this.screenDevices = screenDevices;
    }

    public Dimension getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(Dimension screenSize) {
        this.screenSize = screenSize;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public void setHeapMemoryUsage(String heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public String getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public void setNonHeapMemoryUsage(String nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    public String getVm() {
        return vm;
    }

    public void setVm(String vm) {
        this.vm = vm;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGlRenderer() {
        return glRenderer;
    }

    public void setGlRenderer(String glRenderer) {
        this.glRenderer = glRenderer;
    }

    public String getGlVendor() {
        return glVendor;
    }

    public void setGlVendor(String glVendor) {
        this.glVendor = glVendor;
    }

    public String getGlVersion() {
        return glVersion;
    }

    public void setGlVersion(String glVersion) {
        this.glVersion = glVersion;
    }

    public void addEnabledModule(String str) {
        enabledModules.add(str);
    }

    public void addDisabledModule(String str) {
        disabledModules.add(str);
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Element writeXml(Document document) {
        Element reportE = document.createElement("report");
        reportE.setAttribute("version", "0.7");

        //Date
        Element dateE = document.createElement("date");

        //LastModifiedDate
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateE.setTextContent(sdf.format(date));
        dateE.appendChild(document.createComment("yyyy-MM-dd HH:mm:ss"));
        reportE.appendChild(dateE);

        //Exceptions
        Element exceptionsE = document.createElement("exceptions");
        {
            Element exceptionE = document.createElement("exception");

            //Summary
            Element titleE = document.createElement("title");
            titleE.setTextContent(summary);
            exceptionE.appendChild(titleE);

            //Throwable
            Element throwableE = document.createElement("throwable");
            if (throwable != null) {
                StringWriter wr = new StringWriter();
                throwable.printStackTrace(new PrintWriter(wr, true));
                throwableE.setTextContent(wr.toString());
            }
            exceptionE.appendChild(throwableE);

            exceptionsE.appendChild(exceptionE);
        }
        reportE.appendChild(exceptionsE);

        //User description
        Element userDescriptionE = document.createElement("description");
        userDescriptionE.setTextContent(userDescription);
        reportE.appendChild(userDescriptionE);

        //User email
        Element userEmailE = document.createElement("email");
        userEmailE.setTextContent(userEmail);
        reportE.appendChild(userEmailE);

        //Version
        Element versionE = document.createElement("version");
        versionE.setTextContent(version);
        reportE.appendChild(versionE);

        //VM
        Element vmE = document.createElement("vm");
        vmE.setTextContent(vm);
        reportE.appendChild(vmE);

        //Os
        Element osE = document.createElement("os");
        osE.setTextContent(os);
        reportE.appendChild(osE);

        //CPU
        Element cpuE = document.createElement("cpucount");
        cpuE.setTextContent(String.valueOf(numberOfProcessors));
        reportE.appendChild(cpuE);

        //GL
        Element glVendorE = document.createElement("glVendor");
        glVendorE.setTextContent(glVendor);
        reportE.appendChild(glVendorE);
        Element glRendererE = document.createElement("glRenderer");
        glRendererE.setTextContent(glRenderer);
        reportE.appendChild(glRendererE);
        Element glVersionE = document.createElement("glVersion");
        glVersionE.setTextContent(glVersion);
        reportE.appendChild(glVersionE);

        //Heap
        Element heapE = document.createElement("heapmemory");
        heapE.setTextContent(heapMemoryUsage);
        reportE.appendChild(heapE);

        //NonHeap
        Element nonHeapE = document.createElement("nonheapmemory");
        nonHeapE.setTextContent(nonHeapMemoryUsage);
        reportE.appendChild(nonHeapE);

        //Screen size
        Element screenSizeE = document.createElement("screensize");
        screenSizeE.setTextContent("width=" + screenSize.width + " height=" + screenSize.height);
        reportE.appendChild(screenSizeE);

        //Screen devices
        Element devicesE = document.createElement("screendevices");
        devicesE.setTextContent(String.valueOf(screenDevices));
        reportE.appendChild(devicesE);

        //Modules
        Element modulesE = document.createElement("modules");
        for (String m : enabledModules) {
            Element enabledModuleE = document.createElement("enabledmodule");
            enabledModuleE.setTextContent(m);
            modulesE.appendChild(enabledModuleE);
        }
        for (String m : disabledModules) {
            Element disabledModuleE = document.createElement("disabledmodule");
            disabledModuleE.setTextContent(m);
            modulesE.appendChild(disabledModuleE);
        }
        reportE.appendChild(modulesE);

        //Log
        Element logE = document.createElement("log");
        logE.setTextContent(log);
        reportE.appendChild(logE);

        document.appendChild(reportE);

        return reportE;
    }
}
