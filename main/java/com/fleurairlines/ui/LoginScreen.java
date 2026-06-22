package com.fleurairlines.ui;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import com.fleurairlines.model.Passenger;
import com.fleurairlines.service.AuthService;
import com.fleurairlines.util.DatabaseException;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScreen {

    private final Stage stage;
    private TabPane     tabs;

    private static final String AEGEAN_BLUE = "#0f2b5c";
    private static final String SKY_BLUE    = "#0073bc";

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene createScene(double width, double height) {
        BorderPane root = new BorderPane();
        StackPane sceneRoot = new StackPane();
        java.net.URL backgroundUrl = getClass().getResource("/images/login-background.png");
        if (backgroundUrl != null) {
            sceneRoot.setStyle("-fx-background-image: url('" + backgroundUrl.toExternalForm() + "'); " +
                    "-fx-background-size: cover; -fx-background-position: center center;");
        } else {
            sceneRoot.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f2b5c, #1e3a8a);");
        }

        Region veil = new Region();
        veil.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(3, 15, 37, 0.78), rgba(15, 43, 92, 0.50));");
        veil.setMouseTransparent(true);

        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(520);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.96); -fx-background-radius: 12px; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.34), 28, 0, 0, 14);");

        Label logo = new Label("✈  FLEUR AIRLINES");
        logo.setTextFill(Color.web(AEGEAN_BLUE));
        logo.setFont(Font.font("System", FontWeight.BOLD, 26));
        logo.setStyle("-fx-letter-spacing: 1.5px;");

        tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-tab-header-background: transparent;");

        Tab passengerLoginTab = new Tab("Passenger Login");
        Tab registerTab       = new Tab("Create Account");
        Tab adminLoginTab     = new Tab("Admin Portal");
        Tab crewLoginTab      = new Tab("Staff Portal");

        passengerLoginTab.setContent(buildPassengerLoginTab(width, height));
        registerTab.setContent(buildRegisterTab());
        adminLoginTab.setContent(buildAdminLoginTab(width, height));
        crewLoginTab.setContent(buildCrewLoginTab(width, height));

        tabs.getTabs().addAll(passengerLoginTab, registerTab, adminLoginTab, crewLoginTab);
        card.getChildren().addAll(logo, createHeroLogo(width), tabs);

        StackPane center = new StackPane(card);
        center.setPadding(new Insets(24));
        root.setCenter(center);
        root.setBottom(new FleurFooter());
        sceneRoot.getChildren().addAll(veil, root);

        FadeTransition fade = new FadeTransition(Duration.millis(450), card);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        return new Scene(sceneRoot, width, height);
    }

    private Node createHeroLogo(double width) {
        StackPane holder = new StackPane();
        holder.setMaxWidth(520);
        holder.setPrefHeight(190);

        Image image = loadResourceImage("/images/logo.png", Math.min(220, width - 180), 170);
        if (image == null) {
            image = createLogoPlaceholder(Math.min(220, width - 180), 170);
        }
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitWidth(Math.min(220, width - 180));
        imageView.setFitHeight(170);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 18, 0, 0, 10);");

        holder.getChildren().add(imageView);
        return holder;
    }

    private Image loadResourceImage(String path, double width, double height) {
        java.net.URL resourceUrl = getClass().getResource(path);
        if (resourceUrl == null) {
            return null;
        }
        return new Image(resourceUrl.toExternalForm(), width, height, true, true);
    }

    private Image createLogoPlaceholder(double width, double height) {
        WritableImage image = new WritableImage((int) width, (int) height);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double t = (double) y / Math.max(1, (int) height - 1);
                writer.setColor(x, y, Color.web("#0f2b5c").interpolate(Color.web("#1e3a8a"), t));
            }
        }
        int centerX = ((int) width) / 2;
        int centerY = ((int) height) / 2;
        for (int y = -22; y <= 22; y++) {
            for (int x = -4; x <= 4; x++) {
                if (Math.abs(x) + Math.abs(y) < 18) {
                    int px = centerX + x;
                    int py = centerY + y;
                    if (px >= 0 && px < width && py >= 0 && py < height) {
                        writer.setColor(px, py, Color.web("#f5cd50"));
                    }
                }
            }
        }
        for (int y = 0; y < 24; y++) {
            for (int x = -12; x <= 12; x++) {
                int px = centerX + x;
                int py = centerY - 28 + y;
                if (px >= 0 && px < width && py >= 0 && py < height && Math.abs(x) < 10) {
                    writer.setColor(px, py, Color.web("#ffffff"));
                }
            }
        }
        return image;
    }

    private Node buildPassengerLoginTab(double width, double height) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20, 4, 4, 4));
        box.setAlignment(Pos.CENTER_LEFT);

        TextField     emailField    = styledField("your@email.com");
        PasswordField passwordField = styledPassword("Password");
        TextField     visiblePass   = styledField("");
        visiblePass.setVisible(false);
        visiblePass.setManaged(false);
        visiblePass.textProperty().bindBidirectional(passwordField.textProperty());
        CheckBox showPass = showPassCheckbox(passwordField, visiblePass);

        Label errorLabel = errorLabel();

        Button loginBtn = navyButton("Sign In", 400);
        loginBtn.setDefaultButton(true); 
        enableEnterToSubmit(emailField, passwordField, visiblePass, loginBtn);
        
        loginBtn.setOnAction(e -> {
            errorLabel.setText("");
            String email = emailField.getText().trim().toLowerCase();
            // Explicitly grab the active field to bypass JavaFX binding glitches
            String password = visiblePass.isVisible() ? visiblePass.getText() : passwordField.getText();
            
            if (email.isEmpty() || password.isEmpty()) { errorLabel.setText("Please enter all credentials."); return; }

            loginBtn.setDisable(true);
            Task<Passenger> task = new Task<>() {
                @Override
                protected Passenger call() throws Exception {
                    return new AuthService().loginPassenger(email, password);
                }
            };
            task.setOnSucceeded(ev -> {
                Passenger p = task.getValue();
                try {
                    SceneTransitionUtil.standardFade(stage, new PassengerDashboard(stage, p).createScene(width, height));
                } catch (DatabaseException ex) {
                    errorLabel.setText(ex.getMessage());
                    loginBtn.setDisable(false);
                }
            });
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                Platform.runLater(() -> {
                    errorLabel.setText(ex != null ? ex.getMessage() : "Authentication rejected.");
                    loginBtn.setDisable(false);
                });
            });
            new Thread(task).start();
        });

        Hyperlink createLink = new Hyperlink("New traveler? Register a passport profile here");
        createLink.setStyle("-fx-text-fill: " + SKY_BLUE + "; -fx-font-size: 12px; -fx-border-color: transparent; -fx-padding: 4 0 0 0;");
        createLink.setOnAction(e -> tabs.getSelectionModel().select(1));

        box.getChildren().addAll(
                fieldLabel("EMAIL"), emailField,
                fieldLabel("PASSWORD"), passwordField, visiblePass, showPass,
                errorLabel, loginBtn, createLink);
        return scrollableAuthPane(box);
    }

    private Node buildCrewLoginTab(double width, double height) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20, 4, 4, 4));
        box.setAlignment(Pos.CENTER_LEFT);

        TextField     emailField    = styledField("crew@fleurairlines.com");
        PasswordField passwordField = styledPassword("Password");
        TextField     visiblePass   = styledField("");
        visiblePass.setVisible(false);
        visiblePass.setManaged(false);
        visiblePass.textProperty().bindBidirectional(passwordField.textProperty());
        CheckBox showPass = showPassCheckbox(passwordField, visiblePass);

        Label errorLabel = errorLabel();

        Button loginBtn = navyButton("Staff Sign In", 400);
        loginBtn.setDefaultButton(true);
        enableEnterToSubmit(emailField, passwordField, visiblePass, loginBtn);
        
        loginBtn.setOnAction(e -> {
            errorLabel.setText("");
            String email = emailField.getText().trim().toLowerCase();
            String password = visiblePass.isVisible() ? visiblePass.getText() : passwordField.getText();
            
            if (email.isEmpty() || password.isEmpty()) { errorLabel.setText("Please enter all fields."); return; }

            loginBtn.setDisable(true);
            Task<CrewMemberScreen.CrewMember> task = new Task<>() {
                @Override
                protected CrewMemberScreen.CrewMember call() throws Exception {
                    return new AuthService().loginCrew(email, password);
                }
            };
            task.setOnSucceeded(ev -> {
                CrewMemberScreen.CrewMember crew = task.getValue();
                try {
                    SceneTransitionUtil.standardFade(stage, new CrewMemberScreen(stage, crew).createScene(width, height));
                } catch (DatabaseException ex) {
                    errorLabel.setText(ex.getMessage());
                    loginBtn.setDisable(false);
                }
            });
            task.setOnFailed(ev -> {
                Platform.runLater(() -> {
                    errorLabel.setText("Invalid official credentials.");
                    loginBtn.setDisable(false);
                });
            });
            new Thread(task).start();
        });

        box.getChildren().addAll(
                fieldLabel("EMAIL"), emailField,
                fieldLabel("PASSWORD"), passwordField, visiblePass, showPass,
                errorLabel, loginBtn);
        return scrollableAuthPane(box);
    }

    private Node buildAdminLoginTab(double width, double height) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20, 4, 4, 4));
        box.setAlignment(Pos.CENTER_LEFT);

        TextField     emailField    = styledField("admin@fleurairlines.com");
        PasswordField passwordField = styledPassword("Password");
        TextField     visiblePass   = styledField("");
        visiblePass.setVisible(false);
        visiblePass.setManaged(false);
        visiblePass.textProperty().bindBidirectional(passwordField.textProperty());
        CheckBox showPass = showPassCheckbox(passwordField, visiblePass);

        Label errorLabel = errorLabel();

        Button loginBtn = navyButton("Administrator Terminal Entry", 400);
        loginBtn.setDefaultButton(true);
        enableEnterToSubmit(emailField, passwordField, visiblePass, loginBtn);
        
        loginBtn.setOnAction(e -> {
            errorLabel.setText("");
            String email = emailField.getText().trim().toLowerCase();
            String password = visiblePass.isVisible() ? visiblePass.getText() : passwordField.getText();
            
            if (email.isEmpty() || password.isEmpty()) { errorLabel.setText("Please enter secure administrative tokens."); return; }

            loginBtn.setDisable(true);
            Task<com.fleurairlines.model.Admin> task = new Task<>() {
                @Override
                protected com.fleurairlines.model.Admin call() throws Exception {
                    return new AuthService().loginAdmin(email, password);
                }
            };
            task.setOnSucceeded(ev -> {
                com.fleurairlines.model.Admin admin = task.getValue();
                try {
                    SceneTransitionUtil.standardFade(stage, new AdminDashboard(stage, admin).createScene(width, height));
                } catch (DatabaseException ex) {
                    errorLabel.setText(ex.getMessage());
                    loginBtn.setDisable(false);
                }
            });
            task.setOnFailed(ev -> {
                Platform.runLater(() -> {
                    errorLabel.setText("Secure authentication clearance denied.");
                    loginBtn.setDisable(false);
                });
            });
            new Thread(task).start();
        });

        box.getChildren().addAll(
                fieldLabel("EMAIL"), emailField,
                fieldLabel("PASSWORD"), passwordField, visiblePass, showPass,
                errorLabel, loginBtn);
        return scrollableAuthPane(box);
    }

    private Node buildRegisterTab() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20, 4, 4, 4));
        box.setAlignment(Pos.TOP_CENTER);

        TextField     nameField     = styledField("Full Legal Name");
        TextField     emailField    = styledField("Email Address");
        PasswordField passwordField = styledPassword("Password");
        TextField     visiblePass   = styledField("");
        visiblePass.setVisible(false);
        visiblePass.setManaged(false);
        visiblePass.textProperty().bindBidirectional(passwordField.textProperty());
        CheckBox showPass = showPassCheckbox(passwordField, visiblePass);

        ComboBox<String> prefixBox = new ComboBox<>();
        prefixBox.getItems().addAll(
                "+1 (USA)", "+7 (Russia)", "+20 (Egypt)", "+27 (South Africa)", "+30 (Greece)",
                "+31 (Netherlands)", "+32 (Belgium)", "+33 (France)", "+34 (Spain)", "+36 (Hungary)",
                "+39 (Italy)", "+40 (Romania)", "+41 (Switzerland)", "+43 (Austria)", "+44 (UK)",
                "+45 (Denmark)", "+46 (Sweden)", "+47 (Norway)", "+48 (Poland)", "+49 (Germany)",
                "+51 (Peru)", "+52 (Mexico)", "+53 (Cuba)", "+54 (Argentina)", "+55 (Brazil)",
                "+56 (Chile)", "+57 (Colombia)", "+58 (Venezuela)", "+60 (Malaysia)", "+61 (Australia)",
                "+62 (Indonesia)", "+63 (Philippines)", "+64 (New Zealand)", "+65 (Singapore)",
                "+66 (Thailand)", "+81 (Japan)", "+82 (South Korea)", "+84 (Vietnam)", "+86 (China)",
                "+90 (Turkey)", "+91 (India)", "+92 (Pakistan)", "+93 (Afghanistan)", "+94 (Sri Lanka)",
                "+95 (Myanmar)", "+98 (Iran)", "+211 (South Sudan)", "+212 (Morocco)", "+213 (Algeria)",
                "+216 (Tunisia)", "+218 (Libya)", "+220 (Gambia)", "+221 (Senegal)", "+222 (Mauritania)",
                "+223 (Mali)", "+224 (Guinea)", "+225 (Côte d'Ivoire)", "+226 (Burkina Faso)",
                "+227 (Niger)", "+228 (Togo)", "+229 (Benin)", "+230 (Mauritius)", "+231 (Liberia)",
                "+232 (Sierra Leone)", "+233 (Ghana)", "+234 (Nigeria)", "+235 (Chad)", "+236 (Central African Republic)",
                "+237 (Cameroon)", "+238 (Cape Verde)", "+239 (São Tomé & Príncipe)", "+240 (Equatorial Guinea)",
                "+241 (Gabon)", "+242 (Republic of the Congo)", "+243 (DR Congo)", "+244 (Angola)",
                "+245 (Guinea-Bissau)", "+246 (Diego Garcia)", "+248 (Seychelles)", "+249 (Sudan)",
                "+250 (Rwanda)", "+251 (Ethiopia)", "+252 (Somalia)", "+253 (Djibouti)", "+254 (Kenya)",
                "+255 (Tanzania)", "+256 (Uganda)", "+257 (Burundi)", "+258 (Mozambique)", "+260 (Zambia)",
                "+261 (Madagascar)", "+262 (Réunion / Mayotte)", "+263 (Zimbabwe)", "+264 (Namibia)",
                "+265 (Malawi)", "+266 (Lesotho)", "+267 (Botswana)", "+268 (Eswatini)", "+269 (Comoros)",
                "+290 (Saint Helena)", "+297 (Aruba)", "+298 (Faroe Islands)", "+299 (Greenland)",
                "+350 (Gibraltar)", "+351 (Portugal)", "+352 (Luxembourg)", "+353 (Ireland)",
                "+354 (Iceland)", "+355 (Albania)", "+356 (Malta)", "+357 (Cyprus)",
                "+358 (Finland)", "+359 (Bulgaria)", "+370 (Lithuania)", "+371 (Latvia)",
                "+372 (Estonia)", "+373 (Moldova)", "+374 (Armenia)", "+375 (Belarus)",
                "+376 (Andorra)", "+377 (Monaco)", "+378 (San Marino)", "+380 (Ukraine)",
                "+381 (Serbia)", "+382 (Montenegro)", "+383 (Kosovo)", "+385 (Croatia)",
                "+386 (Slovenia)", "+387 (Bosnia & Herzegovina)", "+389 (North Macedonia)",
                "+420 (Czech Republic)", "+421 (Slovakia)", "+423 (Liechtenstein)",
                "+501 (Belize)", "+502 (Guatemala)", "+503 (El Salvador)", "+504 (Honduras)",
                "+505 (Nicaragua)", "+506 (Costa Rica)", "+507 (Panama)", "+508 (Saint Pierre & Miquelon)",
                "+509 (Haiti)", "+590 (Guadeloupe)", "+591 (Bolivia)", "+592 (Guyana)",
                "+593 (Ecuador)", "+594 (French Guiana)", "+595 (Paraguay)", "+596 (Martinique)",
                "+597 (Suriname)", "+598 (Uruguay)", "+599 (Caribbean Netherlands)",
                "+670 (East Timor)", "+672 (Australian External Territories)", "+673 (Brunei)",
                "+674 (Nauru)", "+675 (Papua New Guinea)", "+676 (Tonga)", "+677 (Solomon Islands)",
                "+678 (Vanuatu)", "+679 (Fiji)", "+680 (Palau)", "+681 (Wallis & Futuna)",
                "+682 (Cook Islands)", "+683 (Niue)", "+685 (Samoa)", "+686 (Kiribati)",
                "+687 (New Caledonia)", "+688 (Tuvalu)", "+689 (French Polynesia)",
                "+690 (Tokelau)", "+691 (Micronesia)", "+692 (Marshall Islands)",
                "+850 (North Korea)", "+852 (Hong Kong)", "+853 (Macau)", "+855 (Cambodia)",
                "+856 (Laos)", "+880 (Bangladesh)", "+886 (Taiwan)", "+960 (Maldives)",
                "+961 (Lebanon)", "+962 (Jordan)", "+963 (Syria)", "+964 (Iraq)",
                "+965 (Kuwait)", "+966 (Saudi Arabia)", "+967 (Yemen)", "+968 (Oman)",
                "+970 (Palestine)", "+971 (UAE)", "+972 (Israel)", "+973 (Bahrain)",
                "+974 (Qatar)", "+975 (Bhutan)", "+976 (Mongolia)", "+977 (Nepal)",
                "+992 (Tajikistan)", "+993 (Turkmenistan)", "+994 (Azerbaijan)",
                "+995 (Georgia)", "+996 (Kyrgyzstan)", "+998 (Uzbekistan)");
        prefixBox.setEditable(true);
        prefixBox.setPromptText("Country Code");
        prefixBox.setPrefWidth(140);
        prefixBox.getSelectionModel().select("+20 (Egypt)");

        TextField phoneInput = styledField("Phone Number");
        phoneInput.setMaxWidth(220);
        HBox.setHgrow(phoneInput, Priority.ALWAYS);
        HBox phoneRow = new HBox(8, prefixBox, phoneInput);
        phoneRow.setAlignment(Pos.CENTER_LEFT);
        phoneRow.setMaxWidth(400);

        TextField passportField = styledField("Passport Number");

        ComboBox<String> nationalityBox = new ComboBox<>();
        nationalityBox.getItems().addAll(Arrays.stream(Locale.getISOCountries())
                .map(code -> new Locale("", code).getDisplayCountry())
                .sorted().collect(Collectors.toList()));
        nationalityBox.setPromptText("Select Nationality");
        nationalityBox.setMaxWidth(Double.MAX_VALUE);
        nationalityBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1;");

        ComboBox<Integer> dobYear  = new ComboBox<>();
        int curYear = LocalDate.now().getYear();
        for (int y = curYear - 18; y >= 1924; y--) dobYear.getItems().add(y);
        dobYear.setPromptText("Year"); dobYear.setPrefWidth(100);
        dobYear.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1;");

        String[] monthNames = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        ComboBox<String>  dobMonth = new ComboBox<>();
        dobMonth.getItems().addAll(monthNames);
        dobMonth.setPromptText("Month"); dobMonth.setPrefWidth(120);
        dobMonth.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1;");

        ComboBox<Integer> dobDay = new ComboBox<>();
        dobDay.setPromptText("Day"); dobDay.setPrefWidth(90);
        dobDay.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1;");

        Runnable updateDays = () -> {
            Integer y = dobYear.getValue(); String m = dobMonth.getValue();
            if (y == null || m == null) return;
            int mIdx = Arrays.asList(monthNames).indexOf(m) + 1;
            int days = YearMonth.of(y, mIdx).lengthOfMonth();
            dobDay.getItems().clear();
            for (int d = 1; d <= days; d++) dobDay.getItems().add(d);
        };
        dobYear.setOnAction(e -> updateDays.run());
        dobMonth.setOnAction(e -> updateDays.run());
        HBox dobRow = new HBox(8, dobYear, dobMonth, dobDay);
        dobRow.setMaxWidth(400);

        Label errorLabel   = errorLabel();
        Label successLabel = new Label();
        successLabel.setTextFill(Color.web("#10b981"));
        successLabel.setStyle("-fx-font-weight: bold;");

        Button registerBtn = navyButton("Register Profile", 400);
        registerBtn.setDefaultButton(true); 
        registerBtn.setOnAction(e -> {
            errorLabel.setText(""); successLabel.setText("");
            String name        = nameField.getText().trim();
            String email       = emailField.getText().trim().toLowerCase();
            // Explicitly grab the active field to bypass JavaFX binding glitches
            String password    = visiblePass.isVisible() ? visiblePass.getText() : passwordField.getText();
            String dialCode    = prefixBox.getValue();
            String phoneDigits = phoneInput.getText().trim();
            String passport    = passportField.getText().trim().toUpperCase();
            String nationality = nationalityBox.getValue();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || dialCode == null || dialCode.isBlank() || phoneDigits.isEmpty() || passport.isEmpty() || nationality == null || dobYear.getValue() == null) {
                errorLabel.setText("Please complete all required fields."); return;
            }
            int mIdx = Arrays.asList(monthNames).indexOf(dobMonth.getValue()) + 1;
            LocalDate dob = LocalDate.of(dobYear.getValue(), mIdx, dobDay.getValue());
            if (Period.between(dob, LocalDate.now()).getYears() < 18) {
                errorLabel.setText("Passengers must be 18 years of age or older."); return;
            }

            String normalizedDialCode = dialCode.split(" ")[0].trim();
            if (!normalizedDialCode.startsWith("+")) {
                normalizedDialCode = "+" + normalizedDialCode.replaceAll("[^0-9]", "");
            }
            String normalizedPhone = normalizedDialCode + phoneDigits.replaceAll("[^0-9]", "");
            if (!normalizedPhone.matches("^\\+[0-9]{7,15}$")) {
                errorLabel.setText("Please enter a valid international phone number."); return;
            }

            try {
                String dobStr = String.format("%04d-%02d-%02d", dobYear.getValue(), mIdx, dobDay.getValue());
                new AuthService().register(name, email, password, normalizedPhone, passport, nationality, dobStr);
                successLabel.setText("Profile authorized! You can now sign in.");
            } catch (DatabaseException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        box.getChildren().addAll(
                fieldLabel("FULL NAME"),       nameField,
                fieldLabel("EMAIL"),           emailField,
                fieldLabel("PASSWORD"),        passwordField, visiblePass, showPass,
                fieldLabel("PHONE NUMBER"),    phoneRow,
                fieldLabel("PASSPORT NUMBER"), passportField,
                fieldLabel("NATIONALITY"),     nationalityBox,
                fieldLabel("DATE OF BIRTH"),   dobRow,
                errorLabel, successLabel,      registerBtn);

        return scrollableAuthPane(box);
    }

    private ScrollPane scrollableAuthPane(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setMaxHeight(330);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; " +
                "-fx-padding: 0 4 0 0;");
        return scroll;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-letter-spacing: 0.5px;");
        return l;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px; -fx-font-size: 13px;");
        return tf;
    }

    private PasswordField styledPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 10px; -fx-font-size: 13px;");
        return pf;
    }

    private CheckBox showPassCheckbox(PasswordField pf, TextField visible) {
        CheckBox cb = new CheckBox("Show Password");
        cb.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-cursor: hand;");
        cb.selectedProperty().addListener((obs, o, newVal) -> {
            visible.setVisible(newVal); visible.setManaged(newVal);
            pf.setVisible(!newVal);    pf.setManaged(!newVal);
        });
        return cb;
    }

    private Label errorLabel() {
        Label l = new Label();
        l.setTextFill(Color.web("#dc2626"));
        l.setWrapText(true);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        return l;
    }

    private Button navyButton(String text, double width) {
        Button btn = new Button(text);
        btn.setMaxWidth(width);
        btn.setStyle("-fx-background-color: " + AEGEAN_BLUE + "; -fx-text-fill: white; " +
                     "-fx-font-weight: bold; -fx-padding: 12px; -fx-font-size: 14px; " +
                     "-fx-background-radius: 6px; -fx-cursor: hand;");
        return btn;
    }

    private void enableEnterToSubmit(Node email, PasswordField pf, TextField visible, Button btn) {
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> handler = event -> {
            if (event.getCode() == KeyCode.ENTER) { btn.fire(); event.consume(); }
        };
        email.setOnKeyPressed(handler);
        pf.setOnKeyPressed(handler);
        visible.setOnKeyPressed(handler);
    }
}
