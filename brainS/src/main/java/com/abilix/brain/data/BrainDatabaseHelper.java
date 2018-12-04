package com.abilix.brain.data;

import com.abilix.brain.utils.LogMgr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * 数据库工具类，现有文件传输表，页面顺序表两张表。
 */
public class BrainDatabaseHelper extends SQLiteOpenHelper {

    //文件传输的四种状态
    public static final int TRANSFERRING_FILE_STATE_NOT_EXIST = 0;
    public static final int TRANSFERRING_FILE_STATE_EXIST_DIFFERENT_CRC = 1;
    public static final int TRANSFERRING_FILE_STATE_EXIST_SAME_CRC = 2;
    public static final int TRANSFERRING_FILE_STATE_UNKNOWN = 3;

    //页面类型
    public static final int PAGE_TYPE_UNKNOWN = -1;
    public static final int PAGE_TYPE_PROJECT_PROGRAM = 0;
    public static final int PAGE_TYPE_CHART = 1;
    public static final int PAGE_TYPE_SCRATCH = 2;
    public static final int PAGE_TYPE_APK = 6;

    public static final int MOST_INVALID_ROW_NUMBER = 100;

    /**
     * 唯一数据库名
     */
    private static final String BRAIN_DATABASE_NAME = "brain_database";

    /**
     * 续传文件表 表名 记录断点续传中未传完文件信息
     */
    public static final String TABLE_NAME_TRANSFERRING_FILE = "transferring_file";
    /**
     * 续传文件表 列名 id
     */
    private static final String COLUMN_FILE_ID = "file_id";
    /**
     * 续传文件表 列名 路径
     */
    private static final String COLUMN_FILE_PATH = "filepath";
    /**
     * 续传文件表 列名 crc
     */
    private static final String COLUMN_FILE_CRC = "file_crc";
    /**
     * 创建续传文件表sql语句
     */
    private static final String CREATE_TABLE_BROKEN_POINT_CONTINUINGLY_TRANSFERRING_FILE = "create table " + TABLE_NAME_TRANSFERRING_FILE + " (" +
            COLUMN_FILE_ID + " integer primary key autoincrement, " +
            COLUMN_FILE_PATH + " text not null unique , " +
            COLUMN_FILE_CRC + " integer not null)";

	/**scratch，chart，项目编程文件,APP页面 排序信息存储表*/
	private static final String TABLE_NAME_PAGE_SORT_INFO = "table_file_info";
	/**文件信息表 列名 id*/
	private static final String COLUMN_PAGE_ID_IN_TABLE_PAGE_SORT_INFO = "page_id_in_page_sort";
	/**文件信息表 列名 页面名，页面是文件时为文件绝对路径，是APK时为应用包名*/
	private static final String COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO = "page_name_in_page_sort";
	/**文件信息表 列名 页面类型*/
	private static final String COLUMN_PAGE_TYPE_IN_TABLE_PAGE_SORT_INFO = "page_type_in_page_sort";
	/**创建文件信息表sql语句*/
	private static final String CREATE_TABLE_FILE_INFO = "create table "+ TABLE_NAME_PAGE_SORT_INFO +" (" +
            COLUMN_PAGE_ID_IN_TABLE_PAGE_SORT_INFO +" integer primary key autoincrement, " +
            COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO +" text not null unique COLLATE NOCASE, " +
            COLUMN_PAGE_TYPE_IN_TABLE_PAGE_SORT_INFO + " integer not null)";

    /**
     * 数据库版本号
     */
    private static final int BRAIN_DATABASE_VERSION = 2;

    public BrainDatabaseHelper(Context context) {
        this(context, BRAIN_DATABASE_NAME, null, BRAIN_DATABASE_VERSION);
    }

    private BrainDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogMgr.i("BrainDatabaseHelper onCreate()");
        db.execSQL(CREATE_TABLE_BROKEN_POINT_CONTINUINGLY_TRANSFERRING_FILE);
		db.execSQL(CREATE_TABLE_FILE_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogMgr.i("BrainDatabaseHelper onUpgrade()");
		switch (oldVersion) {
		case 1:
			db.execSQL(CREATE_TABLE_FILE_INFO);
		default:
		}
    }

