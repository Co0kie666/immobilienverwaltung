package de.hsbi.immobilienverwaltung.ui.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import de.hsbi.immobilienverwaltung.ui.auth.LoginView;
import de.hsbi.immobilienverwaltung.ui.dashboard.DashboardView;
import de.hsbi.immobilienverwaltung.ui.finanzen.FinanzDashboardView;
import de.hsbi.immobilienverwaltung.ui.immobilien.ImmobilienListView;
import de.hsbi.immobilienverwaltung.ui.mieter.MieterVertraegeView;

public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final H1 pageTitle = new H1();
    private final Paragraph pageSubtitle = new Paragraph();

    public MainLayout() {
        addClassName("main-layout");

        setPrimarySection(Section.DRAWER); // Sidebar geht von oben bis unten

        addToDrawer(createSidebar());
        addToNavbar(createHeader());
    }

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.addClassName("sidebar"); // CSS
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setSizeFull(); // Volle Höhe für die Sidebar
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Logo Bereich
        HorizontalLayout logoArea = new HorizontalLayout();
        logoArea.addClassName("logo-area");
        logoArea.setAlignItems(FlexComponent.Alignment.CENTER);
        logoArea.setWidthFull();
        logoArea.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        Icon logoIcon = VaadinIcon.BUILDING.create();

        Div logoBox = new Div(logoIcon); // Box um später das icon stylen zu können
        logoBox.addClassName("logo-box");

        Span logoText = new Span("ImmoPro");
        logoText.addClassName("logo-text");

        logoArea.add(logoBox, logoText);

        // Menu Label
        Span menuLabel = new Span("Menu");
        menuLabel.addClassName("menu-label");

        // navigation
        VerticalLayout navigation = new VerticalLayout();
        navigation.addClassName("navigation");
        navigation.setPadding(false);
        navigation.setSpacing(false);
        navigation.setAlignItems(FlexComponent.Alignment.STRETCH);

        navigation.add(
                createNavLink("Dashboard", VaadinIcon.HOME, DashboardView.class),
                createNavLink("Immobilien", VaadinIcon.BUILDING, ImmobilienListView.class),
                createNavLink("Mieter & Verträge", VaadinIcon.USERS, MieterVertraegeView.class),
                createNavLink("Finanzen", VaadinIcon.CHART, FinanzDashboardView.class)
        );

        // Abstandshalter, damit General nach unten geschoben wird
        Div spacer = new Div();
        spacer.addClassName("sidebar-spacer");


        // Bottom Navigation
        VerticalLayout bottomNavigation = new VerticalLayout();
        bottomNavigation.addClassName("navigation");
        bottomNavigation.setPadding(false);
        bottomNavigation.setSpacing(false);
        bottomNavigation.setAlignItems(FlexComponent.Alignment.STRETCH);

        RouterLink logoutLink = createNavLink("Logout", VaadinIcon.SIGN_OUT, LoginView.class);
        logoutLink.addClassName("logout-link");

        bottomNavigation.add(logoutLink);

        sidebar.add(logoArea, menuLabel, navigation, spacer, bottomNavigation);

        return sidebar;
    }

    // Erstellt einen Navigationslink mit Icon Text
    private RouterLink createNavLink(String text, VaadinIcon icon, Class<? extends Component> target) {
        RouterLink link = new RouterLink();
        link.setRoute(target);
        link.addClassName("nav-link");

        if (target.equals(DashboardView.class)) {
            link.setHighlightCondition((routerLink, event) ->
                    event.getLocation().getPath().isEmpty()
                            || event.getLocation().getPath().equals("dashboard")
            );
        }

        Icon navIcon = icon.create();
        navIcon.addClassName("nav-icon");

        Span label = new Span(text);
        link.add(navIcon, label);

        return link;
    }

    // Kopfzeile mit Seitentitel, Untertitel, Suchleiste und Benutzerprofil
    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("app-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Linker Bereich: Titel + Untertitel
        Div titleArea = new Div();
        titleArea.addClassName("title-area");

        pageTitle.addClassName("page-title");
        pageSubtitle.addClassName("page-subtitle");

        titleArea.add(pageTitle, pageSubtitle);

        // Rechter Bereich: Suchleiste + Benutzerprofil
        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.addClassName("header-right");
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField searchField = new TextField();
        searchField.addClassName("search-field");
        searchField.setPlaceholder("Suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        HorizontalLayout userProfile = createUserProfile();

        rightArea.add(searchField, userProfile);

        header.add(titleArea, rightArea);

        return header;
    }

    private HorizontalLayout createUserProfile() {
        HorizontalLayout userProfile = new HorizontalLayout();
        userProfile.addClassName("user-profile");
        userProfile.setAlignItems(FlexComponent.Alignment.CENTER);

        Avatar avatar = new Avatar("Admin User");
        avatar.addClassName("user-avatar");

        Div userText = new Div();
        userText.addClassName("user-text");

        Span name = new Span("Admin User");
        name.addClassName("user-name");

        Span email = new Span("admin@immopro.de");
        email.addClassName("user-email");

        userText.add(name, email);

        userProfile.add(avatar, userText);

        return userProfile;
    }

    /*Aktualisiert den Titel im Header passend zur aktuellen Seite
    Wird nach jeder Navigation automatisch aufgerufen */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        Component content = getContent(); // holt sich aktuelle View

        if (content instanceof HasPageHeader page) { // prüft ob die View das Interface HasPageTitle implementiert
            pageTitle.setText(page.getPageTitle());

            String subtitle = page.getPageSubtitle();
            pageSubtitle.setText(subtitle);
            pageSubtitle.setVisible(subtitle != null && !subtitle.isBlank());
        } else {
            pageTitle.setText("");
            pageSubtitle.setText("");
            pageSubtitle.setVisible(false);
        }
    }
}