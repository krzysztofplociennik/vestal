module com.plociennik.vestal {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.plociennik.vestal to javafx.fxml;
    exports com.plociennik.vestal;
    exports com.plociennik.vestal.controller;
    opens com.plociennik.vestal.controller to javafx.fxml;
}