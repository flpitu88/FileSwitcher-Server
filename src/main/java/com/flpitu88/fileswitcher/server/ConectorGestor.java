package com.flpitu88.fileswitcher.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Properties;

import com.flpitu88.fileSwitcher.colecciones.ListaPaths;
import com.flpitu88.fileSwitcher.colecciones.ReprArchivo;

import com.flpitu88.fileSwitcher.utilitarios.Archivo;
import com.flpitu88.fileswitcher.gestorpaths.MenuGestor;
import com.flpitu88.fileswitcher.gestorpaths.PathGuardar;
import com.flpitu88.fileswitcher.gestorpaths.PathUsuario;

public class ConectorGestor {
	
	// Lista de usuarios registrados
    private LinkedList<PathUsuario> datosUsers = new LinkedList<PathUsuario>();
	// Lista de paths que se guardan
    private LinkedList<PathGuardar> datosPaths = new LinkedList<PathGuardar>();
    // Configuracion con listado de Paths
    private Properties configuracion;
    // Path generico de nuevos usuarios
    private static String pathGenerico = Server.obtenerConfiguracion().getProperty("pathGenerico");
    private String pathUsuarios = null;
	
    //Getters y Setters
    public LinkedList<PathUsuario> getDatosUsers() {
		return datosUsers;
	}
    
	public void setDatosUsers(LinkedList<PathUsuario> datosUsers) {
		this.datosUsers = datosUsers;
	}
	
	public LinkedList<PathGuardar> getDatosPaths() {
		return datosPaths;
	}
	
	public void setDatosPaths(LinkedList<PathGuardar> datosPaths) {
		this.datosPaths = datosPaths;
	}
	
	public Properties getConfiguracion() {
		return configuracion;
	}
	
	public void setConfiguracion(Properties configuracion) {
		this.configuracion = configuracion;
	}
	
	// Constructor de Clase
 	public ConectorGestor(){
 	// Cargo la configuracion del archivo
        this.configuracion = MenuGestor.obtenerConfiguracion();
        this.pathUsuarios = this.getConfiguracion().getProperty("usersGuardado");
        String pathDirs = this.getConfiguracion().getProperty("archGuardado");
        this.cargarListaUsers(pathUsuarios);
        this.cargarListaPaths(pathDirs);
 	}
 	
 	@SuppressWarnings("unchecked")
	public synchronized void cargarListaUsers(String path){
    	try {
    		Archivo archCargar = new Archivo(path);
    		if (archCargar.existe()){
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
				this.setDatosUsers((LinkedList<PathUsuario>) ois.readObject());
				ois.close();
    		} else {
    			this.setDatosUsers(new LinkedList<PathUsuario>());
    		}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
 	
 	@SuppressWarnings("unchecked")
	public synchronized void cargarListaPaths(String path){
    	try {
    		Archivo archCargar = new Archivo(path);
    		if (archCargar.existe()){
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
				this.setDatosPaths((LinkedList<PathGuardar>) ois.readObject());
				ois.close();
    		} else {
    			this.setDatosPaths(new LinkedList<PathGuardar>());
    		}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
 	
 	/*
	 * Metodo que reemplaza las barras invertidas por las barras
	 * normales utilizadas por el sistema en los paths, para poder
	 * realizar las consultas a la base de la misma manera en la que se
	 * guardan los datos
	 */
	public String cambiarBarraInvertida(String cadena){
        char[] tmp = cadena.toCharArray();
        for (int i=0;i<cadena.length();i++){
            if (cadena.charAt(i) == '\\'){
                tmp[i] = '/';
            }
        }
        String resul = new String(tmp);
        return resul;
    }
 	
 	
 	//METODOS DE CONSULTAS A LAS LISTAS
 	
	//Armo una lista con todos los paths a persistir
	public ListaPaths consultaListadoPaths(){
		ListaPaths lista = new ListaPaths();
		for (int i=0;i<this.getDatosPaths().size();i++){
			String pathArreglado1 = this.cambiarBarraInvertida(this.getDatosPaths().get(i).getPathPropio());
	    	String pathArreglado2 = this.cambiarBarraInvertida(this.getDatosPaths().get(i).getPathComun());
	    	ReprArchivo reprArch = new ReprArchivo(pathArreglado1,pathArreglado2,0);
	        lista.addPath(reprArch);
		}
		return lista;
	}
	
	//Consulta de la ubicacion del usuario en la lista
	public int consultaIdUser(String user){
		int resul = -1;
		for (int i=0;i<this.getDatosUsers().size();i++){
			if (this.getDatosUsers().get(i).getUsuario().equals(user)){
				resul = i;
			}
		}
		return resul;
	}
	
	//Metodo que chequea si existe el usuario
	public boolean existeUsuario(String user){
		boolean resul = false;
		for (int i=0;i<this.getDatosUsers().size();i++){
			if (this.getDatosUsers().get(i).getUsuario().equals(user)){
				resul = true;
			}
		}
		return resul;
	}
	
	//Metodo que devuelve el path donde se guardan los datos del usuario 
	public String obtenerPathDeUsuarioEnServer(String user){
		String path = "";
		for (int i=0;i<this.getDatosUsers().size();i++){
			if (this.getDatosUsers().get(i).getUsuario().equals(user)){
				path = this.getDatosUsers().get(i).getPathGuardar();
			}
		}
		return path;
	}

	// Metodo que devuelve el path de donde guardar los datos de clientes
	public String getPathEnCliente(String pathServer){
		String pathEnCliente = "";
		String pathABuscar = cambiarBarraInvertida(pathServer);
		for (int i=0;i<this.getDatosPaths().size();i++){
			String pathArreglado1 = this.cambiarBarraInvertida(this.getDatosPaths().get(i).getPathPropio());
	    	String pathArreglado2 = this.cambiarBarraInvertida(this.getDatosPaths().get(i).getPathComun());
	    	if (pathABuscar.equals(pathArreglado2)){
	    		pathEnCliente = pathArreglado1;
	    		return pathEnCliente;
	    	}
		}
		return pathEnCliente;
	}
	
	// Metodo para agregar un usuario nuevo a la lista
	public boolean registrarNuevoUsuario(String user){
		boolean resul = false;
		PathUsuario nuevoUser = new PathUsuario(user,pathGenerico+user);
		this.getDatosUsers().add(nuevoUser);
		Archivo carpeta = new Archivo(pathGenerico+user);
		carpeta.crearCarpeta();
		this.guardarUsuarios();
		resul = true;
		return resul;
	}
	
	// Metodo para guardar la lista en archivo despues de agregar un nuevo usuario
	public void guardarUsuarios(){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.pathUsuarios));
			oos.writeObject(datosUsers);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
