package com.ejemplo.mistareas;

public class Tarea {
	private String mDescripcion;
	private String mContactoURI;
	private String mContactoTelefono;
	
	public Tarea() {
		mDescripcion = "";
		mContactoURI = "";
		mContactoTelefono = "";
	}
	
	@Override
	public String toString() {
		return mDescripcion;
	}
	
	public String getDescripcion() {
		return mDescripcion;
	}

	public void setDescripcion(String unaDescripcion) {
		mDescripcion = unaDescripcion;
	}

	public String getContactoURI() {
		return mContactoURI;
	}

	public String getContactoTelefono() {
		return mContactoTelefono;
	}

	public void setContacto(String contactoURI, String telefono) {
		mContactoURI = contactoURI;
		mContactoTelefono = telefono;
	}
}
