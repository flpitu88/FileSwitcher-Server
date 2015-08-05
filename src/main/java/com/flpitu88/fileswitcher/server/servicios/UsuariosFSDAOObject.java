/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

import com.flpitu88.fileswitcher.gestorpaths.PathUsuario;
import java.util.List;

/**
 *
 * @author flavio
 */
public class UsuariosFSDAOObject implements UsuariosFSDAO {

    private List<PathUsuario> usuarios;
    
    @Override
    public void iniciarUsuariosDAO(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean existeUsuario(String usuario) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void guardarUsuarios() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
