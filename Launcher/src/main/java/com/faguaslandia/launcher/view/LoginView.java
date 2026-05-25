package com.faguaslandia.launcher.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView extends StackPane {

    private TextField usuario;
    private PasswordField password;
    private Button loginBtn;
    private Label mensaje;

    public LoginView() {
        getStyleClass().add("login-container");
        setAlignment(Pos.CENTER);

        VBox box = new VBox(16);
        box.getStyleClass().add("login-box");
        box.setAlignment(Pos.CENTER);

        Label logo = new Label("Faguáslandia");
        logo.getStyleClass().add("login-title");

        Label sub = new Label("Inicia sesión para acceder a tu biblioteca");
        sub.getStyleClass().add("login-subtitle");

        usuario = new TextField();
        usuario.setPromptText("Correo electrónico");
        usuario.getStyleClass().add("login-field");

        password = new PasswordField();
        password.setPromptText("Contraseña");
        password.getStyleClass().add("login-field");

        loginBtn = new Button("INICIAR SESIÓN");
        loginBtn.getStyleClass().add("btn-login");

        mensaje = new Label();
        mensaje.getStyleClass().add("login-error");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(300);

        box.getChildren().addAll(logo, sub, usuario, password, loginBtn, mensaje);

        getChildren().add(box);
    }

    public TextField getUsuario()     { return usuario; }
    public PasswordField getPassword(){ return password; }
    public Button getLoginBtn()       { return loginBtn; }
    public Label getMensaje()         { return mensaje; }
}