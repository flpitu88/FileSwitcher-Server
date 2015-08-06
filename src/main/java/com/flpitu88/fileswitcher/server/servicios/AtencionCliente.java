/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import com.flpitu88.fileSwitcher.mensajes.MensajeChequeoSocket;
import com.flpitu88.fileSwitcher.mensajes.MensajeNuevoUsuario;
import com.flpitu88.fileSwitcher.mensajes.MensajePedidoGuardar;
import com.flpitu88.fileSwitcher.mensajes.MensajePedidoRecuperar;
import com.flpitu88.fileSwitcher.mensajes.MensajePresentacion;
import com.flpitu88.fileSwitcher.utilitarios.Logueo;
import com.flpitu88.fileswitcher.server.view.Vista;
import com.flpitu88.fileswitcher.server.view.VistaConsola;
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

        // Seteo el logger
        this.logger = logger;

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
        logger.logArchivo("El tamanio maximo de transferencia es (total y por archivo) "
                + maxSizeMB + " Mbytes");
        vista.imprimirStatus("Tamanio maximo de transferencia: " + maxSizeMB);
        this.srvTrans.setTamMaximo(maxSizeArch);

        try {
            // Seteo el maximo tamanio de transmision de archivos
            this.srvTrans.inicializarStreams(conec.getSocket());
            vista.imprimirInfo("Servicio de transmision de archivos con cliente iniciado");
            start();
        } catch (IOException ex) {
            // Falla la creacion de los streams
            logger.logArchivo("Fallo la creacion de los streams, el hilo de atencion no se lanza");
        }
    }

    @Override
    public void run() {
        try {
            Object mensajeAux = srvTrans.recibirMje();

            //RECIBO MENSAJE DE PRESENTACION DE USUARIO
            if (mensajeAux instanceof MensajePresentacion) {
                MensajePresentacion mensaje = (MensajePresentacion) mensajeAux;
                logger.logArchivo("Recibido mensaje de presentacion usuario: "
                        + mensaje.nombreUsuario);
                vista.imprimirStatus("Usuario " + mensaje.nombreUsuario
                        + " conectado desde IP " + conec.getIpConexion());
                // Chequeo la existencia del usuario, si no existe lo registro
                if (srvItems.existeUsuario(mensaje.nombreUsuario)) {
                    logger.logArchivo("El usuario ya se encuentra registrado");
                } else {
                    logger.logArchivo("Usuario sin registrarse. Se procede a registrarlo");
                    srvItems.registrarUsuario(mensaje.nombreUsuario);
                }

                // Armo mensaje de respuesta y lo envio
                MensajePresentacion mjeRespuesta = new MensajePresentacion();
                this.establecerUsuarioActivo(mensaje.nombreUsuario);
                mjeRespuesta.nombreUsuario = usuarioActivo();
                mjeRespuesta.guardIni = (this.guardInicial) ? 1 : 0;
                srvTrans.enviarMje(mjeRespuesta);

            } else // RECIBO UN MENSAJE QUE SOLICITA EL ENVIO DE ARCHIVOS AL CLIENTE
            if (mensajeAux instanceof MensajePedidoRecuperar) {
                MensajePedidoRecuperar mensaje = (MensajePedidoRecuperar) mensajeAux;
//                logger.logAmbosAtCli(this.socket, "El usuario " + mensaje.getNombreUsuario()
//                        + " solicita recuperar sus archivos");
//                this.setUsuario(mensaje.getNombreUsuario());
//                recuperacionArchivos(mensaje.getNombreUsuario());
            } else // RECIBO UN MENSAJE DE CLIENTE QUE SOLICITA GUARDAR ARCHIVOS
            if (mensajeAux instanceof MensajePedidoGuardar) {
                MensajePedidoGuardar mensaje = (MensajePedidoGuardar) mensajeAux;
//                logger.logAmbosAtCli(this.socket, "El usuario " + mensaje.getUsuario()
//                        + " solicita guardar sus archivos");
//                this.setUsuario(mensaje.getUsuario());
//                guardarArchivosDeUsuario();
            } else // Si recibo mensaje de nuevo usuario - REGISTRAR USUARIO
            if (mensajeAux instanceof MensajeNuevoUsuario) {
                MensajeNuevoUsuario mensaje = (MensajeNuevoUsuario) mensajeAux;
//                logger.logAmbosAtCli(this.socket, "Recibido mensaje de registrar usuario");
//                registrarUsuario(mensaje.getUsuario());
            } else if (mensajeAux instanceof MensajeChequeoSocket) {
//                logger.logArchivoAtCli(this.socket, "Recibido mensaje para chequear estado del socket");
            }
            if (!(mensajeAux instanceof MensajeChequeoSocket)) {
//                logger.logAmbosAtCli(this.socket, "---------- Se cierra el hilo de solicitud de cliente -------------");
            }

        } catch (IOException ex) {
            Logger.getLogger(AtencionCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void establecerUsuarioActivo(String nombre) {
        this.usuario = nombre;
    }

    public String usuarioActivo() {
        return this.usuario;
    }

}