    /**
     * 存储未传完文件的文件信息
     *
     * @param db       数据库对象
     * @param filePath 文件路径
     * @param crc      文件crc
     * @return 存储结果
     */
    public synchronized boolean insertTransferringFileInfo(SQLiteDatabase db, String filePath, int crc) {
        LogMgr.i("insertTransferringFileInfo() filePath = " + filePath + " crc = " + crc);
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_FILE_CRC, crc);
        long result = db.insert(TABLE_NAME_TRANSFERRING_FILE, null, values);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除未传完文件的文件信息
     *
     * @param db
     * @param filePath
     * @return 删除结果
     */
    public synchronized boolean deleteTransferringFileInfo(SQLiteDatabase db, String filePath) {
        LogMgr.i("deleteTransferringFileInfo() filePath = " + filePath);
        int result = db.delete(TABLE_NAME_TRANSFERRING_FILE, COLUMN_FILE_PATH + " = ?", new String[]{filePath});
        if (result == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询未传完文件的文件信息是否存在
     *
     * @param db
     * @param filePath
     * @param crc
     * @return 文件是否存在的状态
     * <p>不存在, TRANSFERRING_FILE_STATE_NOT_EXIST = 0;
     * <p>存在，但crc值不同 TRANSFERRING_FILE_STATE_EXIST_DIFFERENT_CRC = 1;
     * <p>存在，crc值相同 TRANSFERRING_FILE_STATE_EXIST_SAME_CRC = 2;
     * <p>未知状态,TRANSFERRING_FILE_STATE_UNKNOWN = 3;
     */
    public synchronized int queryTransferringFileInfo(SQLiteDatabase db, String filePath, int crc) {
        LogMgr.i("queryTransferringFileInfo() filePath = " + filePath + " crc = " + crc);
        int result;
        Cursor cursor = db.query(TABLE_NAME_TRANSFERRING_FILE, null,
                COLUMN_FILE_PATH + " = ?", new String[]{filePath}, null, null, null);
        if (cursor.getCount() == 0) {
            LogMgr.i("queryTransferringFileInfo() 不存在 filePath = " + filePath);
            result = TRANSFERRING_FILE_STATE_NOT_EXIST;
        } else if (cursor.getCount() == 1) {
            if (cursor.moveToFirst()) {
                int crc_db = cursor.getInt(cursor.getColumnIndex(COLUMN_FILE_CRC));
                if (crc_db == crc) {
                    LogMgr.i("queryTransferringFileInfo() 存在相同crc的 filePath = " + filePath);
                    result = TRANSFERRING_FILE_STATE_EXIST_SAME_CRC;
                } else {
                    LogMgr.i("queryTransferringFileInfo() 存在不同crc的 filePath = " + filePath + " crc_db = " + crc_db);
                    result = TRANSFERRING_FILE_STATE_EXIST_DIFFERENT_CRC;
                }
            } else {
                LogMgr.e("queryTransferringFileInfo() 查询时异常1");
                result = TRANSFERRING_FILE_STATE_UNKNOWN;
            }
        } else {
            LogMgr.e("queryTransferringFileInfo() 查询时异常2");
            result = TRANSFERRING_FILE_STATE_UNKNOWN;
        }
        cursor.close();
        return result;
    }


    /**
     * 查询所有未传完文件的文件信息
     *
     * @param db
     */
    public synchronized void queryAllTransferringFileInfo(SQLiteDatabase db) {
        LogMgr.i("queryAllTransferringFileInfo()");
        Cursor cursor = db.query(TABLE_NAME_TRANSFERRING_FILE, null, null, null, null, null, null);
        LogMgr.d("queryAllTransferringFileInfo() 表中的行数 = " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                //遍历Cursor对象
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_FILE_ID));
                String filePath = cursor.getString(cursor.getColumnIndex(COLUMN_FILE_PATH));
                int crc = cursor.getInt(cursor.getColumnIndex(COLUMN_FILE_CRC));
                LogMgr.d("id = " + id + " filePath = " + filePath + " crc = " + crc);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

	/**
	 * 页面排序表存储页面信息
	 * @param db 数据库对象
	 * @param pageName 页面名
	 * @param pageType 页面类型
	 * @return 插入信息的rowID，如返回-1表示插入失败
	 */
	public synchronized long insertTablePageSortInfo(SQLiteDatabase db, String pageName, int pageType){
		LogMgr.i("insertTablePageSortInfo() pageName = "+pageName+" pageType = "+pageType);
		ContentValues values  = new ContentValues();
		values.put(COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO, pageName);
		values.put(COLUMN_PAGE_TYPE_IN_TABLE_PAGE_SORT_INFO, pageType);
        return db.insert(TABLE_NAME_PAGE_SORT_INFO, null, values);
	}
//	
//	/**
//	 * 文件信息表更新存储文件信息
//	 * @param db 数据库对象
//	 * @param filePath 文件路径
//	 * @param createTime 文件创建时间
//	 * @return 更新结果
//	 */
//	public synchronized boolean updateTableFileInfo(SQLiteDatabase db, String filePath, long createTime){
//		LogMgr.i("updateTableFileInfo() filePath = "+filePath+" createTime = "+createTime);
//		ContentValues values  = new ContentValues();
//		values.put(COLUMN_PAGE_TYPE_IN_TABLE_PAGE_SORT_INFO, createTime);
//		long result = db.update(TABLE_NAME_PAGE_SORT_INFO, values, COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO + " = ?", new String[]{filePath});
//		if(result == 1){
//			return true;
//		}else{
//			return false;
//		}
//	}
//	
	/**
	 * 删除页面排序表的页面信息
	 * @param db
	 * @param pageName
	 * @return 删除结果
	 */
	public synchronized boolean deleteTablePageSortInfo(SQLiteDatabase db, String pageName){
		LogMgr.i("deleteTablePageSortInfo() pageName = "+pageName);
		int result = db.delete(TABLE_NAME_PAGE_SORT_INFO, COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO + " = ?", new String[]{ pageName });
		if(result == 1){
			return true;
		}else{
			return false;
		}
	}
//	
	/**
	 * 查询页面排序表中页面信息是否存在
	 * @param db
	 * @param pageName
	 * @return 页面的ID，代表页面被创建的先后顺序。如果返回 -1，表示没有此页面的信息
	 */
	public synchronized long queryTablePageSortInfo(SQLiteDatabase db, @NonNull String pageName){
		LogMgr.i("queryTablePageSortInfo() pageName = "+pageName);
        long result = -1;

		Cursor cursor = db.query(TABLE_NAME_PAGE_SORT_INFO, null, COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO + " = ?", new String[]{ pageName }, null, null, null);
		if(cursor.getCount() == 0){
			LogMgr.i("queryTablePageSortInfo() 不存在 pageName = "+pageName);
			result = -1;
		}else if(cursor.getCount() == 1){
			LogMgr.i("queryTablePageSortInfo() 存在 pageName = "+pageName);
			if(cursor.moveToFirst()){
				result = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_ID_IN_TABLE_PAGE_SORT_INFO));
				LogMgr.i("queryTablePageSortInfo() 存在文件信息 = "+pageName + " result = "+result);
			}else{
				LogMgr.e("queryTablePageSortInfo() 查询时异常1");
				result = -1;
			}
		}else{
			LogMgr.e("queryTablePageSortInfo() 个数异常 cursor.getCount() = "+cursor.getCount());
			result = -1;
		}
		cursor.close();
        return result;
	}

