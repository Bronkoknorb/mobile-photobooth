package at.czedik.photoupp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class FilenamePatternTest {

	@Test
	void testInputPattern() {
		Pattern pat = StorageService.inputFilenamePattern;

		assertTrue(pat.matcher("test.jpg").matches());
		assertTrue(pat.matcher("test.JPG").matches());
		assertTrue(pat.matcher("test.jpeg").matches());
		assertTrue(pat.matcher("test.JPEG").matches());
		assertTrue(pat.matcher("te#+.-_& st.jpg").matches());

		assertFalse(pat.matcher("test.gif").matches());
		assertFalse(pat.matcher(".jpg").matches());
		assertFalse(pat.matcher("testjpg").matches());
		assertFalse(pat.matcher("test.jpgjpg").matches());
		assertFalse(pat.matcher("/test.jpg").matches());
		assertFalse(pat.matcher("te\\st.jpg").matches());
		assertFalse(pat.matcher("../test.jpg").matches());
	}

	@Test
	void testFinalPattern() {
		Pattern pat = StorageService.finalFilenamePattern;

		assertTrue(pat.matcher(
				"2023-02-15_17-17-26_IMG-20230215-WA0011__bbd9d54a3c77ebe99f7e7edb832a15026165cb9d453cb5d175bcce94e405a819.jpg")
				.matches());

		assertFalse(pat.matcher(
				"2023-02-15_17-17-26_IMG-20230215-WA0011__bbd9d54a3c77ebe99f7e7edb832a15026165cb9d453cb5d175bcce94e405a81.jpg")
				.matches());
		assertFalse(pat.matcher(
				"2023-02-15_17-17-26_IMG-20230215-WA0011__bbd9d54a3c77ebe99f7e7edb832a15026165cb9d453cb5d175bcce94e405a819.jpeg")
				.matches());

	}
}
