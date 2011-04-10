/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.spigot.plugin.email;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Yi Du
 */
public class EmailDataType {//implements SocialNetwork{
    public static final String FILTER_EMAIL_ADDRESS_FROM = "email address from";
    public static final String FILTER_EMAIL_ADDRESS_TO = "email address to";
    public static final String FILTER_EMAIL_ADDRESS_CC = "email address cc";
    public static final String FILTER_EMAIL_ADDRESS_BCC = "email address bcc";

    public static final String FILTER_DATERANGE_AFTER = "date after";
    public static final String FILTER_DATERANGE_BEFORE = "date before";
    public static final String FILTER_ATTACHMENT = "att";
    public static final String FILTER_CC = "cc";
    public static final String FILTER_BCC = "bcc";
    public static final String FILTER_SUBJECT = "subject";
    public static final String FILTER_message = "message";

    public static final char SPLIT_CHAR = '|';
    public static final String DATEFORMAT = "yyyy-MM-dd";

    public static final String SERVER_TYPE_POP3 = "POP3";//TODO connect to panel
    public static final String SERVER_TYPE_IMAP = "IMAP";
    
//    private String name;
    private boolean hasFilter;
    private boolean hasCcAsWeight;
    private boolean hasBccAsWeight;
    private boolean useOneNodeIfSameDisplayName;
    private boolean isFromLocalFile;//true means from localfile;false means from server
    private String fileFilterType;//selected file type of file filter
    private HashMap<String, String> filterProperty = new HashMap<String, String>();

    //options used when from mail server are as below
    private String serverType;
    private String serverURL;
    private String userName;
    private String userPsw;
    private boolean useSSL;
    private int port;
    private File[] files;//not null if receive from local file

    public EmailDataType(){
        
    }
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getName() {
//        return name;
//    }

    public boolean hasFilter() {
        return hasFilter;
    }

    public void setFilter(boolean filter) {
        this.hasFilter = filter;
    }

    public void setFilterProperty(String filter, String property) {
        filterProperty.put(filter, property);
    }

    public String getFilterProperty(String filter) {
        return filterProperty.get(filter);
    }

    public String getServerType(){
        return this.serverType;
    }

    public void setServerType(String serverType){
        if(serverType == null)
            return;
        this.serverType = serverType;
//        if(serverType.equals(SERVER_TYPE_POP3))
//            this.serverType = SERVER_TYPE_POP3;
//        if(serverType.equals(SERVER_TYPE_IMAP))
//            this.serverType = SERVER_TYPE_POP3;
    }

    public String getServerURL(){
        return this.serverURL;
    }

    public void setServerURL(String serverURL){
        this.serverURL = serverURL;
    }

    public String getUserName(){
        return this.userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getUserPsw(){
        return this.userPsw;
    }

    public void setUserPsw(String userPsw){
        this.userPsw = userPsw;
    }

    public boolean hasCcAsWeight(){
        return hasCcAsWeight;
    }

    public void setCcAsWeight(boolean hasCcAsWeight){
        this.hasCcAsWeight = hasCcAsWeight;
    }

    public boolean hasBccAsWeight(){
        return hasBccAsWeight;
    }

    public void setBccAsWeight(boolean hasBccAsWeight){
        this.hasBccAsWeight = hasBccAsWeight;
    }

    public HashMap<String, String> getFilter(){
        return this.filterProperty;
    }

    public boolean isFromLocalFile(){
        return isFromLocalFile;
    }

    public void setFromLocalFile(boolean flag){
        isFromLocalFile = flag;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] filePath) {
        this.files = filePath;
    }

    public String getFileFilterType() {
        return fileFilterType;
    }

    public void setFileFilterType(String fileFilterType) {
        this.fileFilterType = fileFilterType;
    }

    public boolean isUseOneNodeIfSameDisplayName() {
        return useOneNodeIfSameDisplayName;
    }

    public void setUseOneNodeIfSameDisplayName(boolean useOneNodeIfSameDisplayName) {
        this.useOneNodeIfSameDisplayName = useOneNodeIfSameDisplayName;
    }
    
    /**
     * copy options from "from" to "to"
     * @param from
     * @param to 
     */
    public static void makeACopy(EmailDataType from, EmailDataType to) {
        to.setFromLocalFile(from.isFromLocalFile());
        to.setFilter(from.hasFilter());
        if(from.isFromLocalFile()){
            to.setFileFilterType(from.getFileFilterType());
            //we don't copy files selected from jfiledialog
        }
        else{
            to.setUserName(from.getUserName());
            to.setUserPsw(from.getUserPsw());
            to.setPort(from.getPort());
            to.setServerType(from.getServerType());
            to.setUseSSL(from.isUseSSL());
            to.setServerURL(from.getServerURL());
        }
        if(from.hasFilter()){
            HashMap<String, String> temp = from.getFilter();
            for(String s :temp.keySet()){
                to.setFilterProperty(s, temp.get(s));
            }
        }
        to.setBccAsWeight(from.hasBccAsWeight());
        to.setCcAsWeight(from.hasCcAsWeight());
        to.setUseOneNodeIfSameDisplayName(from.isUseOneNodeIfSameDisplayName());

    }
}
