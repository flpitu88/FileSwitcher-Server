/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.aplicacion;

import com.flpitu88.fileswitcher.server.servicios.AtencionCliente;
import com.flpitu88.fileswitcher.server.servicios.Conexion;
import com.flpitu88.fileswitcher.server.servicios.ConexionImpl;
import com.flpitu88.fileswitcher.server.servicios.Propiedades;
import com.flpitu88.fileswitcher.server.servicios.ServicioItemsyUsuarios;
import com.flpitu88.fileswitcher.server.view.Vista;
import com.flpitu88.fileswitcher.server.view.VistaConsola;
import com.flpitu88.fileswitcher.utilitarios.Archivo;
import com.flpitu88.fileswitcher.utilitarios.Logueo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author flpitu88
 */
public class ServerApp {

    private ServicioItemsyUsuarios srvItUs;
    private Vista visual;
    private Logueo logger;
    private Propiedades propiedades;
    private ServerSocket ssocket;

    private void main(String[] args) {
        iniciar();
    }

    private void iniciar() {
        try {
            propiedades = new Propiedades();
            visual = new VistaConsola();

            // Obtengo path de donde se creara el archivo de log del servidor
            Archivo archDirLog = new Archivo(propiedades.getDirDeLog());
            if (!archDirLog.existe()) {
                archDirLog.crearDirectorios();
            }

            // Instancio el logger para el Servidor
            String pathServer = propiedades.getDirDeLog() + "LogServer.log";
            logger = new Logueo(pathServer, "ServerLog");

            // Instancio el logger para la Atencion de los clientes
            String pathAtCli = propiedades.getDirDeLog() + "LogAtClien.log";
            Logueo logAt = new Logueo(pathAtCli, "AtencionClienteLog");

            // Obtengo el puerto y lanzo el Servidor que atiende conexiones
            ssocket = new ServerSocket(propiedades.getPuertoServidor());
            logger.logArchivo("Socket de escucha iniciado");
            visual.imprimirInfo("Esperando conexiones ....");

            while (true) {
                Conexion conex = new ConexionImpl(ssocket.accept());
                logger.logArchivo("Conectado con cliente de IP: " + conex.getIpConexion());
                visual.imprimirInfo("Conectado cliente desde IP: " + conex.getIpConexion());
                // Lanzo hilo de atencion al nuevo cliente
                new AtencionCliente(conex, propiedades, logAt);
                logger.logArchivo("Se lanza hilo de atencion al cliente");
            }

        } catch (IOException ex) {
            System.err.println("Error al abrir el archivo de configuracion");
        }

//        Properties config = obtenerConfiguracion();
//        if (config != null) {
//            System.out.println("Cargada la configuracion del server");
//        } else {
//            System.out.println("No encontre el archivo");
//            System.exit(0);
//        }
//
//        ServerSocket ssocket = null;
//        Socket socket = null;
//        // Configuro el logger
//        Logueo logger;
//        Logueo logAt;
//
//        try {
//            String dirLog = Server.obtenerConfiguracion()
//                    .getProperty("logServ");
//            Archivo archDirLog = new Archivo(dirLog);
//            if (!archDirLog.existe()) {
//                archDirLog.crearDirectorios();
//            }
//            String pathServer = Server.obtenerConfiguracion().getProperty(
//                    "logServ")
//                    + "LogServer.log";
//            logger = new Logueo(pathServer, "ServerLog");
//
//            // Instancio logueo para la atencion de los clientes
//            String pathAtCli = Server.obtenerConfiguracion().getProperty(
//                    "logServ")
//                    + "LogAtClien.log";
//            logAt = new Logueo(pathAtCli, "AtencionClienteLog");
//
//            // Instancio el ConectorGestor hecho en aplicacion
//            ConectorGestor conector = new ConectorGestor();
//
//            int puerto = Integer.parseInt(config.getProperty("serverPort"));
//            ssocket = new ServerSocket(puerto);
//
//            logger.logArchivo("Creado el socket de escucha");
//
//            while (true) {
//                logger.logAmbos("Esperando conexion de cliente");
//                socket = ssocket.accept();
//                logger.logAmbos("Conectado con cliente de IP: "
//                        + socket.getInetAddress());
//                // Lanzo hilo con el socket y la conexion a la base de datos
//                new AtencionCliente(socket, conector, logAt);
//                logger.logArchivo("---     ---     ---- Lanzo hilo de atencion a cliente ----     --- ---");
//            }
//        } catch (UnknownHostException ex) {
//            System.err.println("UnknownHostException Exception main");
//        } catch (IOException ex) {
//            System.err.println("IOException Exception main");
//            ex.printStackTrace();
//        } catch (SecurityException e) {
//            System.err.println("SecurityException Exception main");
//        }
    }

}
