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

    public Propiedades() {
        this.config = cargarProperties();
    }

    private Properties cargarProperties() {
        Properties conf = new Properties();
        try {
            // Cargo el archivo en la ruta especificada
            conf.load(new FileInputStream("serverConfig.properties"));
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

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

}
