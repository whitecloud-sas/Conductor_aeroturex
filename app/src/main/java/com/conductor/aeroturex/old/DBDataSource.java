package com.conductor.aeroturex.old;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import com.conductor.aeroturex.MainActivity;
import com.conductor.aeroturex.service.TaxiLujoService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBDataSource {
    int v_cantservicios = 100, v_cantmensajes = 200;
    // Database fields
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {DBHelper.COLUMNA_ID, DBHelper.COLUMNA_DESCRIPCION};

    public DBDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void dropdb() {
        dbHelper.onUpgrade(database, 2, 3);
    }

    public DB createRegistro(String comment, String tipo, int estado) {
        log("createRegistro " + comment);
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMNA_DESCRIPCION, comment);
        values.put(DBHelper.COLUMNA_TIPO, tipo);
        values.put(DBHelper.COLUMNA_ESTADO, estado);
        values.put(DBHelper.COLUMNA_IDLIST, System.currentTimeMillis() / 1000L);
        long insertId = database.insert(DBHelper.TABLA, null, values);
        Cursor cursor = database.query(DBHelper.TABLA, allColumns, DBHelper.COLUMNA_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        DB newComment = cursorToComment(cursor);
        cursor.close();

        return newComment;
    }

    public long create_mensaje(String comment, int me) {
        log("create_mensaje " + comment);
        ContentValues values = new ContentValues();

        String[] allColumns_mensajes = {"m_id", "m_descripcion"};
        try {

            JSONObject jObj = new JSONObject(comment);
            values.put("m_descripcion", comment);
            values.put("m_estado", me);
            values.put("m_fecha_creacion", System.currentTimeMillis() / 1000L);
            values.put("m_origen", jObj.getString("origen"));
            values.put("m_id_postgres", jObj.getString("id"));
            values.put("m_me", me);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        long insertId = database.insert("mensajes", null, values);
        Cursor cursor = database.query("mensajes", allColumns_mensajes, " m_id = " + insertId, null, null, null, null, null);
        cursor.moveToFirst();
        cursor.close();

        return insertId;
    }

    public JSONObject select_tipo_para_enviar(String tipo) {
        String descripcion, tiempo, id, sql;
        JSONObject jObj = new JSONObject();
        long fecha_actual = (System.currentTimeMillis() / 1000L);
        //traemos todos los que esten en estado 1 con mas de 10 segs
        sql = "SELECT idlist,id FROM da WHERE tipo='" + tipo + "' and estado=1 limit 1";
        Cursor c = database.rawQuery(sql, null);

        if (c.moveToFirst()) {
            do {
                tiempo = c.getString(0);
                id = c.getString(1);
                //compara si tiene mas de 10 segs de espera de haber sido enviado
                //log("Fecha actual: " + fecha_actual+ " >= " + (Long.parseLong(tiempo)+60) );
                if (fecha_actual >= (Long.parseLong(tiempo) + 10)) {
                    //log("registro " + tipo + " fue creado hace mas de 10 segs, debe ser reenviado");
                    sql = "update da set idlist=" + fecha_actual + ",estado=0 where id=" + id;
                    database.execSQL(sql);
                }
            } while (c.moveToNext());
        }

        c.close();

        //traemos todos los que esten en estado 0
        sql = "SELECT descripcion,id FROM da WHERE tipo='" + tipo + "' and estado in (0) limit 1";
        Cursor c1 = database.rawQuery(sql, null);
        try {
            if (c1.moveToFirst()) {
                do {
                    descripcion = c1.getString(0);
                    id = c1.getString(1);

                    sql = "update " + DBHelper.TABLA + " set estado=1 where id=" + id;
                    database.execSQL(sql);

                    jObj.put("sqlite_id", id);
                    jObj.put("descripcion", descripcion);
                } while (c1.moveToNext());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c1.close();
        return jObj;
    }

    //para revisar si se borra o no
    public String selectRegistro(long id, String tipo) {
        String descripcion = "", idsrv = "", estado = "", tipoCancela = "";
        updateRegistros(tipo);
        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_DESCRIPCION + "," + DBHelper.COLUMNA_ID + "," + DBHelper.COLUMNA_ESTADO + "," + DBHelper.COLUMNA_TIPOCANCELA + " FROM " + DBHelper.TABLA + " WHERE " + DBHelper.COLUMNA_IDLIST + "=" + id + " and tipo='" + tipo + "'", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan m�s registros
            do {
                descripcion = c.getString(0);
                idsrv = c.getString(1);
                estado = c.getString(2);
                tipoCancela = c.getString(3);
            } while (c.moveToNext());
        }
        c.close();
//		System.out.println("Registro Seleccionado con listid: " + id + " regid "+idsrv + " estado: " + estado);
        if (tipo.equals("servicio"))
            return descripcion + "|" + tipoCancela + "|" + idsrv + ";;" + estado;
        else if (tipo.equals("mensaje"))
            return descripcion + "|" + tipoCancela + "|" + idsrv;
        else
            return null;
    }

    public Boolean validaLeidos() {
        Boolean respuesta = false;
        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_ID + " FROM " + DBHelper.TABLA + " WHERE " + DBHelper.COLUMNA_ESTADO + "=2 and tipo='mensaje'", null);

        if (c.moveToFirst()) {
            do {
                respuesta = true;
            } while (c.moveToNext());
        }
        c.close();

        return respuesta;
    }

    public String validaPendientes() {
        String descripcion = "", estado = "", id = "", tipoCancela = "";

        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_ID + "," + DBHelper.COLUMNA_DESCRIPCION + "," + DBHelper.COLUMNA_ESTADO + "," + DBHelper.COLUMNA_TIPOCANCELA + " FROM " + DBHelper.TABLA + " WHERE " + DBHelper.COLUMNA_ESTADO + " in (1,6,7) and tipo='servicio'", null);

        if (c.moveToFirst()) {
            do {
                id = c.getString(0);
                descripcion = c.getString(1);
                estado = c.getString(2);
                tipoCancela = c.getString(3);
            } while (c.moveToNext());
        }
        c.close();
        //log("Servicio Pendiente: " + id + " estado: "+estado+ " descripcion: "+ descripcion + " tipoCancela: "+ tipoCancela);
        return id + "|" + descripcion + "|" + estado + "|" + tipoCancela;
    }

    public String select(String tipo) {
        String descripcion = "";

        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_DESCRIPCION + " FROM " + DBHelper.TABLA + " WHERE tipo='" + tipo + "'", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan m�s registros
            do {
                descripcion = c.getString(0);
            } while (c.moveToNext());
        }
        c.close();
        return descripcion;
    }

    public double selectIdentificador() {
        double identificador = 0;
        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_DESCRIPCION + " FROM " + DBHelper.TABLA + " WHERE tipo='identificador'", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan mas registros
            do {
                identificador = Double.parseDouble(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return identificador;
    }

    public String selectSolicitaUbicacion() {
        String descripcion = "";

        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_DESCRIPCION + " FROM " + DBHelper.TABLA + " WHERE tipo='ubicacion'", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan más registros
            do {
                descripcion = c.getString(0);
            } while (c.moveToNext());
        }
        //y lo cerramos
        c.close();
        //si descripcion = "" es porque no hay nada creado aun
        return descripcion;
    }

    public void create(String desc, String tipo) {

        Cursor c = database.rawQuery("SELECT " + DBHelper.COLUMNA_DESCRIPCION + " FROM " + DBHelper.TABLA + " WHERE tipo='" + tipo + "'", null);
        //no hay registro creado aun
        if (c.getCount() == 0) {
            createRegistro(desc, tipo, 0);
        } else {
            String sql = "update " + DBHelper.TABLA + " set " + DBHelper.COLUMNA_DESCRIPCION + "='" + desc + "' where tipo='" + tipo + "'";
            database.execSQL(sql);
        }

        if (tipo.equals(Constants.ACTION.TRAMA_X_ENVIAR)) {
            MainActivity.trama_x_enviar_contador = 10;
        }
    }

    public void update_postgres_id(JSONObject jObj){
        try{
            String descripcion="";
            Cursor c = database.rawQuery("SELECT m_descripcion from mensajes where m_id = "+ jObj.getString("m_id"), null);

            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                //Recorremos el cursor hasta que no hayan más registros
                do {
                    descripcion = c.getString(0);
                } while (c.moveToNext());
            }
            //y lo cerramos
            c.close();

            JSONObject jObj2 = new JSONObject(descripcion);
            jObj2.put("id",jObj.getString("id"));

            String sql = "UPDATE mensajes set m_descripcion='"+jObj2.toString()+"'," +
                    "m_id_postgres = " + jObj.getString("id") +
                    " WHERE m_id = " + jObj.getString("m_id");

            log("update_postgres_id "+sql);
            database.execSQL(sql);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public String get_from_postgres_id(String m_id_postgres){

        String descripcion = "";
        Cursor c = database.rawQuery("SELECT m_descripcion from mensajes where m_id_postgres = " + m_id_postgres, null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan más registros
            do {
                descripcion = c.getString(0);
            } while (c.moveToNext());
        }
        //y lo cerramos
        c.close();

        return descripcion;
    }

    public void deleteId(String id) {
        if (id.trim().equals("")) {
            //en caso que el id este vacio y no genere cierre de la app, ponemos el valor 0
            id = "0";
        }

        String sql = "DELETE FROM " + DBHelper.TABLA + " where id=" + id;
        log("deleteId " + sql);
        database.execSQL(sql);
    }

    public void delete_desc(String desc, String tipo) {
        String sql = "DELETE FROM " + DBHelper.TABLA + " where tipo='" + tipo + "' and descripcion like '%" + desc + "%'";
        log(sql);
        database.execSQL(sql);
    }

    public void delete_msj_desc(String id) {

        if (id.equals("")) {
            id = "0";
        }

        String sql = "DELETE FROM mensajes where m_id_postgres = " + id;
        log(sql);
        database.execSQL(sql);
    }

    public void updateRegistros(String tipo) {
        //actualiza el id de la lista que muestra en la UI
        int cont = 0;
        String sql = "SELECT " + DBHelper.COLUMNA_ID + " FROM " + DBHelper.TABLA + " where " + DBHelper.COLUMNA_TIPO + "='" + tipo + "'order by " + DBHelper.COLUMNA_ID + " desc";
        Cursor c = database.rawQuery(sql, null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan más registros
            do {
                String id = c.getString(0);
                sql = "update " + DBHelper.TABLA + " set idlist=" + (cont++) + "  where id=" + id;
                database.execSQL(sql);
            } while (c.moveToNext());
        }
        c.close();
    }

    public void updateEstado(String id, int estado) {
        String sql;
        sql = "update " + DBHelper.TABLA + " set " + DBHelper.COLUMNA_ESTADO + "=" + estado + "  where estado=2 and id=" + id;
//		System.out.println("Actualizando estado: " + sql);
        database.execSQL(sql);
    }

    public void updateServEstado(int estado) {
        String sql = "update " + DBHelper.TABLA + " set " + DBHelper.COLUMNA_ESTADO + "=" + estado + " where tipo='servicio' and " + DBHelper.COLUMNA_ESTADO + " in (1,6,7)";
//		System.out.println("Actualizando estado: " + sql);
        database.execSQL(sql);
    }

    public void updateServtipoCancela(String tipoCancela) {
        String sql = "update " + DBHelper.TABLA + " set " + DBHelper.COLUMNA_TIPOCANCELA + "=" + tipoCancela + " where tipo='servicio' and " + DBHelper.COLUMNA_ESTADO + "=1";
//		System.out.println("Actualizando Tipo Cancelacion: " + sql);
        database.execSQL(sql);
    }

    public void deleteComment(DB comment) {
        long id = comment.getId();
//		log("Registro Eliminado con id: " + id);
        database.delete(DBHelper.TABLA, DBHelper.COLUMNA_ID + " = " + id, null);
    }

    public void deleteAll() {
//		log("Eliminando todos los registros de "+DBHelper.TABLA);
        database.delete(DBHelper.TABLA, null, null);
    }

    public void deleteTipo(String tipo) {
        String sql = "DELETE FROM " + DBHelper.TABLA + " where tipo='" + tipo + "'";
        database.execSQL(sql);
    }

    public void deleteOrigen(String desc) {
        //try {
        //JSONObject jObj = new JSONObject(desc);
        //String origen = jObj.getString("origen");
        String sql = "DELETE FROM mensajes where m_origen='" + desc + "'";
        log("deleteOrigen " + sql);
        database.execSQL(sql);
        //}catch (JSONException e){
        //   e.printStackTrace();
        //}
    }

    public void delete_msg_id(String id) {
        //try {
        //JSONObject jObj = new JSONObject(content);
        //String m_id_postgres = jObj.getString("id");
        String sql = "DELETE FROM mensajes where m_id_postgres=" + id;
        log(sql);
        database.execSQL(sql);
        //}catch (JSONException e){
        //e.printStackTrace();
        //}
    }

    public JSONArray getAllRegs(String tipo) {
        //selecciona los registros de acuerdo al tipo(servicio,mensaje, etc)
        JSONArray jArr = new JSONArray();
        String id, descripcion;
        int v_contador = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.TABLA + " where " + DBHelper.COLUMNA_TIPO + "='" + tipo + "' order by " + DBHelper.COLUMNA_ID + " desc", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                v_contador++;
                id = cursor.getString(0);
                descripcion = cursor.getString(1);
                //log("Leyendo " + tipo + ": id " + id + " descripcion: " + servicio);
                if (tipo.equals("servicio")) {
                    if (v_contador > v_cantservicios) {
                        database.delete(DBHelper.TABLA, DBHelper.COLUMNA_ID + " = " + id, null);
                        log("Eliminando por exceso de servicios:" + v_cantservicios + "|contador: " + v_contador);
                    } else {
                        JSONObject list1 = new JSONObject(descripcion);
                        jArr.put(list1);
                        //actualiza el id de la lista
                        String sql = "update " + DBHelper.TABLA + " set idlist=" + v_contador + "  where id=" + id;
                        database.execSQL(sql);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
        return jArr;
    }

    public JSONArray getAll_mensajes_grouped() {
        JSONArray jArr = new JSONArray();
        String id, descripcion;
        int v_contador = 0;
        Cursor cursor = database.rawQuery("SELECT m_id,m_descripcion,m_origen FROM mensajes GROUP BY m_origen ORDER BY m_id DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                v_contador++;
                id = cursor.getString(0);
                descripcion = cursor.getString(1);

                JSONObject list1 = new JSONObject(descripcion);
                jArr.put(list1);

                //actualiza el id de la lista
                String sql = "update mensajes set m_lista_posicion=" + v_contador + "  where m_id=" + id;
                database.execSQL(sql);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
        return jArr;
    }

    public JSONArray getAll_mensajes(JSONObject jObj) {
        JSONArray jArr = new JSONArray();
        String descripcion, m_origen = "",m_fecha_creacion;
        //String m_id_postgres = "0";
        log("getAll_mensajes " + jObj);
        try {
            if(jObj.has("user_name"))
                m_origen = jObj.getString("user_id");
            else if(jObj.has("nombre"))
                m_origen = jObj.getString("user_id");
            else if(jObj.has("origen")){
                m_origen = jObj.getString("origen");
            }else{
                m_origen = jObj.getString("destino");
            }

            if(m_origen.equals("0"))
                m_origen="Central";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int me;

        //traemos todos los mensajes de ese origen
        String sql ="SELECT m_id,m_descripcion,m_me,m_fecha_creacion FROM mensajes WHERE m_origen='" + m_origen + "' ORDER BY m_id ASC";
        log(sql);
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                descripcion = cursor.getString(1);
                me = cursor.getInt(2);
                m_fecha_creacion = cursor.getString(3);
                JSONObject list1 = new JSONObject(descripcion);
                list1.put("me", me);
                list1.put("m_fecha_creacion",m_fecha_creacion);
                jArr.put(list1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
        return jArr;
    }

    public int cantServicios() {
        //selecciona los registros de acuerdo al tipo(servicios,mensajes.etc)
        int v_contador = 0;
        Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.TABLA + " where " + DBHelper.COLUMNA_TIPO + "='servicio'", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            v_contador++;
            cursor.moveToNext();
        }
        cursor.close();
        return v_contador;
    }

    private DB cursorToComment(Cursor cursor) {
        DB comment = new DB();
        comment.setId(cursor.getLong(0));
        comment.setComment(cursor.getString(1));
        return comment;
    }

    public JSONArray selectUbicacionPendientes() {

        JSONArray jArray = new JSONArray();
        String sql = "SELECT su_fecha,su_servicio,su_id,su_distancia,su_precio," +
                "su_tiempoespera,su_velocidad,su_orientacion,su_latitud,su_longitud,su_precision,su_fechagps,su_esbuena " +
                "FROM da " +
                "WHERE " + DBHelper.COLUMNA_TIPO + "='"+Constants.DB.RUTA+"' " +
                "and su_id='0' " +
                "ORDER BY su_fechagps";
        if (database != null) {
            Cursor c = database.rawQuery(sql, null);
            if (c.moveToFirst()) {
                do {
                    try {

                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("cmd", "76");
                        jsonObj.put("servicio", c.getString(1));
                        jsonObj.put("lat_", c.getString(8));
                        jsonObj.put("lng_", c.getString(9));
                        jsonObj.put("distancia", c.getString(3));
                        jsonObj.put("precio", c.getString(4));
                        jsonObj.put("velocidad", c.getString(6));
                        jsonObj.put("orientacion", c.getString(7));
                        jsonObj.put("tiempo_espera", c.getString(5));
                        jsonObj.put("fecha_", c.getString(10));
                        jsonObj.put("precision", c.getString(11));
                        jsonObj.put("lat", String.valueOf(MainActivity.v_latitud));
                        jsonObj.put("lng", String.valueOf(MainActivity.v_longitud));
                        jsonObj.put("date_gps", String.valueOf(MainActivity.v_fechaGPS));
                        jArray.put(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        return jArray;
    }

    public void createUbicacion(JSONObject jObj) {
        try {

            if(!jObj.getString("su_servicio").equals("0") && !jObj.getString("su_servicio").equals("")) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMNA_SUID, jObj.getString("su_id"));
                values.put(DBHelper.COLUMNA_SUSERVICIO, jObj.getString("su_servicio"));
                values.put(DBHelper.COLUMNA_SULATITUD, jObj.getString("su_latitud"));
                values.put(DBHelper.COLUMNA_SULONGITUD, jObj.getString("su_longitud"));
                values.put(DBHelper.COLUMNA_SUPRECIO, jObj.getString("su_precio"));
                values.put(DBHelper.COLUMNA_SUDISTANCIA, jObj.getString("su_distancia"));
                values.put(DBHelper.COLUMNA_SUTIEMPOESPERA, jObj.getString("su_tiempoespera"));
                values.put(DBHelper.COLUMNA_SUORIENTACION, jObj.getString("su_orientacion"));
                values.put(DBHelper.COLUMNA_SUVELOCIDAD, jObj.getString("su_velocidad"));
                values.put(DBHelper.COLUMNA_SUFECHAGPS, jObj.getString("su_precision"));
                values.put(DBHelper.COLUMNA_SUPRECISION, jObj.getString("su_fechagps"));
                values.put(DBHelper.COLUMNA_SUESBUENA, jObj.getString("su_esbuena"));
                values.put(DBHelper.COLUMNA_TIPO, Constants.DB.RUTA);
                values.put(DBHelper.COLUMNA_DESCRIPCION, Constants.DB.RUTA);
                database.insert(DBHelper.TABLA, null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateUbicacion(JSONObject jObj) {
        try {
            String sql = "update " + DBHelper.TABLA + " set "
                    + DBHelper.COLUMNA_SUID + "='" + jObj.getString("id") + "' "
                    + "where su_id='0' "
                    + "and su_precio='" + jObj.getString("precio") + "' "
                    + "and su_distancia='" + jObj.getString("distancia") + "'";
            database.execSQL(sql);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String select_v2(String tipo) {
        String descripcion = "";

        Cursor c = database.rawQuery("SELECT descripcion,id FROM da WHERE tipo='" + tipo + "'", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan mas registros
            do {
                if(isJSONValid(c.getString(0))){
                    descripcion = c.getString(0);
                    try {
                        JSONObject jObj = new JSONObject(descripcion);
                        jObj.put("slt",c.getString(1));
                        descripcion = jObj.toString();
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }else{
                    descripcion = c.getString(0) + "|" + c.getString(1);
                }
            } while (c.moveToNext());
        }
        c.close();
        return descripcion;
    }

    boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public void update_conversacion_readed(String origen) {

        String sql = "update mensajes set m_estado=1 where m_estado=0 and m_origen='" + origen + "'";
        log("update_conversacion_readed " + sql);
        database.execSQL(sql);
    }

    public int cant_mensajes_sin_leer_origen(String origen) {
        int cantidad=0;

        Cursor c = database.rawQuery("SELECT count(*) FROM mensajes WHERE m_estado=0 and m_origen='" + origen + "'", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan mas registros
            do {
                cantidad = c.getInt(0);
            } while (c.moveToNext());
        }
        c.close();
        return cantidad;
    }

    public int cant_mensajes_sin_leer() {

        int cantidad=0;

        Cursor c = database.rawQuery("SELECT count(*) FROM mensajes WHERE m_estado=0", null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan mas registros
            do {
                cantidad = c.getInt(0);
            } while (c.moveToNext());
        }
        c.close();
        return cantidad;
    }

    public void update_servicio(String servicio_id, int valor, int recorrido, String estado, String empresa) {

        String descripcion="",id="0";
        String sql ="SELECT descripcion,id FROM da WHERE descripcion like '%" + "\"servicio\":\"" + servicio_id + "\"%'";
        log("update_servicio: " + sql);
        Cursor c = database.rawQuery(sql, null);

        //Nos aseguramos de que existe al menos un registro
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no hayan mas registros
            do {
                descripcion = c.getString(0);
                id=c.getString(1);
                log("update_servicio leyendo servicio: " + descripcion);
            } while (c.moveToNext());
        }
        c.close();

        try {
            if (!descripcion.equals("")) {
                JSONObject jObj = new JSONObject(descripcion);
                jObj.put("estado", estado);
                jObj.put("recorrido", recorrido+"");
                jObj.put("empresa", empresa);
                jObj.put("valor", valor + "");


                ContentValues valores = new ContentValues();
                valores.put("descripcion", jObj.toString());
                log("update_servicio actualizando descripcion: "  + jObj.toString());
                //Actualizamos el registro en la tabla
                database.update(DBHelper.TABLA, valores, "id=" + id, null);

            }


        }catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private static void log(String s) {
        Log.d(DBDataSource.class.getSimpleName(), "######" + s + "######");
    }
}