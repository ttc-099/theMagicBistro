package com.files.projBistro.controllers;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundHelper {

    public static void playSuccess() {
        try {
            Media sound = new Media(SoundHelper.class.getResource("/audio/orderSuccess.mp3").toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            // Silent fail
        }
    }
}