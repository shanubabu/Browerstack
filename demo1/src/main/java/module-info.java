module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires google.cloud.translate;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.support;


    opens com.example.demo1 to javafx.fxml;
    exports com.example.demo1;
}