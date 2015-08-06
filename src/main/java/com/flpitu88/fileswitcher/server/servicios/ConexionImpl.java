/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import java.net.Socket;

/**
 *
 * @author flpitu88
 */
public class ConexionImpl implements Conexion {

    private Socket sock;

    public ConexionImpl(Socket sock) {
        this.sock = sock;
    }

    @Override
    public String getIpConexion() {
        return sock.getInetAddress().toString();
    }

    @Override
    public Socket getSocket() {
        return sock;
    }

}
