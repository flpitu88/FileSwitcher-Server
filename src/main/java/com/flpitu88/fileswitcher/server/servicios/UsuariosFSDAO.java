/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flpitu88.fileswitcher.server.servicios;

/**
 *
 * @author flavio
 */
public interface UsuariosFSDAO {
    
    public void iniciarUsuariosDAO(String path);
    
    public boolean existeUsuario(String usuario);
    
    public void guardarUsuarios();
}
