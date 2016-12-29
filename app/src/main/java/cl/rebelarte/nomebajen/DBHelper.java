package cl.rebelarte.nomebajen;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import cl.rebelarte.nomebajen.clases.Fiscalizador;
import cl.rebelarte.nomebajen.clases.Usuario;

/**
 * Created by HP on 22-12-2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static String DB = "NMB2.db";
    private static int version = 1;

    public DBHelper(Context context){
        super(context, DB, null, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        crearBD(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        crearBD(db);
    }

    public void crearBD(SQLiteDatabase db){
        try{
            db.execSQL("CREATE TABLE IF NOT EXISTS Fiscalizadores(id long, nombreUsuario varchar(255), latitud real, longitud real, fecha varchar(255), hora varchar(10),descripcion varchar(255), usuario varchar(255), subido int, eliminado int);");
            Log.e("Tabla","Tabla Fiscalizadores creada");
            db.execSQL("CREATE TABLE IF NOT EXISTS Usuario(id varchar(255) PRIMARY KEY, nombre varchar(255),email varchar(255),imei varchar(200), foto varchar(255), estado int);");
            Log.e("Tabla","Tabla Usuario creada");
            db.execSQL("CREATE TABLE IF NOT EXISTS Valorizacion(id INTEGER PRIMARY KEY, valor int, usuario varchar(255))");
            Log.e("Tabla","Tabla valorizacion creada");
            db.execSQL("CREATE TABLE IF NOT EXISTS Correlativo(nroCorrelativo long)");
            Log.e("Tabla","Tabla Correlativo Creada");
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
        }
    }

    public long getCorrelativo(SQLiteDatabase db){
        long correlativo = 0;
        try{
            String sql = "SELECT nroCorrelativo FROM Correlativo ORDER BY nroCorrelativo DESC LIMIT 1";
            Cursor c = db.rawQuery(sql,null);
            if(c.getCount() > 0){

                if(c.moveToFirst()){
                    do{
                        correlativo = c.getLong(0);

                    }while (c.moveToNext());
                }
            }
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
        }
        return correlativo;
    }

    public int actualizarCorrelativo(SQLiteDatabase db){
        int response = 0;
        try{
            String SQL = "UPDATE Correlativo SET nroCorrelativo = nroCorrelativo + 1";
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
            response = -1;
        }
        return response;
    }

    public int borrarCorrelativo(SQLiteDatabase db){
        int response = 0;
        try{
            String SQL = "DELETE FROM Correlativo";
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
            response = -1;
        }
        return response;
    }

    public int insertarCorrelativo(SQLiteDatabase db, long correlativo){
        int response = 0;
        try{
            String SQL = "INSERT INTO Correlativo VALUES("+correlativo+")";
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            response = -1;
            Log.e("SQLiteException", ex.getMessage());
        }
        return response;
    }

    public Usuario getUser(SQLiteDatabase db){
        Usuario u = null;
        try{
            String sql = "SELECT id, nombre, email, imei, estado, foto FROM Usuario";
            Cursor c = db.rawQuery(sql,null);
            if(c.getCount() > 0){
                u = new Usuario();
                if(c.moveToFirst()){
                    do{
                        u.setId(c.getString(0));
                        u.setNombre(c.getString(1));
                        u.setEmail(c.getString(2));
                        u.setImei(c.getString(3));
                        u.setEstado(c.getInt(4));
                        u.setFoto(c.getString(5));
                    }while (c.moveToNext());
                }
            }
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
        }
        return u;
    }

    public int modificarFoto(String id, String foto, SQLiteDatabase db){
        int response = 0;
        try{
            String SQL = "UPDATE Usuario SET foto = '"+foto+"' WHERE id = '"+id+"'";
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
            response = -1;
        }
        return response;
    }

    public int agregarUsuario(Usuario u, SQLiteDatabase db){
        int response = 0;
        try{
            String sql = "INSERT INTO Usuario(id, nombre,email,imei,estado, foto) VALUES('"+u.getId()+"','"+u.getNombre()+"','"+u.getEmail()+"','"+u.getImei()+"',1,'"+u.getFoto()+"')";
            db.execSQL(sql);
            response = 1;
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
            response = -1;
        }
        return response;
    }

    public int actualizarEstadoUsuario(int estado, SQLiteDatabase db){
        int response = 0;
        try{
            String sql = "UPDATE Usuario SET estado = " + estado;
            db.execSQL(sql);
            response = 1;
        }catch (SQLiteException ex){
            response = -1;
        }
        return  response;
    }

    public Fiscalizador getFiscalizador(SQLiteDatabase db, long id){
        Fiscalizador f = null;
        try{
            String sql = "SELECT id, usuario, nombreUsuario, latitud, longitud, fecha, hora, descripcion FROM Fiscalizadores WHERE id = " + id;
            Cursor c = db.rawQuery(sql,null);
            if(c.getCount() > 0){
                f = new Fiscalizador();
                if(c.moveToFirst()){
                    do{
                        f.setId(c.getLong(0));
                        f.setUsuario(c.getString(1));
                        f.setNombreUsuario(c.getString(2));
                        f.setLatitud(c.getDouble(3));
                        f.setLongitud(c.getDouble(4));
                        f.setFecha(c.getString(5));
                        f.setHora(c.getString(6));
                        f.setDescripcion(c.getString(7));
                    }while (c.moveToNext());
                }
            }
        }catch (SQLiteException ex){
            Log.e("SQLiteException", ex.getMessage());
        }
        return f;
    }

    public int agregarFiscalizador(Fiscalizador f, SQLiteDatabase db){
        int response = 0;
        try{
            String SQL = "INSERT INTO Fiscalizadores(id,usuario,nombreUsuario,latitud,longitud,fecha,hora,descripcion, subido, eliminado) VALUES("+f.getId()+",'"+f.getUsuario()+"','"+f.getNombreUsuario()+"',"+f.getLatitud()+","+f.getLongitud()+",'"+f.getFecha()+"','"+f.getHora()+"','"+f.getDescripcion()+"',"+f.getSubido()+",0)";
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            response = -1;
        }
        return response;
    }

    public int BorrarFiscalizaciones(SQLiteDatabase db){
        int response = 0;
        try{
            String SQL = "DELETE FROM Fiscalizadores WHERE subido = 1";
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            response = -1;
        }
        return response;
    }

    public ArrayList<Fiscalizador> SubirFiscalizadores(SQLiteDatabase db){
        ArrayList<Fiscalizador> fiscalizadores = null;
        try{
            Cursor c = db.rawQuery("SELECT id, latitud, longitud, fecha, hora, descripcion, nombreUsuario, usuario FROM Fiscalizadores WHERE subido = 0" ,null);
            fiscalizadores = new ArrayList<Fiscalizador>();


            if(c.getCount() > 0){

                if(c.moveToFirst()){
                    do{
                        Fiscalizador f = new Fiscalizador();
                        f.setId(c.getLong(0));
                        f.setLatitud(c.getDouble(1));
                        f.setLongitud(c.getDouble(2));
                        f.setFecha(c.getString(3));
                        f.setHora(c.getString(4));
                        f.setDescripcion(c.getString(5));
                        f.setNombreUsuario(c.getString(6));
                        f.setUsuario(c.getString(7));
                        fiscalizadores.add(f);
                    }while(c.moveToNext());
                }
            }
        }catch(SQLiteException e){
            Log.e("SQLiteException",e.getMessage());
        }catch (Exception e){
            Log.e("Exception",e.getMessage());
        }
        return fiscalizadores;
    }

    public int actualizarFiscalizacionSubida(SQLiteDatabase db, long id){
        int response = 0;
        String SQL = "UPDATE Fiscalizadores SET subido = 1 WHERE id = " + id;
        try{
            db.execSQL(SQL);
            response = 1;
        }catch (SQLiteException ex){
            response = -1;
            Log.e("SQLiteException",ex.getMessage());
        }
        return response;
    }

    public ArrayList<Fiscalizador> verFiscalizadores(SQLiteDatabase db){
        ArrayList<Fiscalizador> fiscalizadores = null;
        try{
            Cursor c = db.rawQuery("SELECT id, latitud, longitud, fecha, hora,  descripcion FROM Fiscalizadores" ,null);
            fiscalizadores = new ArrayList<Fiscalizador>();


            if(c.getCount() > 0){

                if(c.moveToFirst()){
                    do{
                        Fiscalizador f = new Fiscalizador();
                        f.setId(c.getLong(0));
                        f.setLatitud(c.getDouble(1));
                        f.setLongitud(c.getDouble(2));
                        f.setFecha(c.getString(3));
                        f.setHora(c.getString(4));
                        f.setDescripcion(c.getString(5));
                        fiscalizadores.add(f);
                    }while(c.moveToNext());
                }
            }
        }catch(SQLiteException e){
            Log.e("SQLiteException",e.getMessage());
        }catch (Exception e){
            Log.e("Exception",e.getMessage());
        }
        return fiscalizadores;
    }


}
