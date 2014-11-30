package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

public class FileUtils {
	public static final boolean REVERSE = false;
	
	public static String resourceToString(String name) throws IOException {
		File file = resourceToFile(name);
		Scanner scanner = new Scanner(file);
		String string = scanner.useDelimiter("\\A").next();
		scanner.close();
		return string;
	}

	public static File resourceToFile(String name) throws IOException {
		URL url = ClassLoader.getSystemClassLoader().getResource(name);
		if (url != null) {
			try {
				return new File(url.toURI());
			} catch (URISyntaxException e) {}
		}
		throw new IOException();
	}

	public static BufferedReader resourceToReader(String name) throws IOException {
		return resourceToReader(name, REVERSE);
	}

	public static BufferedReader resourceToReader(String name, boolean reverse) throws IOException {
		File file = resourceToFile(name);
		return fileToReader(file, reverse);
	}

	public static BufferedReader fileToBufferedReader(File file) throws FileNotFoundException {
		return fileToReader(file, REVERSE);
	}

	public static BufferedReader fileToReader(File file, boolean reverse) throws FileNotFoundException {
		InputStream inputStream = fileToInputStream(file, reverse);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		return new BufferedReader(inputStreamReader);
	}

	public static InputStream fileToInputStream(File file) throws FileNotFoundException {
		return fileToInputStream(file, REVERSE);
	}

	public static InputStream fileToInputStream(File file, boolean reverse) throws FileNotFoundException {
		InputStream inputStream;
		if (reverse) {
			inputStream = new ReverseLineInputStream(file);
		} else {
			inputStream = new FileInputStream(file);
		}
		return inputStream;
	}

	public static InputStream resourceToInputStream(String name) throws IOException {
		return resourceToInputStream(name, REVERSE);		
	}

	public static InputStream resourceToInputStream(String name, boolean reverse) throws IOException {
		if (reverse) { 
			File file = resourceToFile(name);
			return fileToInputStream(file, reverse);
		} else {
			return ClassLoader.getSystemClassLoader().getResourceAsStream(name);
		}
	}

	public static Scanner resourceToScanner(String string) throws IOException {
		return new Scanner(resourceToReader(string));
	}
}
