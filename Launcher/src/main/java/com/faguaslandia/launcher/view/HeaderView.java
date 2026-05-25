package com.faguaslandia.launcher.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class HeaderView extends HBox {

    private final Label lblBiblioteca;
    private final Label lblTienda;
    private final Label lblAmigos;
    private final Label lblPerfil;
    private final Label badgeAmigos;
    private final StackPane amigosWrapper;

    private Label activeLabel;

    public HeaderView() {
        getStyleClass().add("header");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(30);
        setPadding(new Insets(0, 40, 0, 40));

        Label logo = new Label("FAGUÁSLANDIA");
        logo.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 0 30 0 0;");

        lblBiblioteca = makeNav("Biblioteca");
        lblTienda     = makeNav("Tienda");
        lblAmigos     = makeNav("Amigos");
        lblPerfil     = makeNav("Perfil");

        badgeAmigos = new Label("");
        badgeAmigos.getStyleClass().add("nav-badge");
        badgeAmigos.setVisible(false);
        badgeAmigos.setManaged(false);
        badgeAmigos.setMouseTransparent(true);

        amigosWrapper = new StackPane(lblAmigos, badgeAmigos);
        amigosWrapper.setAlignment(Pos.CENTER);
        StackPane.setAlignment(badgeAmigos, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeAmigos, new Insets(4, -4, 0, 0));

        setActive(lblBiblioteca);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(logo, lblBiblioteca, lblTienda, amigosWrapper, lblPerfil, spacer);
    }

    public void setBadgeAmigos(int total) {
        if (total <= 0) {
            badgeAmigos.setVisible(false);
            badgeAmigos.setManaged(false);
        } else {
            badgeAmigos.setText(total > 99 ? "99+" : String.valueOf(total));
            badgeAmigos.setVisible(true);
            badgeAmigos.setManaged(true);
        }
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
                lbl.setStyle(null);
        });

        return lbl;
    }

    private void setActive(Label lbl) {
        if (activeLabel != null) {
            activeLabel.getStyleClass().remove("header-label-active");
            activeLabel.setStyle(null);
        }

        activeLabel = lbl;
        lbl.getStyleClass().add("header-label-active");
        lbl.setStyle(
                "-fx-text-fill: #e6f0f8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 18 0 18 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: transparent transparent #66c0f4 transparent;" +
                        "-fx-border-width: 0 0 2 0;"
        );
    }

    public void setActions(Runnable bibliotecaAction, Runnable tiendaAction,
                           Runnable amigosAction, Runnable perfilAction) {

        if (bibliotecaAction != null)
            lblBiblioteca.setOnMouseClicked(e -> { setActive(lblBiblioteca); bibliotecaAction.run(); });

        if (tiendaAction != null)
            lblTienda.setOnMouseClicked(e -> { setActive(lblTienda); tiendaAction.run(); });

        if (amigosAction != null) {
            amigosWrapper.setOnMouseClicked(e -> { setActive(lblAmigos); amigosAction.run(); });
            amigosWrapper.setStyle("-fx-cursor: hand;");
        }

        if (perfilAction != null)
            lblPerfil.setOnMouseClicked(e -> { setActive(lblPerfil); perfilAction.run(); });
    }

    public void setActions(Runnable bibliotecaAction, Runnable tiendaAction, Runnable perfilAction) {
        setActions(bibliotecaAction, tiendaAction, null, perfilAction);
    }

    public void activarAmigos() {
        setActive(lblAmigos);
    }
}