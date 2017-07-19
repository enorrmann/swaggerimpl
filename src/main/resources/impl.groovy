import groovy.json.*

public String getTipoInasistencia(Map p){
	def rows = p.$sql.rows("select * from la_tipo_inasistencia",1,10);
	return new JsonBuilder(rows).toString();
	//return new JsonBuilder(p).toPrettyString();
}

public String getMotivosInasistencia(params){
	print params;
	return new JsonBuilder("ok").toString();
}

public String getCiclos(params){
	print params;
	return new JsonBuilder("ok").toString();
}

public String getPeriodos(params){
	def query =
	"""SELECT
	    ciclo_lectivo,
	    tipo_evaluacion,
	    evaluacion_periodo,
	    fecha_desde,
	    fecha_hasta
		FROM v_me_periodos_x_seccion
		WHERE 1=1
		    AND gp_id_grupo_alumno_anio = :idSeccion
		    AND ciclo_lectivo  = :cicloLectivo
		    AND LA_ID_TIPO_PROMOCION != 4
		ORDER BY la_id_evaluacion_periodo ASC
	"""
	def rows = params.$sql.rows(query,params,1,10);
	return new JsonBuilder(rows).toPrettyString();
}

public String updateInasistencia(params){
	return new JsonBuilder(params).toPrettyString();
}

