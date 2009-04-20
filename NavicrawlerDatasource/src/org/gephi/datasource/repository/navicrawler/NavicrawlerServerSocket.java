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
package org.gephi.datasource.repository.navicrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.gephi.importer.api.ImportContainer;
import org.gephi.importer.api.ImportController;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class NavicrawlerServerSocket {

    static final boolean DEBUG = false;
    private ServerSocket serverSocket;
    private boolean running = true;
    private ExecutorService executor;

    public NavicrawlerServerSocket(int port) {
        try {
            executor = Executors.newSingleThreadExecutor();
            serverSocket = new ServerSocket(port);
            System.out.println("Navicrawler Server Started on " + port);

        } catch (IOException e) {
            error(e);
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            error(e);
        }
    }

    public void run() throws IOException {
        while (running) {
            Socket socket = serverSocket.accept();
            try {
                executor.execute(new SocketThread(socket));
            } catch (IOException e) {
                socket.close();
            }
        }
        serverSocket.close();
    }

    public void dataPushed(ImportContainer importContainer) {
    }

    public void error(Exception e) {
    }

    class SocketThread implements Runnable {

        private Socket socket;

        public SocketThread(Socket s) throws IOException {
            socket = s;
        }

        public void run() {
            try {
                if (DEBUG) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (reader.ready()) {
                        System.out.println(reader.readLine());
                    }
                } else {
                    System.out.println("Data received from socket");
                    ImportController importController = Lookup.getDefault().lookup(ImportController.class);
                    
                }

                socket.close();
            } catch (Exception e) {
                error(e);
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
