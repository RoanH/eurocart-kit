package nl.group9.eurocart.api;

import nl.group9.eurocart.order.Terms;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/terms")
public class TermsController {

	public record TermsInfo(String version, String url) {
	}

	@GetMapping
	public TermsInfo current() {
		return new TermsInfo(Terms.CURRENT_VERSION, "/terms.html");
	}

}
