package com.ejemplo.mistareas;

import java.util.ArrayList;

public class AdminTarea {
	/**
	 * Las tareas se guardarán en un arreglo.
	 */
	private ArrayList<Tarea> mArregloTareas;

	public AdminTarea() {
		mArregloTareas = new ArrayList<Tarea>();
	}
	
	public void agregar(String descripcion) {
		Tarea unaTarea = new Tarea();
		unaTarea.setDescripcion(descripcion);
		mArregloTareas.add(unaTarea);
	}
	
	public void asociarContacto(int id, String contactoURI, String telefono) {
		mArregloTareas.get(id).setContacto(contactoURI, telefono);
	}
	
	public String obtenerContactoTelefono(int id) {
		return mArregloTareas.get(id).getContactoTelefono();
	}
	
	public String obtenerContactoURI(int id) {
		return mArregloTareas.get(id).getContactoURI();
	}
	
	public void quitar(int id) {
		mArregloTareas.remove(id);
	}
	
	public ArrayList<Tarea> getArray() {
		return mArregloTareas;
	}
}
