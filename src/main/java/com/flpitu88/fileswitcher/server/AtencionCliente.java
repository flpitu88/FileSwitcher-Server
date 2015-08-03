package com.flpitu88.fileswitcher.server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.flpitu88.fileswitcher.colecciones.ListaPaths;
import com.flpitu88.fileswitcher.colecciones.ReprArchivo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.flpitu88.fileswitcher.mensajes.MensajeArchivosGuardar;
import com.flpitu88.fileswitcher.mensajes.MensajeArchivosRecuperar;
import com.flpitu88.fileswitcher.mensajes.MensajeChequeoSocket;
import com.flpitu88.fileswitcher.mensajes.MensajeConfirTransferencia;
import com.flpitu88.fileswitcher.mensajes.MensajeNuevoUsuario;
import com.flpitu88.fileswitcher.mensajes.MensajePedidoGuardar;
import com.flpitu88.fileswitcher.mensajes.MensajePedidoRecuperar;
import com.flpitu88.fileswitcher.mensajes.MensajePresentacion;
import com.flpitu88.fileswitcher.utilitarios.Archivo;
import com.flpitu88.fileswitcher.utilitarios.Logueo;

/**
 * 
 * @author Flavio L. Pietrolati
 */
public class AtencionCliente extends Thread {

	// Tamanio maximo de los archivos a transferir
	final int size_limit;

	// Atributos
	private final ConectorGestor conecBase;
	private String usuario;

	private Logueo logger;
	private ObjectOutputStream oosEnvio;
	private ObjectInputStream oisRecep;
	private Socket socket;
	private boolean guardIni = false;

	// Constructor de clase
	public AtencionCliente(Socket socket, ConectorGestor conecBase, Logueo logger) {
		String guarIni = Server.obtenerConfiguracion().getProperty(
				"guardadoInicial");
		if (guarIni.equalsIgnoreCase("si")) {
			this.guardIni = true;
		} else {
			this.guardIni = false;
		}
		int maxSizeMB = Integer.parseInt(Server.obtenerConfiguracion()
				.getProperty("maxTotalSize"));
		int maxSizeArch = maxSizeMB * 1048576;
		System.out
				.println("El tamanio maximo de transferencia es (total y por archivo)"
						+ maxSizeMB + " Mbytes");
		this.size_limit = maxSizeArch;
		this.conecBase = conecBase;
		this.setSocket(socket);
		try {
			this.oosEnvio = new ObjectOutputStream(socket.getOutputStream());
			this.oisRecep = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("IO Exception Constructor");
			e.printStackTrace();
			System.exit(0);
		}
		this.logger = logger;
		start();
	}

