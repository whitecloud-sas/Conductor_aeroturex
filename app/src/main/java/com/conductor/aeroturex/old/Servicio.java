package com.conductor.aeroturex.old;

public class Servicio {

	private String barrio;
	private String fecha;
	private String estado;

   //public Servicio(){}

	public Servicio(String barrio, String fecha, String estado){
		setName(barrio);
		setSex(fecha);
		setEstado(estado);
	}

	public String getName() {
		return barrio;
	}
	public String getEstado() {
		return estado;
	}
	public String getSex() {
		return fecha;
	}
	public void setName(String barrio) {
		this.barrio = barrio;
	}
	public void setSex(String fecha) {
		this.fecha = fecha;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}

}
