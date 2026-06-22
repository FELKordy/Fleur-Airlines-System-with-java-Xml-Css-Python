module com.fleurairlines {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.desktop;

    opens com.fleurairlines             to javafx.fxml;
    opens com.fleurairlines.model       to javafx.fxml;
    opens com.fleurairlines.database    to javafx.fxml;
    opens com.fleurairlines.util        to javafx.fxml;
    opens com.fleurairlines.pattern     to javafx.fxml;
    opens com.fleurairlines.service     to javafx.fxml;
    opens com.fleurairlines.ui          to javafx.fxml;

    exports com.fleurairlines;
    exports com.fleurairlines.model;
    exports com.fleurairlines.database;
    exports com.fleurairlines.util;
    exports com.fleurairlines.pattern;
    exports com.fleurairlines.service;
    exports com.fleurairlines.ui;
}