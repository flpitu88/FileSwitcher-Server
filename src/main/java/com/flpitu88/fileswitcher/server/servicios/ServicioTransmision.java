/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author flavio
 */
public class ServicioTransmision {

    private int tamMaximo;
    private ObjectOutputStream oosEnvio;
    private ObjectInputStream oisRecep;

    public int getSize_limit() {
        return tamMaximo;
    }

    public void setTamMaximo(int size_limit) {
        this.tamMaximo = size_limit;
    }

    public void inicializarStreams(Socket socket) throws IOException {
        this.oosEnvio = new ObjectOutputStream(socket.getOutputStream());
        this.oisRecep = new ObjectInputStream(socket.getInputStream());
    }
}
