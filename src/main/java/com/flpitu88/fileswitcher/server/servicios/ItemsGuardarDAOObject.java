/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import com.flpitu88.fileswitcher.colecciones.ListaPaths;
import com.flpitu88.fileswitcher.gestorpaths.PathGuardar;
import java.util.List;

/**
 *
 * @author flpitu88
 */
public class ItemsGuardarDAOObject implements ItemsGuardarDAO {

    private List<PathGuardar> items;

    @Override
    public void iniciarItemsDAO(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListaPaths consultaListadoItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String obtenerPathUsuario(String usuario) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String obtenerPathCliente(String cliente) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void guardarItemsDAO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
