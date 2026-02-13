package com.faguaslandia.launcher.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HeaderView extends HBox {

    private final Label lblBiblioteca;
    private final Label lblTienda;
    private final Label lblPerfil;

    public HeaderView() {
        setSpacing(30);
        setPadding(new Insets(15));
        getStyleClass().add("header");

        lblBiblioteca = new Label("Biblioteca");
        lblTienda = new Label("Tienda");
        lblPerfil = new Label("Perfil");

        lblBiblioteca.getStyleClass().add("header-label");
        lblTienda.getStyleClass().add("header-label");
        lblPerfil.getStyleClass().add("header-label");

        getChildren().addAll(lblBiblioteca, lblTienda, lblPerfil);
    }

    public void setActions(Runnable bibliotecaAction, Runnable tiendaAction, Runnable perfilAction) {

        if (bibliotecaAction != null) {
            lblBiblioteca.setOnMouseClicked(e -> bibliotecaAction.run());
        }

        if (tiendaAction != null) {
            lblTienda.setOnMouseClicked(e -> tiendaAction.run());
        }

        if (perfilAction != null) {
            lblPerfil.setOnMouseClicked(e -> perfilAction.run());
        }
    }

}
