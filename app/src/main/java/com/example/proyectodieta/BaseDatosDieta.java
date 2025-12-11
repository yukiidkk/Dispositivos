package com.example.proyectodieta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BaseDatosDieta extends SQLiteOpenHelper {

    private static final String DB_NAME = "dietas.db";

    private static final int DB_VERSION = 3;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public BaseDatosDieta(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creación de todas las tablas
        db.execSQL("CREATE TABLE dietas (id_dieta INTEGER PRIMARY KEY, nombre TEXT, imagen INTEGER)");
        db.execSQL("CREATE TABLE platillos (id_platillo INTEGER PRIMARY KEY, id_dieta INTEGER, nombre TEXT, tipo_comida TEXT, FOREIGN KEY(id_dieta) REFERENCES dietas(id_dieta))");
        db.execSQL("CREATE TABLE ingredientes (id_ingrediente INTEGER PRIMARY KEY, nombre TEXT, consumido INTEGER)");
        db.execSQL("CREATE TABLE platillo_ingrediente (id_platillo INTEGER, id_ingrediente INTEGER, FOREIGN KEY(id_platillo) REFERENCES platillos(id_platillo), FOREIGN KEY(id_ingrediente) REFERENCES ingredientes(id_ingrediente))");
        db.execSQL("CREATE TABLE dieta_ingrediente (id_dieta INTEGER, id_ingrediente INTEGER, FOREIGN KEY(id_dieta) REFERENCES dietas(id_dieta), FOREIGN KEY(id_ingrediente) REFERENCES ingredientes(id_ingrediente))");
        db.execSQL("CREATE TABLE progreso_semana (id INTEGER PRIMARY KEY, dieta_elegida INTEGER, fecha_inicio TEXT)");

        // Inserción de datos iniciales
        insertarDietasIniciales(db);
        insertarPlatillosEIngredientesIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS platillo_ingrediente");
        db.execSQL("DROP TABLE IF EXISTS dieta_ingrediente");
        db.execSQL("DROP TABLE IF EXISTS ingredientes");
        db.execSQL("DROP TABLE IF EXISTS platillos");
        db.execSQL("DROP TABLE IF EXISTS dietas");
        db.execSQL("DROP TABLE IF EXISTS progreso_semana");
        onCreate(db);
    }

    private boolean isPlatilloPreparado(int idPlatillo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM platillo_ingrediente pi INNER JOIN ingredientes i ON pi.id_ingrediente = i.id_ingrediente WHERE pi.id_platillo = ? AND i.consumido = 0", new String[]{String.valueOf(idPlatillo)});
        if (c.moveToFirst()) {
            int ingredientesNoConsumidos = c.getInt(0);
            c.close();
            return ingredientesNoConsumidos == 0;
        }
        c.close();
        return false;
    }

    private void insertarDietasIniciales(SQLiteDatabase db) {
        insertarDieta(db, "Keto", R.drawable.keto);
        insertarDieta(db, "Vegetariana", R.drawable.vegetariana);
        insertarDieta(db, "Vegana", R.drawable.vegana);
        insertarDieta(db, "Libre de Gluten", R.drawable.glutenfree);
        insertarDieta(db, "Carnívora", R.drawable.carnivora);
    }

    private void insertarDieta(SQLiteDatabase db, String nombre, int imagen) {
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("imagen", imagen);
        db.insert("dietas", null, cv);
    }

    public List<Dieta> obtenerTodasLasDietas() {
        List<Dieta> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM dietas", null);
        while (c.moveToNext()) {
            lista.add(new Dieta(c.getInt(0), c.getString(1), c.getInt(2)));
        }
        c.close();
        return lista;
    }

    public Dieta obtenerDietaPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM dietas WHERE id_dieta = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            Dieta d = new Dieta(c.getInt(0), c.getString(1), c.getInt(2));
            c.close();
            return d;
        }
        c.close();
        return null;
    }

    public long insertarPlatillo(int idDieta, String nombre, String tipoComida) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_dieta", idDieta);
        cv.put("nombre", nombre);
        cv.put("tipo_comida", tipoComida);
        return db.insert("platillos", null, cv);
    }

    public List<Platillo> obtenerPlatillosPorDieta(int idDieta) {
        List<Platillo> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM platillos WHERE id_dieta = ?", new String[]{String.valueOf(idDieta)});
        while (c.moveToNext()) {
            Platillo platillo = new Platillo(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3));
            platillo.setPreparado(isPlatilloPreparado(platillo.getIdPlatillo()));
            lista.add(platillo);
        }
        c.close();
        return lista;
    }

    public Platillo obtenerPlatillo(int idPlatillo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM platillos WHERE id_platillo = ?", new String[]{String.valueOf(idPlatillo)});
        if (c.moveToFirst()) {
            Platillo p = new Platillo(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3));
            c.close();
            return p;
        }
        c.close();
        return null;
    }

    public List<Ingrediente> obtenerIngredientesPorDieta(int idDieta) {
        List<Ingrediente> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT i.id_ingrediente, i.nombre, i.consumido FROM ingredientes i INNER JOIN dieta_ingrediente di ON i.id_ingrediente = di.id_ingrediente WHERE di.id_dieta = ? ORDER BY i.nombre ASC", new String[]{String.valueOf(idDieta)});
        while (c.moveToNext()) {
            lista.add(new Ingrediente(c.getInt(0), c.getString(1), c.getInt(2)));
        }
        c.close();
        return lista;
    }

    public List<Ingrediente> obtenerIngredientesDePlatillo(int idPlatillo) {
        List<Ingrediente> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT ingredientes.id_ingrediente, ingredientes.nombre, ingredientes.consumido FROM ingredientes INNER JOIN platillo_ingrediente ON ingredientes.id_ingrediente = platillo_ingrediente.id_ingrediente WHERE platillo_ingrediente.id_platillo = ?", new String[]{String.valueOf(idPlatillo)});
        while (c.moveToNext()) {
            lista.add(new Ingrediente(c.getInt(0), c.getString(1), c.getInt(2)));
        }
        c.close();
        return lista;
    }

    public List<Ingrediente> obtenerIngredientesConsumidos() {
        List<Ingrediente> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM ingredientes WHERE consumido = 1 ORDER BY nombre ASC", null);
        while (c.moveToNext()) {
            lista.add(new Ingrediente(c.getInt(0), c.getString(1), c.getInt(2)));
        }
        c.close();
        return lista;
    }

    public void marcarIngredienteConsumido(int idIngrediente) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("consumido", 1);
        db.update("ingredientes", cv, "id_ingrediente = ?", new String[]{String.valueOf(idIngrediente)});
    }

    public void reiniciarIngredientesConsumidos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ingredientes SET consumido = 0");
    }

    public void agregarIngredienteAPlatillo(int idPlatillo, int idIngrediente) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id_platillo", idPlatillo);
        cv.put("id_ingrediente", idIngrediente);
        db.insert("platillo_ingrediente", null, cv);
    }

    public void guardarDietaElegida(int idDieta) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM progreso_semana");
        ContentValues cv = new ContentValues();
        cv.put("dieta_elegida", idDieta);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        cv.put("fecha_inicio", sdf.format(new Date()));
        db.insert("progreso_semana", null, cv);
    }

    public int obtenerDietaGuardada() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT dieta_elegida FROM progreso_semana LIMIT 1", null);
        if (c.moveToFirst()) {
            int d = c.getInt(0);
            c.close();
            return d;
        }
        c.close();
        return -1;
    }

    public boolean semanaExpirada() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT fecha_inicio FROM progreso_semana LIMIT 1", null);
        if (c.moveToFirst()) {
            String fechaInicioStr = c.getString(0);
            c.close();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            try {
                Date fechaInicio = sdf.parse(fechaInicioStr);
                Calendar inicio = Calendar.getInstance();
                inicio.setTime(fechaInicio);
                inicio.add(Calendar.DAY_OF_YEAR, 7);
                return Calendar.getInstance().after(inicio);
            } catch (ParseException e) {
                return true;
            }
        }
        c.close();
        return true;
    }

    public void reiniciarSemana() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM progreso_semana");
    }

    private long _insertarIngrediente(SQLiteDatabase db, String nombre) {
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("consumido", 0);
        return db.insert("ingredientes", null, cv);
    }

    private void _insertarPlatillo(SQLiteDatabase db, int idDieta, String nombre, String tipo, long[] idsIngredientes) {
        ContentValues cvPlatillo = new ContentValues();
        cvPlatillo.put("id_dieta", idDieta);
        cvPlatillo.put("nombre", nombre);
        cvPlatillo.put("tipo_comida", tipo);
        long idPlatillo = db.insert("platillos", null, cvPlatillo);
        if (idPlatillo != -1) {
            for (long idIngrediente : idsIngredientes) {
                ContentValues cvRel = new ContentValues();
                cvRel.put("id_platillo", idPlatillo);
                cvRel.put("id_ingrediente", idIngrediente);
                db.insert("platillo_ingrediente", null, cvRel);
            }
        }
    }

    private void _asociarIngredienteDieta(SQLiteDatabase db, int idDieta, long idIngrediente) {
        ContentValues cv = new ContentValues();
        cv.put("id_dieta", idDieta);
        cv.put("id_ingrediente", idIngrediente);
        db.insert("dieta_ingrediente", null, cv);
    }

    private void insertarPlatillosEIngredientesIniciales(SQLiteDatabase db) {
        final int DIETA_KETO = 1, DIETA_VEGETARIANA = 2, DIETA_VEGANA = 3, DIETA_GLUTEN_FREE = 4, DIETA_CARNIVORA = 5;

        // Ingredientes
        long ingPechugaPollo = _insertarIngrediente(db, "Pechuga de Pollo");
        long ingSalmon = _insertarIngrediente(db, "Salmón");
        long ingHuevo = _insertarIngrediente(db, "Huevo");
        long ingCarneRes = _insertarIngrediente(db, "Carne de Res");
        long ingLentejas = _insertarIngrediente(db, "Lentejas");
        long ingGarbanzos = _insertarIngrediente(db, "Garbanzos");
        long ingTofu = _insertarIngrediente(db, "Tofu");
        long ingTocino = _insertarIngrediente(db, "Tocino");
        long ingChuletaCerdo = _insertarIngrediente(db, "Chuleta de Cerdo");
        long ingAtun = _insertarIngrediente(db, "Atún en lata");
        long ingLechuga = _insertarIngrediente(db, "Lechuga");
        long ingTomate = _insertarIngrediente(db, "Tomate");
        long ingCebolla = _insertarIngrediente(db, "Cebolla");
        long ingAguacate = _insertarIngrediente(db, "Aguacate");
        long ingEspinacas = _insertarIngrediente(db, "Espinacas");
        long ingBrocoli = _insertarIngrediente(db, "Brócoli");
        long ingChampinones = _insertarIngrediente(db, "Champiñones");
        long ingPimiento = _insertarIngrediente(db, "Pimiento");
        long ingPepino = _insertarIngrediente(db, "Pepino");
        long ingZanahoria = _insertarIngrediente(db, "Zanahoria");
        long ingAceiteOliva = _insertarIngrediente(db, "Aceite de Oliva");
        long ingQuesoCheddar = _insertarIngrediente(db, "Queso Cheddar");
        long ingQuesoCabra = _insertarIngrediente(db, "Queso de Cabra");
        long ingAlmendras = _insertarIngrediente(db, "Almendras");
        long ingNueces = _insertarIngrediente(db, "Nueces");
        long ingMantequilla = _insertarIngrediente(db, "Mantequilla");
        long ingCremaAgria = _insertarIngrediente(db, "Crema Agria");
        long ingArroz = _insertarIngrediente(db, "Arroz");
        long ingPanIntegral = _insertarIngrediente(db, "Pan Integral");
        long ingAvena = _insertarIngrediente(db, "Avena");
        long ingQuinoa = _insertarIngrediente(db, "Quinoa");
        long ingPanSinGluten = _insertarIngrediente(db, "Pan sin Gluten");
        long ingTortillasMaiz = _insertarIngrediente(db, "Tortillas de Maíz");
        long ingFresa = _insertarIngrediente(db, "Fresa");
        long ingArandano = _insertarIngrediente(db, "Arándano");
        long ingLimon = _insertarIngrediente(db, "Limón");
        long ingSal = _insertarIngrediente(db, "Sal");
        long ingPimienta = _insertarIngrediente(db, "Pimienta");
        long ingAjoPolvo = _insertarIngrediente(db, "Ajo en Polvo");
        long ingColiflor = _insertarIngrediente(db, "Coliflor");
        long ingYogurGriego = _insertarIngrediente(db, "Yogur Griego");
        long ingSemillasChia = _insertarIngrediente(db, "Semillas de Chía");
        long ingCostillasRes = _insertarIngrediente(db, "Costillas de Res");
        long ingAceiteCoco = _insertarIngrediente(db, "Aceite de Coco");
        long ingHarinaAlmendras = _insertarIngrediente(db, "Harina de Almendras");
        long ingLecheAlmendras = _insertarIngrediente(db, "Leche de Almendras");
        long ingProteinaPolvo = _insertarIngrediente(db, "Proteína en Polvo (Vegana)");
        long ingPastaSinGluten = _insertarIngrediente(db, "Pasta sin Gluten");
        long ingCamarones = _insertarIngrediente(db, "Camarones");
        long ingMayonesa = _insertarIngrediente(db, "Mayonesa");
        long ingMostaza = _insertarIngrediente(db, "Mostaza");
        long ingMiel = _insertarIngrediente(db, "Miel");
        long ingFrijoles = _insertarIngrediente(db, "Frijoles");
        long ingBerenjena = _insertarIngrediente(db, "Berenjena");
        long ingSalsaSoja = _insertarIngrediente(db, "Salsa de Soya");



        // Asociaciones Dieta-Ingrediente
        long[] ketoIng = {ingPechugaPollo, ingSalmon, ingHuevo, ingCarneRes, ingTocino, ingChuletaCerdo, ingAtun, ingLechuga, ingTomate, ingCebolla, ingAguacate, ingEspinacas, ingBrocoli, ingChampinones, ingPimiento, ingPepino, ingAceiteOliva, ingQuesoCheddar, ingQuesoCabra, ingAlmendras, ingNueces, ingMantequilla, ingCremaAgria, ingFresa, ingArandano, ingLimon, ingSal, ingPimienta, ingAjoPolvo, ingColiflor, ingYogurGriego, ingSemillasChia, ingAceiteCoco, ingHarinaAlmendras, ingLecheAlmendras, ingCamarones, ingMayonesa, ingMostaza};
        long[] vegIng = {ingHuevo, ingLentejas, ingGarbanzos, ingTofu, ingLechuga, ingTomate, ingCebolla, ingAguacate, ingEspinacas, ingBrocoli, ingChampinones, ingPimiento, ingPepino, ingZanahoria, ingAceiteOliva, ingQuesoCheddar, ingQuesoCabra, ingAlmendras, ingNueces, ingMantequilla, ingCremaAgria, ingArroz, ingPanIntegral, ingAvena, ingQuinoa, ingPanSinGluten, ingTortillasMaiz, ingFresa, ingArandano, ingLimon, ingSal, ingPimienta, ingAjoPolvo, ingColiflor, ingYogurGriego, ingSemillasChia, ingAceiteCoco, ingHarinaAlmendras, ingLecheAlmendras, ingProteinaPolvo, ingPastaSinGluten, ingMayonesa, ingMostaza};
        long[] veganIng = {ingLentejas, ingGarbanzos, ingTofu, ingLechuga, ingTomate, ingCebolla, ingAguacate, ingEspinacas, ingBrocoli, ingChampinones, ingPimiento, ingPepino, ingZanahoria, ingAceiteOliva, ingAlmendras, ingNueces, ingArroz, ingPanIntegral, ingAvena, ingQuinoa, ingPanSinGluten, ingTortillasMaiz, ingFresa, ingArandano, ingLimon, ingSal, ingPimienta, ingAjoPolvo, ingColiflor, ingSemillasChia, ingAceiteCoco, ingHarinaAlmendras, ingLecheAlmendras, ingProteinaPolvo, ingPastaSinGluten, ingMostaza};
        long[] glutenFreeIng = {ingPechugaPollo, ingSalmon, ingHuevo, ingCarneRes, ingLentejas, ingGarbanzos, ingTofu, ingTocino, ingChuletaCerdo, ingAtun, ingLechuga, ingTomate, ingCebolla, ingAguacate, ingEspinacas, ingBrocoli, ingChampinones, ingPimiento, ingPepino, ingZanahoria, ingAceiteOliva, ingQuesoCheddar, ingQuesoCabra, ingAlmendras, ingNueces, ingMantequilla, ingCremaAgria, ingArroz, ingAvena, ingQuinoa, ingPanSinGluten, ingTortillasMaiz, ingFresa, ingArandano, ingLimon, ingSal, ingPimienta, ingAjoPolvo, ingColiflor, ingYogurGriego, ingSemillasChia, ingCostillasRes, ingAceiteCoco, ingHarinaAlmendras, ingLecheAlmendras, ingProteinaPolvo, ingPastaSinGluten, ingCamarones, ingMayonesa, ingMostaza};
        long[] carnivoreIng = {ingPechugaPollo, ingSalmon, ingHuevo, ingCarneRes, ingTocino, ingChuletaCerdo, ingAtun, ingMantequilla, ingQuesoCheddar, ingSal, ingPimienta, ingAjoPolvo, ingCostillasRes, ingCamarones, ingMayonesa, ingMostaza};

        for(long id : ketoIng) _asociarIngredienteDieta(db, DIETA_KETO, id);
        for(long id : vegIng) _asociarIngredienteDieta(db, DIETA_VEGETARIANA, id);
        for(long id : veganIng) _asociarIngredienteDieta(db, DIETA_VEGANA, id);
        for(long id : glutenFreeIng) _asociarIngredienteDieta(db, DIETA_GLUTEN_FREE, id);
        for(long id : carnivoreIng) _asociarIngredienteDieta(db, DIETA_CARNIVORA, id);

        // Platillos
        _insertarPlatillo(db, DIETA_KETO, "Huevos revueltos con tocino", "Desayuno", new long[]{ingHuevo, ingTocino, ingAguacate, ingQuesoCheddar});
        _insertarPlatillo(db, DIETA_KETO, "Ensalada Cobb", "Almuerzo", new long[]{ingPechugaPollo, ingLechuga, ingTomate, ingHuevo, ingTocino, ingAguacate, ingQuesoCabra});
        _insertarPlatillo(db, DIETA_KETO, "Salmón al horno con brócoli", "Comida", new long[]{ingSalmon, ingBrocoli, ingAceiteOliva, ingLimon, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_KETO, "Rollitos de Pollo y Queso", "Colación", new long[]{ingPechugaPollo, ingQuesoCheddar, ingPepino, ingPimiento});
        _insertarPlatillo(db, DIETA_KETO, "Pollo cremoso con espinacas", "Cena", new long[]{ingPechugaPollo, ingEspinacas, ingCremaAgria, ingAjoPolvo, ingQuesoCheddar});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Avena con fresas y almendras", "Desayuno", new long[]{ingAvena, ingFresa, ingAlmendras, ingNueces});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Ensalada de garbanzos", "Almuerzo", new long[]{ingGarbanzos, ingLechuga, ingTomate, ingPepino, ingCebolla, ingAceiteOliva, ingLimon});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Lentejas estofadas con verduras", "Comida", new long[]{ingLentejas, ingZanahoria, ingCebolla, ingPimiento, ingAjoPolvo, ingTomate});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Tostada de aguacate", "Colación", new long[]{ingPanIntegral, ingAguacate, ingLimon, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Tofu salteado con brócoli", "Cena", new long[]{ingTofu, ingBrocoli, ingPimiento, ingAceiteOliva, ingAjoPolvo});
        _insertarPlatillo(db, DIETA_VEGANA, "Batido de espinacas y arándanos", "Desayuno", new long[]{ingEspinacas, ingArandano, ingLecheAlmendras, ingAvena});
        _insertarPlatillo(db, DIETA_VEGANA, "Tazón de quinoa con vegetales", "Almuerzo", new long[]{ingQuinoa, ingBrocoli, ingGarbanzos, ingPimiento, ingAguacate, ingLimon});
        _insertarPlatillo(db, DIETA_VEGANA, "Chili vegano", "Comida", new long[]{ingLentejas, ingGarbanzos, ingTomate, ingCebolla, ingPimiento, ingAjoPolvo, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_VEGANA, "Zanahorias con hummus deconstruido", "Colación", new long[]{ingZanahoria, ingGarbanzos, ingLimon, ingAceiteOliva});
        _insertarPlatillo(db, DIETA_VEGANA, "Tacos de lentejas", "Cena", new long[]{ingLentejas, ingTortillasMaiz, ingLechuga, ingTomate, ingCebolla, ingAguacate});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Omelette de espinacas y queso", "Desayuno", new long[]{ingHuevo, ingEspinacas, ingQuesoCheddar, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Pollo a la plancha con quinoa", "Almuerzo", new long[]{ingPechugaPollo, ingQuinoa, ingBrocoli, ingAceiteOliva});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Salmón con ensalada fresca", "Comida", new long[]{ingSalmon, ingLechuga, ingPepino, ingTomate, ingLimon});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Tostada sin gluten con aguacate", "Colación", new long[]{ingPanSinGluten, ingAguacate, ingTomate, ingSal});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Arroz con pollo y vegetales", "Cena", new long[]{ingArroz, ingPechugaPollo, ingPimiento, ingCebolla, ingZanahoria});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Bistec con huevos fritos", "Desayuno", new long[]{ingCarneRes, ingHuevo, ingMantequilla, ingSal});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Hamburguesas de res y tocino", "Almuerzo", new long[]{ingCarneRes, ingTocino, ingQuesoCheddar, ingCebolla});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Chuletas de cerdo en mantequilla", "Comida", new long[]{ingChuletaCerdo, ingMantequilla, ingAjoPolvo, ingSal});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Ensalada de atún", "Colación", new long[]{ingAtun, ingMayonesa, ingHuevo, ingCebolla});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Salmón a la plancha con tocino", "Cena", new long[]{ingSalmon, ingTocino, ingLimon, ingMantequilla, ingSal});
        _insertarPlatillo(db, DIETA_KETO, "Pancakes de Harina de Almendras", "Desayuno", new long[]{ingHarinaAlmendras, ingHuevo, ingMantequilla, ingFresa});
        _insertarPlatillo(db, DIETA_KETO, "Yogur con Nueces y Chía", "Desayuno", new long[]{ingYogurGriego, ingNueces, ingSemillasChia, ingArandano});
        _insertarPlatillo(db, DIETA_KETO, "Pizza de base de Coliflor", "Almuerzo", new long[]{ingColiflor, ingQuesoCheddar, ingPimiento, ingChampinones, ingTomate});
        _insertarPlatillo(db, DIETA_KETO, "Camarones al Ajillo", "Almuerzo", new long[]{ingCamarones, ingAjoPolvo, ingMantequilla, ingLimon, ingAceiteOliva});
        _insertarPlatillo(db, DIETA_KETO, "Lasaña de Calabacín", "Comida", new long[]{ingCarneRes, ingQuesoCabra, ingTomate, ingCebolla, ingQuesoCheddar});
        _insertarPlatillo(db, DIETA_KETO, "Pimientos Rellenos de Carne", "Comida", new long[]{ingPimiento, ingCarneRes, ingCebolla, ingQuesoCheddar});
        _insertarPlatillo(db, DIETA_KETO, "Bombones de Grasa", "Colación", new long[]{ingAceiteCoco, ingMantequilla, ingHarinaAlmendras, ingNueces});
        _insertarPlatillo(db, DIETA_KETO, "Tiras de apio con queso crema", "Colación", new long[]{ingQuesoCabra, ingPimienta, ingSal});
        _insertarPlatillo(db, DIETA_KETO, "Sopa de brócoli y cheddar", "Cena", new long[]{ingBrocoli, ingQuesoCheddar, ingCremaAgria, ingCebolla});
        _insertarPlatillo(db, DIETA_KETO, "Tacos de lechuga con atún", "Cena", new long[]{ingLechuga, ingAtun, ingMayonesa, ingCebolla, ingTomate});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Tostadas francesas", "Desayuno", new long[]{ingPanIntegral, ingHuevo, ingMantequilla, ingFresa, ingMiel});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Huevos a la mexicana", "Desayuno", new long[]{ingHuevo, ingTomate, ingCebolla, ingPimiento, ingSal});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Wrap de Hummus y Vegetales", "Almuerzo", new long[]{ingGarbanzos, ingTortillasMaiz, ingLechuga, ingZanahoria, ingPepino});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Sándwich de queso a la parrilla", "Almuerzo", new long[]{ingPanIntegral, ingQuesoCheddar, ingMantequilla, ingTomate});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Pasta con champiñones", "Comida", new long[]{ingPastaSinGluten, ingChampinones, ingCremaAgria, ingAjoPolvo});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Hamburguesa de lentejas", "Comida", new long[]{ingLentejas, ingPanIntegral, ingCebolla, ingAjoPolvo, ingLechuga});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Yogur griego con miel", "Colación", new long[]{ingYogurGriego, ingMiel, ingNueces});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Manzana con mantequilla de maní", "Colación", new long[]{ingMantequilla, ingSal});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Quesadillas de Frijoles", "Cena", new long[]{ingTortillasMaiz, ingFrijoles, ingQuesoCheddar});
        _insertarPlatillo(db, DIETA_VEGETARIANA, "Pizza margarita", "Cena", new long[]{ingPanIntegral, ingTomate, ingQuesoCabra, ingAceiteOliva});
        _insertarPlatillo(db, DIETA_VEGANA, "Pudín de Chía", "Desayuno", new long[]{ingSemillasChia, ingLecheAlmendras, ingFresa, ingArandano});
        _insertarPlatillo(db, DIETA_VEGANA, "Tostada con aguacate y tomate", "Desayuno", new long[]{ingPanIntegral, ingAguacate, ingTomate, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_VEGANA, "Sopa de lentejas y zanahoria", "Almuerzo", new long[]{ingLentejas, ingZanahoria, ingCebolla, ingTomate});
        _insertarPlatillo(db, DIETA_VEGANA, "Ensalada de quinoa y frijoles negros", "Almuerzo", new long[]{ingQuinoa, ingFrijoles, ingPimiento, ingCebolla, ingLimon});
        _insertarPlatillo(db, DIETA_VEGANA, "Curry de Garbanzos", "Comida", new long[]{ingGarbanzos, ingAceiteCoco, ingEspinacas, ingCebolla, ingArroz});
        _insertarPlatillo(db, DIETA_VEGANA, "Tofu al horno con brócoli", "Comida", new long[]{ingTofu, ingBrocoli, ingAceiteOliva, ingAjoPolvo, ingSalsaSoja});
        _insertarPlatillo(db, DIETA_VEGANA, "Frutos secos mixtos", "Colación", new long[]{ingAlmendras, ingNueces});
        _insertarPlatillo(db, DIETA_VEGANA, "Batido de Proteína Vegana", "Colación", new long[]{ingProteinaPolvo, ingLecheAlmendras, ingFresa});
        _insertarPlatillo(db, DIETA_VEGANA, "Berenjena a la parmesana (sin queso)", "Cena", new long[]{ingBerenjena, ingTomate, ingAjoPolvo, ingPanIntegral});
        _insertarPlatillo(db, DIETA_VEGANA, "Salteado de vegetales", "Cena", new long[]{ingBrocoli, ingPimiento, ingZanahoria, ingCebolla, ingSalsaSoja});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Batido de Proteínas y Frutas", "Desayuno", new long[]{ingProteinaPolvo, ingLecheAlmendras, ingFresa, ingArandano});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Yogur con frutas y nueces", "Desayuno", new long[]{ingYogurGriego, ingFresa, ingNueces, ingMiel});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Tacos de pollo", "Almuerzo", new long[]{ingPechugaPollo, ingTortillasMaiz, ingAguacate, ingTomate, ingCebolla});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Sopa de pollo y vegetales", "Almuerzo", new long[]{ingPechugaPollo, ingZanahoria, ingCebolla, ingSal});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Pasta sin gluten a la boloñesa", "Comida", new long[]{ingPastaSinGluten, ingCarneRes, ingTomate, ingCebolla});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Pescado empapelado con verduras", "Comida", new long[]{ingSalmon, ingPimiento, ingZanahoria, ingLimon});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Rodajas de pepino con queso", "Colación", new long[]{ingPepino, ingQuesoCabra});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Chips de Manzana al Horno", "Colación", new long[]{ingMiel});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Camarones al coco", "Cena", new long[]{ingCamarones, ingAceiteCoco, ingHuevo, ingHarinaAlmendras});
        _insertarPlatillo(db, DIETA_GLUTEN_FREE, "Pizza con base de quinoa", "Cena", new long[]{ingQuinoa, ingTomate, ingQuesoCheddar, ingPimiento});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Chicharrón de cerdo", "Desayuno", new long[]{ingChuletaCerdo, ingSal});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Revoltillo con carne molida", "Desayuno", new long[]{ingHuevo, ingCarneRes, ingQuesoCheddar});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Alitas de pollo fritas", "Almuerzo", new long[]{ingPechugaPollo, ingMantequilla, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Costillas de res al horno", "Almuerzo", new long[]{ingCostillasRes, ingSal, ingPimienta});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Filete de Salmón y Camarones", "Comida", new long[]{ingSalmon, ingCamarones, ingMantequilla, ingLimon});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Caldo de huesos", "Comida", new long[]{ingCostillasRes, ingSal});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Tiras de cecina", "Colación", new long[]{ingCarneRes, ingSal});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Rollitos de tocino", "Colación", new long[]{ingTocino});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Pechuga de pavo al horno", "Cena", new long[]{ingPechugaPollo, ingMantequilla, ingSal});
        _insertarPlatillo(db, DIETA_CARNIVORA, "Sardinas en lata con limón", "Cena", new long[]{ingAtun, ingLimon, ingSal});
    }
}
