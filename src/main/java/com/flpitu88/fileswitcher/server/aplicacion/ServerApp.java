/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.aplicacion;

import com.flpitu88.fileSwitcher.utilitarios.Logueo;
import com.flpitu88.fileswitcher.server.servicios.Propiedades;
import com.flpitu88.fileswitcher.server.servicios.ServicioItemsyUsuarios;
import com.flpitu88.fileswitcher.server.view.Vista;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

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
        propiedades = new Propiedades();
        
        if (propiedades.getConfig() != null){
            
        } else {
            System.err.println("No existe archivo de configuracion");
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
