package com.flpitu88.fileswitcher.server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import com.flpitu88.fileswitcher.utilitarios.Archivo;
import com.flpitu88.fileswitcher.utilitarios.Logueo;

/**
 *
 * @author Flavio L. Pietrolati
 */
public class Server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties config = obtenerConfiguracion();
        if (config != null) {
            System.out.println("Cargada la configuracion del server");
        } else {
            System.out.println("No encontre el archivo");
            System.exit(0);
        }

        ServerSocket ssocket = null;
        Socket socket = null;
        // Configuro el logger
        Logueo logger;
        Logueo logAt;

        try {
            String dirLog = Server.obtenerConfiguracion()
                    .getProperty("logServ");
            Archivo archDirLog = new Archivo(dirLog);
            if (!archDirLog.existe()) {
                archDirLog.crearDirectorios();
            }
            String pathServer = Server.obtenerConfiguracion().getProperty(
                    "logServ")
                    + "LogServer.log";
            logger = new Logueo(pathServer, "ServerLog");

            // Instancio logueo para la atencion de los clientes
            String pathAtCli = Server.obtenerConfiguracion().getProperty(
                    "logServ")
                    + "LogAtClien.log";
            logAt = new Logueo(pathAtCli, "AtencionClienteLog");

            // Instancio el ConectorGestor hecho en aplicacion
            ConectorGestor conector = new ConectorGestor();

            int puerto = Integer.parseInt(config.getProperty("serverPort"));
            ssocket = new ServerSocket(puerto);

            logger.logArchivo("Creado el socket de escucha");

            while (true) {
                logger.logAmbos("Esperando conexion de cliente");
                socket = ssocket.accept();
                logger.logAmbos("Conectado con cliente de IP: "
                        + socket.getInetAddress());
                // Lanzo hilo con el socket y la conexion a la base de datos
                new AtencionCliente(socket, conector, logAt);
                logger.logArchivo("---     ---     ---- Lanzo hilo de atencion a cliente ----     --- ---");
            }
        } catch (UnknownHostException ex) {
            System.err.println("UnknownHostException Exception main");
        } catch (IOException ex) {
            System.err.println("IOException Exception main");
            ex.printStackTrace();
        } catch (SecurityException e) {
            System.err.println("SecurityException Exception main");
        }
    }

    public static Properties obtenerConfiguracion() {
        Properties config = new Properties();
        try {
            // Cargo el archivo en la ruta especificada
            config.load(new FileInputStream("serverConfig.properties"));
        } catch (FileNotFoundException e) {
            System.err.println("Error, El archivo de configuracion no existe");
            System.exit(0);
        } catch (IOException e) {
            System.err
                    .println("Error, No se puede leer el archivo de configuracion");
            System.exit(0);
        }
        return config;
    }

}
