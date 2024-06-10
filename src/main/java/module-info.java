module pk.wieik.raidjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    opens pk.wieik.raidjavafx to javafx.fxml;
    exports pk.wieik.raidjavafx;
}