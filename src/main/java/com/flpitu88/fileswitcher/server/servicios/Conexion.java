/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import java.net.Socket;

/**
 *
 * @author flavio
 */
public interface Conexion {

    public String getIpConexion();
    
    public Socket getSocket();

}
