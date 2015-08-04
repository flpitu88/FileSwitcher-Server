/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import com.flpitu88.fileswitcher.gestorpaths.PathGuardar;
import com.flpitu88.fileswitcher.gestorpaths.PathUsuario;
import java.util.LinkedList;
import java.util.Properties;

/**
 *
 * @author flpitu88
 */
public class PathsDAOStatic implements PathsDAO {

    private Properties configuracion;
    private LinkedList<PathUsuario> datosUsers = null;
    private LinkedList<PathGuardar> datosPaths = null;

    public PathsDAOStatic(LinkedList<PathUsuario> datosUsers, LinkedList<PathGuardar> datosPaths) {
        this.datosUsers = datosUsers;
        this.datosPaths = datosPaths;
    }

    public PathsDAOStatic() {
    }

    @Override
    public void cargarListaPaths(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cargarListaUsuarios(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
