/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import com.flpitu88.fileswitcher.server.view.Vista;
import com.flpitu88.fileswitcher.server.view.VistaConsola;
import com.flpitu88.fileswitcher.utilitarios.Logueo;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author flpitu88
 */
public class AtencionCliente extends Thread {

    private Vista vista;
    private ServicioItemsyUsuarios srvItems;
    private String usuario;
    private Logueo logger;
    private ServicioTransmision srvTrans;
    private boolean guardInicial;
//      Tamanio maximo de los archivos a transferir
//	final int size_limit;
//	private ObjectOutputStream oosEnvio;
//	private ObjectInputStream oisRecep;
//	private boolean guardIni = false;
    private Conexion conec;

    public AtencionCliente(Conexion conex, Propiedades prop, Logueo logger) {

        // Seteo la conexion
        this.conec = conex;
        
        // Instancio un objeto de vista (consola)
        this.vista = new VistaConsola();

        // Seteo si esta habilitado el guardado inicial de archivos
        this.guardInicial = prop.guardadoInicial();

        // Instancio el servicio de transmision de archivos
        this.srvTrans = new ServicioTransmision();

        // Obtengo el tamanio maximo de transmision de archivos        
        int maxSizeMB = prop.getMaximoTamTransfer();
        int maxSizeArch = maxSizeMB * 1048576;
        logger.logArchivo("El tamanio maximo de transferencia es (total y por archivo)"
                + maxSizeMB + " Mbytes");
        vista.imprimirStatus("Tamanio maximo de transferencia: " + maxSizeMB);
        this.srvTrans.setTamMaximo(maxSizeArch);

        try {
            // Seteo el maximo tamanio de transmision de archivos
            this.srvTrans.inicializarStreams(conec.getSocket());
        } catch (IOException ex) {
            Logger.getLogger(AtencionCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        this.logger = logger;
        start();
    }
}
