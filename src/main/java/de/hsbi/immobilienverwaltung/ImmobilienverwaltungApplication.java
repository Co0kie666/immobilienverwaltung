package de.hsbi.immobilienverwaltung;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("immobilienverwaltung")
public class ImmobilienverwaltungApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(ImmobilienverwaltungApplication.class, args);
    }
}
