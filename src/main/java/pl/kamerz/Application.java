package pl.kamerz;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Aura.STYLESHEET)
public class Application implements AppShellConfigurator {

    @Value("${app.base-url:}")
    private String baseUrl;

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addLink("stylesheet", "styles.css");
        settings.addLink("stylesheet", "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css");
        settings.addFavIcon("icon", "img/logo.png", "32x32");

        String image = baseUrl + "/img/logo.png";
        settings.addMetaTag("og:type",        "website");
        settings.addMetaTag("og:site_name",   "Spash Softworks");
        settings.addMetaTag("og:title",       "Spash Softworks");
        settings.addMetaTag("og:description", "Executor APIs, offsets, and open-source tools for Roblox.");
        settings.addMetaTag("og:image",       image);
        settings.addMetaTag("twitter:card",   "summary");
        settings.addMetaTag("twitter:title",  "Spash Softworks");
        settings.addMetaTag("twitter:description", "Executor APIs, offsets, and open-source tools for Roblox.");
        settings.addMetaTag("twitter:image",  image);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