	@Override
	public void run() {

		try {
			// Configuro el logger
//			String pathAtCli = Server.obtenerConfiguracion().getProperty(
//					"logServ")
//					+ "LogAtClien.log";
//			logger = new Logueo(pathAtCli, "AtencionClienteLog");

			logger.logAmbosAtCli(this.socket," --------------- Inicio hilo de atencion cliente ----------------");

			// Recibo mensaje del cliente
			Object mensajeAux = this.oisRecep.readObject();
			// Si recibis un mensaje de presentacion, chequear clave - LOGUEAR
			if (mensajeAux instanceof MensajePresentacion) {
				MensajePresentacion mensaje = (MensajePresentacion) mensajeAux;
				logger.logAmbosAtCli(this.socket,"Recibido mensaje de presentacion usuario: "
						+ mensaje.nombreUsuario);
				// Envio respuesta de chequeo de clave
				MensajePresentacion mjeRespuesta = new MensajePresentacion();
				boolean usuarioOK = this.conecBase
						.existeUsuario(mensaje.nombreUsuario);
				this.setUsuario(mensaje.nombreUsuario);
				mjeRespuesta.nombreUsuario = this.usuario;
				if (usuarioOK) {
					logger.logAmbosAtCli(this.socket,"Chequeado usuario existente");
				} else {
					logger.logAmbosAtCli(this.socket,"Usuario inexistente. Se registra en base");
					registrarUsuario(this.usuario);
				}
				if (this.guardIni == true) {
					mjeRespuesta.guardIni = 1;
				} else {
					mjeRespuesta.guardIni = 0;
				}
				this.oosEnvio.writeObject(mjeRespuesta);
				// this.oosEnvio.flush(); // VER SI LO SACO

			} else // Si recibis un mensaje de pedido de archivos - RECUPERAR
			if (mensajeAux instanceof MensajePedidoRecuperar) {
				MensajePedidoRecuperar mensaje = (MensajePedidoRecuperar) mensajeAux;
				logger.logAmbosAtCli(this.socket,"El usuario " + mensaje.getNombreUsuario()
						+ " solicita recuperar sus archivos");
				this.setUsuario(mensaje.getNombreUsuario());
				recuperacionArchivos(mensaje.getNombreUsuario());
			} else // Si recibis un mensaje de guardar archivos - GUARDAR
			if (mensajeAux instanceof MensajePedidoGuardar) {
				MensajePedidoGuardar mensaje = (MensajePedidoGuardar) mensajeAux;
				logger.logAmbosAtCli(this.socket,"El usuario " + mensaje.getUsuario()
						+ " solicita guardar sus archivos");
				this.setUsuario(mensaje.getUsuario());
				guardarArchivosDeUsuario();
			} else // Si recibo mensaje de nuevo usuario - REGISTRAR USUARIO
			if (mensajeAux instanceof MensajeNuevoUsuario) {
				MensajeNuevoUsuario mensaje = (MensajeNuevoUsuario) mensajeAux;
				logger.logAmbosAtCli(this.socket,"Recibido mensaje de registrar usuario");
				registrarUsuario(mensaje.getUsuario());
			} else if (mensajeAux instanceof MensajeChequeoSocket) {
				logger.logArchivoAtCli(this.socket,"Recibido mensaje para chequear estado del socket");
			}
			if (!(mensajeAux instanceof MensajeChequeoSocket)){
				logger.logAmbosAtCli(this.socket,"---------- Se cierra el hilo de solicitud de cliente -------------");	
			}
		} catch (SocketException e) {
			logger.logAmbosAtCli(this.socket,"Se desconecta el cliente " + this.usuario);
			logger.logAmbosAtCli(this.socket,"Esperando conexion de cliente");
		} catch (IOException e) {
			System.err.println("IO Exception run");
			System.exit(0);
		} catch (ClassNotFoundException e) {
			System.err.println("ClassNotFound Exception run");
			System.exit(0);
		}
	}

	// ############################### COMUNES ###############################

	/*
	 * Metodo para registrar un nuevo usuario en la base de datos
	 */
	public void registrarUsuario(String usuario) {
		try {
			boolean resul = this.conecBase.registrarNuevoUsuario(usuario);
			MensajeNuevoUsuario mensajeRespuesta = new MensajeNuevoUsuario(
					usuario);
			mensajeRespuesta.setResul(resul);
			if (resul) {
				mensajeRespuesta.setResul(true);
				mensajeRespuesta.setMensaje("Usuario creado correctamente");
			} else {
				mensajeRespuesta
						.setMensaje("Fallo registro de usuario. Se corta atencion a cliente");
			}
			// Envio respuesta con el resultado del registro
			this.oosEnvio.writeObject(mensajeRespuesta);
			// this.oosEnvio.flush(); // VER SI LO SACO
		} catch (IOException e) {
			System.err.println("IO Exception recibiendoRegUsuario");
		}
	}

