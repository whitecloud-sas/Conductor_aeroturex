package com.conductor.aeroturex.old;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper {

    static final String TABLA = "da";
    static final String COLUMNA_ID = "id";
    static final String COLUMNA_DESCRIPCION = "descripcion";
    static final String COLUMNA_IDLIST = "idlist";
    static final String COLUMNA_TIPO = "tipo";
    static final String COLUMNA_TIPOCANCELA = "tipo_cancela";
    static final String COLUMNA_ESTADO = "estado";
    static final String COLUMNA_SULATITUD = "su_latitud";
    static final String COLUMNA_SULONGITUD = "su_longitud";
    static final String COLUMNA_SUPRECIO = "su_precio";
    static final String COLUMNA_SUDISTANCIA = "su_distancia";
    static final String COLUMNA_SUVELOCIDAD = "su_velocidad";
    static final String COLUMNA_SUTIEMPOESPERA = "su_tiempoespera";
    static final String COLUMNA_SUORIENTACION = "su_orientacion";
    static final String COLUMNA_SUSERVICIO = "su_servicio";
    static final String COLUMNA_SUFECHA = "su_fecha";
    static final String COLUMNA_SUFECHAGPS = "su_fechagps";
    static final String COLUMNA_SUPRECISION = "su_precision";
    static final String COLUMNA_SUID = "su_id";
    static final String COLUMNA_SUESBUENA = "su_esbuena";
    static final String COLUMNA_IDFROMDB = "idfromdb";
    static final String COLUMNA_ORIGEN = "msg_origen";
    private static final String DATABASE_NAME = "da.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + TABLA
            + "( " + COLUMNA_ID + " integer primary key autoincrement,"
            + COLUMNA_DESCRIPCION + " text not null,"
            + COLUMNA_IDLIST + " integer,"
            + COLUMNA_TIPO + " text,"
            + COLUMNA_SUID + " text,"
            + COLUMNA_SULATITUD + " text,"
            + COLUMNA_SULONGITUD + " text,"
            + COLUMNA_SUPRECIO + " text,"
            + COLUMNA_SUDISTANCIA + " text,"
            + COLUMNA_SUTIEMPOESPERA + " text,"
            + COLUMNA_SUORIENTACION + " text,"
            + COLUMNA_SUSERVICIO + " text,"
            + COLUMNA_SUVELOCIDAD + " text,"
            + COLUMNA_SUFECHA + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMNA_ESTADO + " integer,"
            + COLUMNA_SUFECHAGPS + " text,"
            + COLUMNA_SUESBUENA + " text,"
            + COLUMNA_SUPRECISION + " text,"
            + COLUMNA_TIPOCANCELA + " integer,"
            + COLUMNA_IDFROMDB + " integer,"
            + COLUMNA_ORIGEN + " text"
            +"); ";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);

        database.execSQL("create table mensajes (" +
                "m_id integer primary key autoincrement," +
                "m_fecha_creacion integer," +
                "m_fecha_envio integer," +
                "m_fecha_recibe integer," +
                "m_estado integer," +
                "m_id_postgres int," +
                "m_descripcion text," +
                "m_origen text," + // 0=no leido, 1=leido
                "m_lista_posicion int," +
                "m_me int" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        db.execSQL("DROP TABLE IF EXISTS mensajes");
        onCreate(db);
    }

}
