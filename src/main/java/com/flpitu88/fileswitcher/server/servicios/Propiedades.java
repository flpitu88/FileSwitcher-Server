/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author flavio
 */
public class Propiedades {

    private Properties config;

    public Propiedades() throws IOException {
        this.config = cargarProperties();
    }

    private Properties cargarProperties() throws FileNotFoundException, IOException {
        Properties conf = new Properties();
        conf.load(new FileInputStream("serverConfig.properties"));
        return conf;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public String getDirDeLog() {
        return config.getProperty("logServ");
    }

    public int getPuertoServidor() {
        return Integer.parseInt(config.getProperty("serverPort"));
    }

    public boolean guardadoInicial() {
        boolean resul = false;
        String guarIni = config.getProperty(
                "guardadoInicial");
        if (guarIni.equalsIgnoreCase("si")) {
            resul = true;
        } else {
            resul = false;
        }
        return resul;
    }

    public int getMaximoTamTransfer() {
        return Integer.parseInt(config.getProperty("maxTotalSize"));
    }

}
