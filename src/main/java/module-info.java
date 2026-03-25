module smartpay.lbycpd2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens smartpay.lbycpd2 to javafx.fxml;
    exports smartpay.lbycpd2;
    exports smartpay.lbycpd2.models;
    opens smartpay.lbycpd2.models to javafx.fxml;
    exports smartpay.lbycpd2.controllers;
    opens smartpay.lbycpd2.controllers to javafx.fxml;
    exports smartpay.lbycpd2.services;
    opens smartpay.lbycpd2.services to javafx.fxml;
}