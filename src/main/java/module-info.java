module com.example.java2homework2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires java.sql;


    opens com.example.java2homework2 to javafx.fxml;
    exports com.example.java2homework2;
}