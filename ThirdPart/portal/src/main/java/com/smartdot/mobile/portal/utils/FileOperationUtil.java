package com.smartdot.mobile.portal.utils;

import java.io.File;

/**
 * 文件操作工具类
 * 
 * @author W.B
 * 
 */
public class FileOperationUtil {

	/**
	 * 删除文件/文件夹
	 * 
	 * @param file
	 *            传文件路径删文件,传文件夹路径删文件夹
	 */
	public static void delete(File file) {
		if (file.isFile()) {// 是否文件
			L.v("成功删除文件-->"+file.getName());
			file.delete();
			return;
		}
		if (file.isDirectory()) {// 是否文件夹
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				L.v("成功删除文件夹");
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @return
	 */
	public static boolean fileIsExists(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