    /**
     * 查询所有排序表中的排序信息
     *
     * @param db
     */
    public synchronized void queryAllPageSortInfo(SQLiteDatabase db) {
        LogMgr.i("queryAllPageSortInfo()");
        Cursor cursor = db.query(TABLE_NAME_PAGE_SORT_INFO, null, null, null, null, null, null);
        LogMgr.d("queryAllPageSortInfo() 表中的行数 = " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                //遍历Cursor对象
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_PAGE_ID_IN_TABLE_PAGE_SORT_INFO));
                String pageName = cursor.getString(cursor.getColumnIndex(COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO));
                int pageType = cursor.getInt(cursor.getColumnIndex(COLUMN_PAGE_TYPE_IN_TABLE_PAGE_SORT_INFO));
                LogMgr.d("id = " + id + " pageName = " + pageName + " pageType = " + pageType);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 获取排序表中的行数
     */
    public synchronized int getRowCountInTablePageSortInfo(SQLiteDatabase db){
        LogMgr.i("getRowCountInTablePageSortInfo()");
        Cursor cursor = db.query(TABLE_NAME_PAGE_SORT_INFO, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        LogMgr.d("queryAllPageSortInfo() 表中的行数 = " + count);
        return count;
    }

    /**
     * 删除排序表中的无效信息
     * @param db
     * @param pageNames 当前应用中有效的页面名
     */
    public synchronized int deleteAllInvalidPageSortInfo(SQLiteDatabase db, List<String> pageNames){
        LogMgr.i("deleteAllInvalidPageSortInfo 有效的页面名 pageNames = "+pageNames);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<pageNames.size() ;i++){
            sb.append("'");
            sb.append(pageNames.get(i));
            sb.append("'");
            if(i != pageNames.size() - 1){
                sb.append(",");
            }
        }
        String pageNamesInString = sb.toString();

        int deleteRows = db.delete(TABLE_NAME_PAGE_SORT_INFO, COLUMN_PAGE_NAME_IN_TABLE_PAGE_SORT_INFO + " not in ( "+ pageNamesInString +" ) ", null);
        LogMgr.d("deleteAllInvalidPageSortInfo deleteRows = "+deleteRows);
        return deleteRows;
    }
}
