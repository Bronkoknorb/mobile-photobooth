package at.czedik.photoupp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StorageService {
	private static final Logger log = LoggerFactory.getLogger(StorageService.class);

	private static final String HASH_SEPARATOR = "__";

	private static final String FINAL_EXTENSION = ".jpg";
	
	private static final String VALID_FILENAME_WITHOUT_EXTENSION_PATTERN = "[^\\\\/]+";

	public static final Pattern inputFilenamePattern = Pattern
			.compile("^(" + VALID_FILENAME_WITHOUT_EXTENSION_PATTERN + ")\\.jpe?g$", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern finalFilenamePattern = Pattern.compile(
			"^" + VALID_FILENAME_WITHOUT_EXTENSION_PATTERN + HASH_SEPARATOR + "([a-fA-F0-9]{64})"
					+ Pattern.quote(FINAL_EXTENSION) + "$",
			Pattern.CASE_INSENSITIVE);

	private final Path tempDir;
	private final Path finalDir;

	private final Set<String> alreadyUploadedHashes = Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Autowired
	public StorageService(@Value("${storage.dir}") String storageDir) {
		Path storagePath = Path.of(storageDir).toAbsolutePath().normalize();
		this.tempDir = storagePath.resolve("temp");
		this.finalDir = storagePath.resolve("final");
		init();
	}

	private void init() {
		log.info("Using temp directory: {}", tempDir);
		log.info("Using final directory: {}", finalDir);
		try {
			Files.createDirectories(tempDir);
			Files.createDirectories(finalDir);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot create directory", e);
		}

		initAlreadyUploadedHashes();
	}

	private void initAlreadyUploadedHashes() {
		try (Stream<Path> stream = Files.list(finalDir)) {
			stream.forEach(file -> {
				String filename = file.getFileName().toString();
				Matcher matcher = finalFilenamePattern.matcher(filename);
				if (matcher.matches()) {
					String hash = matcher.group(1);
					alreadyUploadedHashes.add(hash);
				} else {
					log.warn("Ignoring unexpected file in final dir: {}", filename);
				}
			});
		} catch (IOException e) {
			log.error("Cannot list {}", finalDir, e);
		}
	}

	public void store(String filename, InputStream inputStream) throws IOException {
		// it's important to validate the filename properly for security
		String nameWithoutExtension = validateFilenameAndRemoveExtension(filename);

		Path tempFile = getTempFileName();

		String hash = writeAndHashTempFile(inputStream, tempFile);

		boolean hasDuplicate = checkForDuplicates(hash);
		if (hasDuplicate) {
			log.info("File {} (with hash {}) already uploaded. Ignoring.", filename, hash);
			Files.delete(tempFile);
			return;
		}
		
		Path finalFile = getFinalFileName(nameWithoutExtension, hash);

		Files.move(tempFile, finalFile);

		log.info("Stored file: {}", finalFile.getFileName());
	}

	private boolean checkForDuplicates(String hash) {
		boolean added = alreadyUploadedHashes.add(hash);
		boolean hasDuplicate = !added;
		return hasDuplicate;
	}

	private String validateFilenameAndRemoveExtension(String filename) {
		Matcher matcher = inputFilenamePattern.matcher(filename);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid filename: " + filename);
		}
		String nameWithoutExtension = matcher.group(1);
		return nameWithoutExtension;
	}

	private Path getFinalFileName(String nameWithoutExtension, String hash) {
		return finalDir.resolve(nameWithoutExtension + HASH_SEPARATOR + hash + FINAL_EXTENSION);
	}

	private Path getTempFileName() {
		String tempRandomName = UUID.randomUUID().toString() + FINAL_EXTENSION;
		return tempDir.resolve(tempRandomName);
	}

	private String writeAndHashTempFile(InputStream inputStream, Path tempFile) throws IOException {
		String hash;
		try (DigestInputStream dis = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-256"))) {
			Files.copy(dis, tempFile);
			byte[] digest = dis.getMessageDigest().digest();
			hash = HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			// expect sha-256 to always be available
			throw new IllegalStateException(e);
		}
		return hash;
	}

}
