package com.fleurairlines.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneTransitionUtil {

    public static void fadeTransition(Stage stage, Scene newScene, double durationMs) {
        Scene currentScene = stage.getScene();
        if (currentScene == null) {
            stage.setScene(newScene);
            newScene.getRoot().setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(durationMs), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            return;
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(durationMs * 0.4), currentScene.getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_OUT);

        fadeOut.setOnFinished(e -> {
            stage.setScene(newScene);
            newScene.getRoot().setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(durationMs * 0.6), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_IN);
            fadeIn.play();
        });
        fadeOut.play();
    }

    public static void slideTransition(Stage stage, Scene newScene, double durationMs) {
        double width = stage.getWidth() > 0 ? stage.getWidth() : 900;
        Scene currentScene = stage.getScene();

        if (currentScene == null) {
            fadeTransition(stage, newScene, durationMs);
            return;
        }

        Pane currentRoot = (Pane) currentScene.getRoot();
        Pane newRoot = (Pane) newScene.getRoot();

        newRoot.setTranslateX(width);
        newRoot.setOpacity(0.0);
        stage.setScene(newScene);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(currentRoot.translateXProperty(), 0),
                new KeyValue(newRoot.translateXProperty(), width),
                new KeyValue(newRoot.opacityProperty(), 0.0)
            ),
            new KeyFrame(Duration.millis(durationMs),
                new KeyValue(currentRoot.translateXProperty(), -width * 0.5, Interpolator.EASE_BOTH),
                new KeyValue(newRoot.translateXProperty(), 0, Interpolator.EASE_BOTH),
                new KeyValue(newRoot.opacityProperty(), 1.0, Interpolator.EASE_BOTH)
            )
        );
        timeline.play();
    }

    public static void quickFade(Stage stage, Scene newScene)    { fadeTransition(stage, newScene, 150); }
    public static void standardFade(Stage stage, Scene newScene) { fadeTransition(stage, newScene, 250); }
    public static void standardSlide(Stage stage, Scene newScene){ slideTransition(stage, newScene, 320); }
}