	/*
	 * Seteo el usuario activo del hilo
	 */
	public void setUsuario(String user) {
		this.usuario = user;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	// ########################### FIN COMUNES ##############################

	// ############################### GUARDADO DE ARCHIVOS
	// ###############################

	/*
	 * Metodo para guardar los archivos que modifico el usuario
	 */
	public void guardarArchivosDeUsuario() {
		try {
			synchronized (this) {
				logger.logAmbosAtCli(this.socket," ##############        INICIO PROCESO DE GUARDADO DE ARCHIVOS       ############### ");
				// Obtengo la lista de los paths en los que el cliente debe
				// revisar cambios
				ListaPaths lista = this.conecBase.consultaListadoPaths();
				// Envio el listado de paths al cliente
				MensajeArchivosGuardar mjeGuardar = new MensajeArchivosGuardar(
						lista);
				mjeGuardar.setSizeMax(this.size_limit);
				this.oosEnvio.writeObject(mjeGuardar);
				// this.oosEnvio.flush(); // VER SI LO SACO
				logger.logArchivoAtCli(this.socket,"Envio lista de paths con archivos en servidor");
				// Espero recepcion de todos los paths con sus fechas de
				// modificacion
				Object mensajeAux = this.oisRecep.readObject();
				if (mensajeAux instanceof MensajeArchivosGuardar) {
					logger.logArchivoAtCli(this.socket,"Recibo lista de paths con fechas de modificacion");
					MensajeArchivosGuardar mjeGuardarConFecha = (MensajeArchivosGuardar) mensajeAux;
					ListaPaths listaDepurada = depurarListaArchivosActualizados(mjeGuardarConFecha
							.getListado());
					logger.logArchivoAtCli(this.socket,"Lista de archivos modificados filtrada por cliente");
					// limito la cantidad de archivos de la lista que se deben
					// transferir por exceder el tamaÃ±o
					listaDepurada.limitarTamanio(this.size_limit);
					logger.logArchivoAtCli(this.socket,listaDepurada.getCantSinTransferir()
							+ " archivos no se transfieren por exceder maximo. ("
							+ listaDepurada.getTamTotalSinTransferir()
							+ " bytes)");
					// Envio al cliente la lista solo con archivos a enviar
					MensajeArchivosGuardar mjeSoloGuardar = new MensajeArchivosGuardar(
							listaDepurada);
					this.oosEnvio.writeObject(mjeSoloGuardar);
					this.oosEnvio.flush(); // VER SI LO SACO
					logger.logArchivoAtCli(this.socket,"Envio mensaje con la lista depurada con "
							+ listaDepurada.getTamanio() + " elementos");
					String pathEnServer = this.conecBase
							.obtenerPathDeUsuarioEnServer(this.usuario);
					// Voy recibiendo archivos y guardando en la direccion de su
					// carpeta
					if ((listaDepurada.getTamanio() > 0)
							&& (listaDepurada.getCantSinTransferir() < listaDepurada
									.getTamanio())) {
						logger.logAmbosAtCli(this.socket,"Inicio la recepcion de archivos a guardar");
						for (int i = 0; i < listaDepurada.getTamanio(); i++) {
							recibirArchivosAGuardar(pathEnServer);
						}
						int cantArchivos = listaDepurada.getTamanio();
						long cantByEnviados = listaDepurada.getTamanioTotal() / 1024;
						;
						logger.logAmbosAtCli(this.socket,"Recibidos " + cantArchivos
								+ " archivos (" + cantByEnviados
								+ "kb) - Usuario " + this.usuario);
					} else {
						logger.logAmbosAtCli(this.socket,"No hay archivos modificados, no se inicia transferencia");
					}
				}
				logger.logAmbosAtCli(this.socket," ##############        FIN DE GUARDADO DE ARCHIVOS       ############### ");
			}

		} catch (IOException e) {
			logger.logArchivoAtCli(this.socket,"IO Exception envListaPathsGuardar");
			logger.logAmbosAtCli(this.socket,"Error en el guardado de archivos. Se cierra");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.logArchivoAtCli(this.socket,"ClassNotFoundException Exception recibir mjeGuardarLista");
			logger.logAmbosAtCli(this.socket,"Error en el guardado de archivos. Se cierra");
		}
	}

	/*
	 * Metodo que toma todos los paths recibidos por el cliente, y verifica en
	 * cada uno si es mas nuevo o mas viejo. De estar actualizado el del
	 * servidor, no lo agrega a una nueva lista solo con los viejos.
	 */
	public ListaPaths depurarListaArchivosActualizados(ListaPaths lista) {
		// path inicial que reemplaza las rutas de las carpetas de cliente
		String pathIni = this.conecBase
				.obtenerPathDeUsuarioEnServer(this.usuario);
		ListaPaths listaRetorno = new ListaPaths();
		logger.logArchivoAtCli(this.socket,"Path de usuario en server: "
				+ this.cambiarBarraInvertida(pathIni));
		for (int i = 0; i < lista.getTamanio(); i++) {
			ReprArchivo datosFichero = lista.getPath(i);
			logger.logArchivoAtCli(this.socket,"Nuevo path en server: "
					+ this.cambiarBarraInvertida(pathIni
							+ datosFichero.getPathFin()));
			Archivo fichero = new Archivo(pathIni + datosFichero.getPathFin());
			// logger.logArchivoAtCli(this.socket,"Fecha en cliente: " +
			// datosFichero.getfUltMod() + " - Fecha en server: " +
			// fichero.ultimaModif());
			if (estaActualizado(fichero.ultimaModif(),
					datosFichero.getfUltMod())) {
				logger.logArchivoAtCli(this.socket,this.cambiarBarraInvertida(datosFichero
						.getPathFin()) + " actualizado. No se lista");
			} else {
				logger.logArchivoAtCli(this.socket,this.cambiarBarraInvertida(datosFichero
						.getPathFin()) + " desactualizado. Se lista");
				// Debo cambiar el nombre del archivo para poder recibir
				fichero.modificarNombreTemporal();
				datosFichero.setPathIniCli(datosFichero.getPathIni());
				datosFichero.setPathIni(this.cambiarBarraInvertida(pathIni
						+ datosFichero.getPathFin()));
				listaRetorno.addPath(datosFichero);
				// logger.logArchivoAtCli(this.socket,"Agrego a lista de retorno");
			}
		}
		return listaRetorno;
	}

	/*
	 * Metodo que reemplaza las barras invertidas por las barras normales
	 * utilizadas por el sistema en los paths, para poder realizar las consultas
	 * a la base de la misma manera en la que se guardan los datos
	 */
	public String cambiarBarraInvertida(String cadena) {
		char[] tmp = cadena.toCharArray();
		for (int i = 0; i < cadena.length(); i++) {
			if (cadena.charAt(i) == '\\') {
				tmp[i] = '/';
			}
		}
		String resul = new String(tmp);
		return resul;
	}

	/*
	 * Metodo que recibe los archivos del cliente a guardar en el path
	 * correspondiente al mismo.
	 */
	public void recibirArchivosAGuardar(String pathEnServer) {
		try {

			// Nuevo metodo para la recepcion de archivos
			FileOutputStream fos = null;
			Object mensajeAux;
			ReprArchivo repArch;

			mensajeAux = this.oisRecep.readObject();
			if (mensajeAux instanceof ReprArchivo) {
				repArch = (ReprArchivo) mensajeAux;
				int tamArchivo = (int) repArch.getTamanio();
				String pathDestino = repArch.getPathIni();
				logger.logArchivoAtCli(this.socket,"Recibido archivo a guardar en: "
						+ pathDestino);
				logger.logArchivoAtCli(this.socket,"El archivo pesa: " + tamArchivo);
				// Chequeo si existe el directorio contenedor, sino lo creo
				Archivo dirDestino = new Archivo(
						(new Archivo(pathDestino)).getDirPadre());
				if (!dirDestino.existe()) {
					dirDestino.crearDirectorios();
				}
				fos = new FileOutputStream(pathDestino);

				// Creamos el array de bytes para leer los datos del archivo
				byte[] buffer = new byte[tamArchivo];
				logger.logArchivoAtCli(this.socket,"El buffer mide: " + buffer.length);

				// Obtenemos el archivo mediante la lectura de bytes enviados
				for (int i = 0; i < buffer.length; i++) {
					buffer[i] = (byte) this.oisRecep.read();
				}

				// Escribimos el archivo
				fos.write(buffer);

				// Enviar confirmacion de que termino este archivo
				MensajeConfirTransferencia mjeConfir = new MensajeConfirTransferencia();
				mjeConfir.total = tamArchivo;
				logger.logArchivoAtCli(this.socket,"Envio confirmacion de recepcion");
				this.oosEnvio.writeObject(mjeConfir);
				fos.close();

			}
		} catch (IOException e) {
			logger.logArchivoAtCli(this.socket,"IO Exception recibiendoArchivos");
			logger.logConsola("Error en la recepcion de archivo");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.logArchivoAtCli(this.socket,"ClassNotFoundException Exception recibir recibiendoArchivos");
			logger.logConsola("Error en la recepcion de archivo");
		}
	}

	/*
	 * Metodo que chequea si el archivo es mas nuevo que el que se encuentra en
	 * el cliente.
	 */
	public boolean estaActualizado(long fServer, long fCliente) {
		boolean resul = false;
		if (fCliente <= fServer) {
			resul = true;
		}
		return resul;
	}

	// ####################### FIN GUARDADO DE ARCHIVOS
	// ###########################
	// ####################### RECUPERACION DE ARCHIVOS
	// ###########################
	/*
	 * Metodo que gestiona y establece que archivos enviar al cliente conectado.
	 * NUEVA VERSION QUE CHEQUEA FECHAS EN EL CLIENTE
	 */
	public void recuperacionArchivos(String user) {
		try {
			synchronized (this) {
				logger.logAmbosAtCli(this.socket," ##############        INICIO PROCESO DE ENVIO DE ARCHIVOS       ############### ");
				String pathEnServer = this.conecBase
						.obtenerPathDeUsuarioEnServer(user);
				// Clase archivo con el path donde se guardan las carpetas del
				// usuario
				Archivo directorio = new Archivo(pathEnServer);
				logger.logArchivoAtCli(this.socket,"Chequeo el path: "
						+ this.cambiarBarraInvertida(directorio
								.getPathCompleto()));
				ListaPaths listaArchivos = new ListaPaths();
				indexarDirectorios(directorio, listaArchivos, pathEnServer);
				// Creo mensaje para enviar al cliente
				MensajeArchivosRecuperar mjeListado = new MensajeArchivosRecuperar(
						listaArchivos, this.size_limit); // Seteo el maximo como
															// el maximo de toda
															// la transferencia
				this.oosEnvio.writeObject(mjeListado);
				// this.oosEnvio.flush(); // VER SI LO SACO
				logger.logArchivoAtCli(this.socket,"Envio listado de archivos disponibles en servidor");
				// Espero respuesta con la lista filtrada
				Object mjeAux;
				MensajeArchivosRecuperar mjeRespuesta;
				mjeAux = this.oisRecep.readObject();
				// Valores usados para poder loguear el total de archivos y
				// datos enviados
				int cantArchivos = 0;
				long cantByEnviados = 0;
				if (mjeAux instanceof MensajeArchivosRecuperar) {
					mjeRespuesta = (MensajeArchivosRecuperar) mjeAux;
					logger.logArchivoAtCli(this.socket,"Obtengo mensaje con listado filtrado por cliente");
					ListaPaths lista = mjeRespuesta.getListado();
					cantArchivos = lista.getTamanio();
					cantByEnviados = lista.getTamanioTotal();
					// boolean corte = false;
					for (int i = 0; i < lista.getTamanio(); i++) {
						// if (i == lista.getTamanio()-1) { corte = true; }
						// enviarArchivosDeLista(lista.getPath(i),corte);
						transferirArchivo(lista.getPath(i));
					}
				}
				cantByEnviados = cantByEnviados / 1024; // Lo transformo en
														// kBytes
				logger.logAmbosAtCli(this.socket,"Enviados " + cantArchivos + " archivos ("
						+ cantByEnviados + "kb) - Usuario " + this.usuario);
				logger.logAmbosAtCli(this.socket," ##############        FIN DE ENVIO DE ARCHIVOS       ############### ");
			}

		} catch (IOException e) {
			logger.logArchivoAtCli(this.socket,"IO Exception envListaPathsGuardar");
		} catch (ClassNotFoundException e) {
			logger.logArchivoAtCli(this.socket,"ClassNotFoundException Exception envListaPathsGuardar");
			// e.printStackTrace();
		}
	}

	/*
	 * Metodo que recorre directorios indexando en una lista si son archivos,
	 * junto con su fecha de modificacion y sus paths absolutos
	 */
	public void indexarDirectorios(Archivo archivo, ListaPaths listado,
			String pathIni) {
		if (archivo.esArchivo()) {
			String pathFin = archivo.getPathCompleto().substring(
					pathIni.length() + 1, archivo.getPathCompleto().length());
			ReprArchivo nuevoArch = new ReprArchivo(pathIni, pathFin,
					archivo.ultimaModif());
			nuevoArch.setTamanio(archivo.getTamanio());
			String pathFinal = this.conecBase.cambiarBarraInvertida(pathFin);
			String pathFinAux = pathFinal;
			String buscado = "";
			String pathFinAuxPre = "";
			do {
				logger.logArchivoAtCli(this.socket,"Busco en la base el path inicial cliente de: "
						+ this.cambiarBarraInvertida(pathFinAux));
				buscado = this.conecBase.getPathEnCliente(pathFinAux);
				pathFinAuxPre = pathFinAux;
				pathFinAux = obtenerPathContenedor(pathFinAux);
			} while ((buscado.equals("")) && (pathFinAux != null)
					&& !(pathFinAuxPre.equals(pathFinAux)));
			if (buscado.equals("")) {
				logger.logArchivoAtCli(this.socket,"No encontre path inicial en servidor");
			} else {
				nuevoArch.setPathIniCli(buscado);
				logger.logArchivoAtCli(this.socket,"Seteado inicial vale: "
						+ this.cambiarBarraInvertida(buscado));
			}
			logger.logArchivoAtCli(this.socket,"Agrego a la lista el archivo: "
					+ this.cambiarBarraInvertida(nuevoArch.getPathIni() + "/"
							+ nuevoArch.getPathFin()));
			if (nuevoArch.getTamanio() <= this.size_limit) {
				listado.addPath(nuevoArch);
			} else {
				logger.logAmbosAtCli(this.socket,"El archivo "
						+ this.cambiarBarraInvertida(nuevoArch.getPathFin())
						+ " no se envia por superar el tamanio maximo de archivo");
			}
		} else {
			String[] subArchivos = archivo.listarArchivosDelDir();
			if (subArchivos != null) {
				for (int i = 0; i < subArchivos.length; i++) {
					Archivo subArchivo = new Archivo(archivo.getPathCompleto()
							+ "/" + subArchivos[i]);
					// logger.logArchivoAtCli(this.socket,"Indexo la carpeta : " +
					// this.cambiarBarraInvertida(subArchivo.getPathCompleto()));
					indexarDirectorios(subArchivo, listado, pathIni);
				}
			} else {
				// logger.logAmbosAtCli(this.socket("Carpeta para indexar vacia");
			}
		}
	}

	/*
	 * Metodo para obtener el path contenedor, es decir hasta la / anterior
	 */
	public String obtenerPathContenedor(String path) {
		String pathAnt = null;
		char[] pathArray = path.toCharArray();
		int j = -1;
		int largo = path.length();
		if (largo > 0) {
			for (int i = largo - 1; i >= 0; i--) {
				if (pathArray[i] == '/') {
					j = i;
				}
			}
			if (j > -1) {
				pathAnt = path.substring(0, j + 1);
			}
		}
		return pathAnt;
	}

	// nuevo metodo de envio de archivos.. solo envia archivo, y el path lo
	// envia antes
	public void transferirArchivo(ReprArchivo repArch) {

		try {
			// long acum = 0;
			this.oosEnvio.writeObject(repArch);
			// Armo el path del archivo de origen
			String pathOrigen = repArch.getPathIni() + "/"
					+ repArch.getPathFin();
			logger.logAmbosAtCli(this.socket,"Enviando archivo: "
					+ this.cambiarBarraInvertida(pathOrigen));
			// Se abre el fichero.
			FileInputStream fis = new FileInputStream(pathOrigen);

			// Creamos un array de tipo byte con el tamaÃ±o del archivo
			byte[] buffer = new byte[(int) repArch.getTamanio()];

			// Leemos el archivo y lo introducimos en el array de bytes
			fis.read(buffer);

			// Realizamos el envio de los bytes que conforman el archivo
			for (int i = 0; i < buffer.length; i++) {
				this.oosEnvio.write(buffer[i]);
				// acum++;
			}

			fis.close();
			this.oosEnvio.flush();

			// Recibir la confirmacion de que termino
			Object mensajeAux;
			MensajeConfirTransferencia mjeConfir;
			mensajeAux = this.oisRecep.readObject();
			if (mensajeAux instanceof MensajeConfirTransferencia) {
				mjeConfir = (MensajeConfirTransferencia) mensajeAux;
				logger.logArchivoAtCli(this.socket,"Recibo confirmacion de archivo "
						+ pathOrigen);
				logger.logArchivoAtCli(this.socket,"Confirmados " + mjeConfir.total + " bytes");
			} else {
				logger.logArchivoAtCli(this.socket,"No recibo confirmacion de archivo "
						+ pathOrigen);
			}

		} catch (IOException e) {
			System.err.println("Fallo el de la representacion del archivo");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.logAmbosAtCli(this.socket,"Fallo recepcion de mensaje de confirmacion");
			e.printStackTrace();
		}
	}

	public boolean isGuardIni() {
		return guardIni;
	}

	public void setGuardIni(boolean guardIni) {
		this.guardIni = guardIni;
	}

	// ##################### FIN RECUPERACION DE ARCHIVOS
	// ########################
}
