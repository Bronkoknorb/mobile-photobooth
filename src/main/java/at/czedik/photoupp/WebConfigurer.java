package at.czedik.photoupp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/view").setViewName("redirect:/view/");
		registry.addViewController("/view/").setViewName("forward:/view/index.html");
    }
}