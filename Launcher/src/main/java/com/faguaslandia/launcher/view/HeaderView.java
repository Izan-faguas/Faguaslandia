package com.faguaslandia.launcher.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HeaderView extends HBox {

    private final Label lblBiblioteca;
    private final Label lblTienda;
    private final Label lblPerfil;

    private Label activeLabel;

    public HeaderView() {
        getStyleClass().add("header");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(30);
        setPadding(new Insets(0, 40, 0, 40));

        // Logo izquierda
        Label logo = new Label("FAGUÁSLANDIA");
        logo.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 0 30 0 0;");

        lblBiblioteca = makeNav("Biblioteca");
        lblTienda     = makeNav("Tienda");
        lblPerfil     = makeNav("Perfil");

        // Activar Biblioteca por defecto
        setActive(lblBiblioteca);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(logo, lblBiblioteca, lblTienda, lblPerfil, spacer);
    }

    private Label makeNav(String texto) {
        Label lbl = new Label(texto);
        lbl.getStyleClass().add("header-label");
        lbl.setOnMouseEntered(e -> {
            if (lbl != activeLabel)
                lbl.setStyle("-fx-text-fill: #e6f0f8; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 18 0 18 0; -fx-cursor: hand;");
        });
        lbl.setOnMouseExited(e -> {
            if (lbl != activeLabel)
                lbl.setStyle("");
        });
        return lbl;
    }

    private void setActive(Label lbl) {
        if (activeLabel != null) {
            activeLabel.getStyleClass().remove("header-label-active");
            activeLabel.setStyle("");
        }
        activeLabel = lbl;
        lbl.getStyleClass().add("header-label-active");
        lbl.setStyle("-fx-text-fill: #e6f0f8; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 18 0 18 0; -fx-cursor: hand; -fx-border-color: transparent transparent #66c0f4 transparent; -fx-border-width: 0 0 2 0;");
    }

    public void setActions(Runnable bibliotecaAction, Runnable tiendaAction, Runnable perfilAction) {
        if (bibliotecaAction != null) lblBiblioteca.setOnMouseClicked(e -> { setActive(lblBiblioteca); bibliotecaAction.run(); });
        if (tiendaAction != null)     lblTienda.setOnMouseClicked(e -> { setActive(lblTienda); tiendaAction.run(); });
        if (perfilAction != null)     lblPerfil.setOnMouseClicked(e -> { setActive(lblPerfil); perfilAction.run(); });
    }
}