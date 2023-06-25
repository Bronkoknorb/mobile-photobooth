package at.czedik.photoupp;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api")
public class FileUploadController {

	private StorageService service;

	@Autowired
	public FileUploadController(StorageService service) {
		this.service = service;
	}

	@GetMapping("/serve/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		// TODO check that filename is competely valid

		Resource file = null; // TODO storageService.loadAsResource(filename);
		return ResponseEntity.ok().body(file);
	}

	@PostMapping("/up")
	@ResponseBody
	public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		try (InputStream inputStream = file.getInputStream()) {
			service.store(file.getOriginalFilename(), inputStream);
		}
		return "Stored";
	}
}
