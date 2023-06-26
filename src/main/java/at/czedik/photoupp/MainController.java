package at.czedik.photoupp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class MainController {

	private StorageService service;

	@Autowired
	public MainController(StorageService service) {
		this.service = service;
	}
	
	@PostMapping("/up")
	@ResponseBody
	public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		try (InputStream inputStream = file.getInputStream()) {
			service.store(file.getOriginalFilename(), inputStream);
		}
		return "Stored";
	}

	@GetMapping(path = "/list", produces = { MediaType.APPLICATION_JSON_VALUE })
	public PhotoList list(WebRequest request) {
		String etag = service.getETag();
		if (request.checkNotModified(etag)) {
			// shortcut exit - no further processing necessary
			return null;
		}

		return service.list();
	}
	
	@GetMapping(path = "/serve/{filename:.+}", produces = { MediaType.IMAGE_JPEG_VALUE })
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws MalformedURLException {
		Resource file = service.load(filename);
		return ResponseEntity.ok()
				// long caching as these will never change (includes hash in name)
				.cacheControl(CacheControl.maxAge(Duration.ofDays(10)))
				.body(file);
	}
}
