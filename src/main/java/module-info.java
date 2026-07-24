module com.plociennik.vestal {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    requires java.prefs;

    opens com.plociennik.vestal to javafx.fxml;
    exports com.plociennik.vestal;
    exports com.plociennik.vestal.controller;
    opens com.plociennik.vestal.controller to javafx.fxml;
}