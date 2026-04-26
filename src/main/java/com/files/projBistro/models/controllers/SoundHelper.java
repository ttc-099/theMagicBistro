package com.files.projBistro.models.controllers;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Random;

public class SoundHelper {

    private static final Random random = new Random();

    public static void playTapSound() {
        try {
            String soundFile = random.nextBoolean() ? "/audio/tap1.mp3" : "/audio/tap2.mp3";
            Media sound = new Media(SoundHelper.class.getResource(soundFile).toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            // Silent fail
        }
    }

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