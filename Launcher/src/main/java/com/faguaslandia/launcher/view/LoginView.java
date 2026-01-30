package com.faguaslandia.launcher.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {

    private TextField usuario;
    private PasswordField password;
    private Button loginBtn;
    private Label mensaje;

    public LoginView() {
        setSpacing(10);
        setPadding(new Insets(20));

        usuario = new TextField();
        usuario.setPromptText("Usuario");

        password = new PasswordField();
        password.setPromptText("Contraseña");

        loginBtn = new Button("Entrar");
        mensaje = new Label();

        getChildren().addAll(usuario, password, loginBtn, mensaje);
    }

    public TextField getUsuario() { return usuario; }
    public PasswordField getPassword() { return password; }
    public Button getLoginBtn() { return loginBtn; }
    public Label getMensaje() { return mensaje; }
}
