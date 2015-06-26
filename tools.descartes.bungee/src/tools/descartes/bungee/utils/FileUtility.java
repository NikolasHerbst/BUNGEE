/*******************************************************************************
Copyright 2015 Andreas Weber, Nikolas Herbst

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*******************************************************************************/

package tools.descartes.bungee.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class FileUtility {
	public static final String CSV_SPLIT_BY = ";";
	public static final String ENOCDING = "UTF-8";
	
	public static final File FILE_LOCATION =  new File(new File(new File(System.getProperty("user.dir")).getParent(), "tools.descartes.bungee"), "files");

	public static String getFileNameWithoutExtension(File file)
	{
		int end = file.toString().lastIndexOf(".");
		if (end >= 0)
		{
			return file.toString().substring(0, end);
		} else {
			return file.toString();
		}
	}
	
	public static File getRelativeFilePath(File absolutePath, File reference)
	{
		File relativePath = absolutePath;
		if (absolutePath.getAbsolutePath().contains(reference.getAbsolutePath()))
		{
			relativePath = new File(absolutePath.getAbsolutePath().substring(reference.getAbsolutePath().length()+1));
		}
		return  relativePath;
	}
	
	public static File getAbsolutePath(File file) {
		if (file.exists()) {
			// path is already absolute
			return file;
		} else {
			// create absolute path
			return new File(FILE_LOCATION, file.toString());
		}
	}
	
	public static boolean saveProperties(Properties properties, File file) {
		boolean success = false;
		OutputStream output = null;
		try {
			output = new FileOutputStream(file);
			properties.store(output, "");
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
			{
				try {
					output.close();
				} catch (IOException e) {}
			}
		}
		return success;
	}
	
	public static Properties loadProperties(File file) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return properties;
	}
}
