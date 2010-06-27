/**
 *
 * Copyright 2010, Nicolas Leroux.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * User: nicolas
 * Date: Feb 25, 2010
 *
 */
package play.modules.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import play.Logger;
import play.Play;
import play.Play.Mode;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Server {


    public Server() {
        final Properties p = Play.configuration;
        int httpPort = Integer.parseInt(p.getProperty("http.port", "9000"));
        InetAddress address = null;
        if (System.getProperties().containsKey("http.port")) {
            httpPort = Integer.parseInt(System.getProperty("http.port"));
        }
        try {
            if (p.getProperty("http.address") != null) {
                address = InetAddress.getByName(p.getProperty("http.address"));
            }
            if (System.getProperties().containsKey("http.address")) {
                address = InetAddress.getByName(System.getProperty("http.address"));
            }
        } catch (Exception e) {
            Logger.error(e, "Could not understand http.address");
            System.exit(-1);
        }

        // Setup the http server for netty
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
        bootstrap.bind(new InetSocketAddress(address, httpPort));
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        try {
            if (Play.mode == Mode.DEV) {
                if (address == null) {
                    Logger.info("Listening for HTTP on port %s (Waiting a first request to start) ...", httpPort);
                } else {
                    Logger.info("Listening for HTTP at %2$s:%1$s (Waiting a first request to start) ...", httpPort, address);
                }
            } else {
                if (address == null) {
                    Logger.info("Listening for HTTP on port %s ...", httpPort);
                } else {
                    Logger.info("Listening for HTTP at %2$s:%1$s  ...", httpPort, address);
                }
            }
        } catch (ChannelException e) {
            Logger.error("Could not bind on port " + httpPort, e);
            System.exit(-1);
        }


    }


    public static void main(String[] args) throws Exception {
        File root = new File(System.getProperty("application.path"));
        Play.init(root, System.getProperty("play.id", ""));
        if (System.getProperty("precompile") == null) {
            new Server();
        } else {
            Logger.info("Done.");
        }
    }

}