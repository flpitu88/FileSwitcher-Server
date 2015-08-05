/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import com.flpitu88.fileSwitcher.colecciones.ListaPaths;

/**
 *
 * @author flpitu88
 */
public interface ItemsGuardarDAO {
    
    public void iniciarItemsDAO(String path);
    
    public ListaPaths consultaListadoItems();
    
    public String obtenerPathUsuario(String usuario);
    
    public String obtenerPathCliente(String cliente);
    
    public void guardarItemsDAO();
    
}
