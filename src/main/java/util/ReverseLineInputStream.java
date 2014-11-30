package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class ReverseLineInputStream extends InputStream {
	RandomAccessFile randomAccessFile;

	long currentLineStart = -1;
	long currentLineEnd = -1;
	long currentPos = -1;
	long lastPosInFile = -1;

	public ReverseLineInputStream(File file) throws FileNotFoundException {
		randomAccessFile = new RandomAccessFile(file, "r");
		currentLineStart = file.length();
		currentLineEnd = file.length();
		lastPosInFile = file.length() - 1;
		currentPos = currentLineEnd;
	}

	public void findPrevLine() throws IOException {
		currentLineEnd = currentLineStart;

		// No more lines, since at the beginning of the file
		if (currentLineEnd == 0) {
			currentLineEnd = -1;
			currentLineStart = -1;
			currentPos = -1;
			return;
		}

		long filePointer = currentLineStart - 1;
		while (true) {
			// At start of file so this is the first line in the file.
			if (--filePointer < 0) {
				break;
			}

			randomAccessFile.seek(filePointer);
			int readByte = randomAccessFile.readByte();

			// Ignore last LF in file. search back to find the previous LF.
			if (readByte == 0xA && filePointer != lastPosInFile) {
				break;
			}
		}
		// Start at pointer +1, after the LF found or at 0 the start of the file.
		currentLineStart = filePointer + 1;
		currentPos = currentLineStart;
	}

	public int read() throws IOException {
		if (currentPos < currentLineEnd) {
			randomAccessFile.seek(currentPos++);
			int readByte = randomAccessFile.readByte();
			return readByte;
		} else if (currentPos < 0) {
			return -1;
		} else {
			findPrevLine();
			return read();
		}
	}
}