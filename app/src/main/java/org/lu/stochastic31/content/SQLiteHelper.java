package org.lu.stochastic31.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static SQLiteHelper instance;
    private static int VERSION = 4;
    private static String NAME = "stochastic31.db";

    private String[] strCreateSQLs = {
            "create table if not exists stochastic_name (_id integer not null primary key autoincrement, name text not null);",
            "create table if not exists stochastic_random (_id integer not null primary key autoincrement, random text not null, name_index integer not null);",};
    private String[] strDropSQLs = {"drop table if exists stochastic_name;",
            "drop table if exists stochastic_random;",};
    private String[] strClearSQLs = {
            "delete from stochastic_name; select * from sqlite_sequence;update sqlite_sequence set seq=0 where name='stochastic_name';",
            "delete from stochastic_random; select * from sqlite_sequence;update sqlite_sequence set seq=0 where name='stochastic_random';",};

    public static SQLiteHelper getInstance(Context context) {
        if (instance == null)
            instance = new SQLiteHelper(context);
        return instance;
    }

    private SQLiteHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String strCreateSQL : strCreateSQLs)
            db.execSQL(strCreateSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String strDropSQL : strDropSQLs)
            db.execSQL(strDropSQL);
        onCreate(db);
    }

    public void clearDataBase() {
        SQLiteDatabase db = getWritableDatabase();
        for (String strDropSQL : strClearSQLs)
            db.execSQL(strDropSQL);
        db.close();
    }

    public Cursor getNames() {
        return getReadableDatabase().query(true, "stochastic_name",
                new String[]{"_id", "name"}, null, null, null, null, null,
                null);
    }

    public String getName(long name_index) {
        String name = "";
        Cursor c = getReadableDatabase().query(true, "stochastic_name",
                new String[]{"_id", "name"}, "_id=?",
                new String[]{String.valueOf(name_index)}, null, null, null,
                null);
        if (c != null) {
            if (c.moveToFirst())
                name = c.getString(1);
            c.close();
        }
        return name;
    }

    public long getNameIndex(String name) {
        long name_index = 0;
        Cursor c = getReadableDatabase().query(true, "stochastic_name",
                new String[]{"_id", "name"}, "name=?",
                new String[]{name}, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst())
                name_index = c.getLong(0);
            c.close();
        }
        return name_index;
    }

    public Cursor getRandoms(long name_index) {
        return getReadableDatabase().query(true, "stochastic_random",
                new String[]{"_id", "random", "name_index"}, "name_index=?",
                new String[]{String.valueOf(name_index)}, null, null, null,
                null);
    }

    public long insertName(String filename) {
        if (filename == null || filename.trim().length() == 0)
            return 0;
        ContentValues values = new ContentValues();
        values.put("name", filename);
        return getWritableDatabase().insert("stochastic_name", null, values);
    }

    public long insertRandom(long name_index, String random) {
        if (random == null || random.trim().length() == 0)
            return 0;
        ContentValues values = new ContentValues();
        values.put("name_index", name_index);
        values.put("random", random);
        return getWritableDatabase().insert("stochastic_random", null, values);
    }

    public int deleteName(int id) {
        return getWritableDatabase().delete("stochastic_name", "_id=?",
                new String[]{String.valueOf(id)});
    }

    public int deleteName(String name) {
        return getWritableDatabase().delete("stochastic_name", "name=?",
                new String[]{name});
    }

    public int deleteRandoms(long name_index) {
        return getWritableDatabase().delete("stochastic_random",
                "name_index=?", new String[]{String.valueOf(name_index)});
    }

    public int deleteRandom(long name_index, String random) {
        return getWritableDatabase().delete("stochastic_random",
                "name_index=? and random=?",
                new String[]{String.valueOf(name_index), random});
    }

    public int deleteRandom(long id) {
        return getWritableDatabase().delete("stochastic_random", "_id=?",
                new String[]{String.valueOf(id)});
    }

    /**
     * 读取外部存储中指定目录(参见{@link FileHelper#DIR})中文件列表，以更新应用数据库中文件名称表。
     */
    public void importFromFile() {
        for (String title : FileHelper.listTitles()) {
            long name_index = getNameIndex(title);
            if (name_index > 0)// 已经插入的不再重新处理
                continue;
            insertName(title);
            importFromFile(title);
        }
    }

    /**
     * Be careful! Only use this method when the content is not loaded And the
     * user asked to clear the custom data of this random!
     *
     * @param title the title of the file to read
     */
    public void importFromFile(String title) {
        long name_index = getNameIndex(title);
        String filename = title + FileHelper.END_SUFFIX;
        try {
            String random;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(FileHelper.getFileInstance(filename,
                            true)), "utf-8"));
            while ((random = br.readLine()) != null) {
                random = random.trim();
                if (random.length() > 0)
                    insertRandom(name_index, random);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
