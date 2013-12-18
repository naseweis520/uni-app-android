package com.st.cs.unisaarland.SaarlandUniversityApp.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.st.cs.unisaarland.SaarlandUniversityApp.bus.model.PointOfInterest;

import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/2/13
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    private SQLiteDatabase database = null;
    private Context context = null;
    private final String DATABASE_NAME = "pointOfInterest.sqlite3";
    private final String DB_PATH = "/data/data/com.st.cs.unisaarland.SaarlandUniversityApp/databases/";


    public ArrayList<PointOfInterest> getPointsOfInterestForCategoryWithID(int ID){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null ;
        try{
            cursor =database.query("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                    "canshowrightcallout","color","website","lat","longi","ID"},"categorieID = ?",
                    new String[]{Integer.toString(ID)},null,null,null);
            if(cursor!=null)
            {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1)) ;
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    result.add(poi);
                }
                cursor.close();
            }
        }catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<String> getAllCategoryTitles(){
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = null ;
        try{
            cursor =database.query("categorie",new String[]{"title"},null,null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestPartialMatched(){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null ;
        try{
            cursor =database.query("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                    "canshowrightcallout","color","website","lat","longi","ID","categorieID"},null,
                    null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1)) ;
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    poi.setCategoryID(cursor.getInt(9));
                    result.add(poi);
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<String> getPointsOfInterestPartialMatchedTitles(){
        ArrayList<String> result = new ArrayList<String>();
        Cursor cursor = null ;
        try{
            cursor =database.query("pointOfInterest",new String[]{"title"},null,
                    null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getString(0));
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestPartialMatchedForSearchKey(String searchKey){
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null ;
        String sKeyWithPercAtEnd = searchKey + "%";

        String sKeyWithPerAtBegEnd = "% " + searchKey + "%";

        try{
            cursor =database.query("pointOfInterest",new String[]{"title","subtitle","canshowleftcallout",
                    "canshowrightcallout","color","website","lat","longi","ID","categorieID"},
                    "(title LIKE ?) OR (subtitle LIKE ?)  OR (searchkey LIKE ?) OR ( title LIKE ? ) OR (subtitle LIKE ?)" +
                            "  OR (searchkey LIKE ?)",
                    new String[]{sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPercAtEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd,sKeyWithPerAtBegEnd},null,null,"title ASC");
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1)) ;
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    poi.setCategoryID(cursor.getInt(9));
                    result.add(poi);
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForIDs(ArrayList<Integer> ids) {
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null;
        if (ids.size() >= 1) {
            for (int i = 0; i < ids.size(); i++) {
                String idList = String.format("%d", ids.get(i));
                //            for (int i = 1; i<IDs.count; i++) {
                //                idList = [idList stringByAppendingString:[NSString stringWithFormat:@", %d",[[IDs objectAtIndex:i] integerValue]]];
                //            }
                try {
                    cursor = database.query("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                            "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "ID = ?",
                            new String[]{idList}, null, null, null);

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            PointOfInterest poi = new PointOfInterest();
                            poi.setTitle(cursor.getString(0));
                            poi.setSubtitle(cursor.getString(1));
                            poi.setCanShowLeftCallOut(cursor.getInt(2));
                            poi.setCanShowRightCallOut(cursor.getInt(3));
                            poi.setColor(cursor.getInt(4));
                            poi.setWebsite(cursor.getString(5));
                            poi.setLatitude(cursor.getFloat(6));
                            poi.setLongitude(cursor.getFloat(7));
                            poi.setID(cursor.getInt(8));
                            result.add(poi);
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e("MyTag", e.getMessage());
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    public ArrayList<PointOfInterest> getPointsOfInterestForTitle(String title) {
        ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
        Cursor cursor = null;
        try {
            cursor = database.query("pointOfInterest", new String[]{"title", "subtitle", "canshowleftcallout",
                    "canshowrightcallout", "color", "website", "lat", "longi", "ID"}, "title = ? ",new String[]{title}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    PointOfInterest poi = new PointOfInterest();
                    poi.setTitle(cursor.getString(0));
                    poi.setSubtitle(cursor.getString(1));
                    poi.setCanShowLeftCallOut(cursor.getInt(2));
                    poi.setCanShowRightCallOut(cursor.getInt(3));
                    poi.setColor(cursor.getInt(4));
                    poi.setWebsite(cursor.getString(5));
                    poi.setLatitude(cursor.getFloat(6));
                    poi.setLongitude(cursor.getFloat(7));
                    poi.setID(cursor.getInt(8));
                    result.add(poi);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("MyTag", e.getMessage());
            return null;
        }
        return result;
    }

    public ArrayList<Integer> getAllCategoryIDs(){
        ArrayList<Integer> result = new ArrayList<Integer>();
        Cursor cursor = null ;
        try{
            cursor =database.query("categorie",new String[]{"ID"},null,null,null,null,null);
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    result.add(cursor.getInt(0));
                }
                cursor.close();
            }
        } catch (Exception e){
            Log.e("MyTag",e.getMessage());
            return null;
        }
        return result;
    }

    public void crateDatabase() throws IOException {
        boolean vtVarMi = isDatabaseExist();

        if (!vtVarMi) {
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyFileOrDir(String path) {
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = "/data/data/" + context.getPackageName() + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + context.getPackageName() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = context.getAssets().open(DATABASE_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DATABASE_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private boolean isDatabaseExist() {
        SQLiteDatabase control = null;

        try {
            String myPath = DB_PATH + DATABASE_NAME;
            control = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            control = null;
        }

        if (control != null) {
            control.close();
        }
        return control != null ? true : false;
    }

    private boolean openDB(){
        if (database != null && database.isOpen() ) {
            return true;
        }
        else{
            try{
                database =SQLiteDatabase.openDatabase(DB_PATH+DATABASE_NAME, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                        //SQLiteDatabase.NO_LOCALIZED_COLLATORS
            }catch (Exception e){
                Log.e("MyTag",e.getMessage());
                return false;
            }
        }
        return true;
    }

    public DatabaseHandler(Context context) {
        super(context,"pointOfInterest.sqlite3",null,1);
        this.context = context;
//        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
//        boolean isCopied = settings.getBoolean("assetsCopied",false);
//        if(!isCopied){
//            copyFileOrDir("OverlayTiles");
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putBoolean("assetsCopied", true);
//            editor.commit();
//        }
        try {
            crateDatabase();
            openDB();
        } catch (IOException e) {
            Log.e("MyTag",e.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
