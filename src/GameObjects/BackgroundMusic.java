package GameObjects;

import Engine.GameObject;
import Engine.Sound.AudioClip;
import Engine.Sound.AudioPlayer;

public class BackgroundMusic extends GameObject {
    AudioClip backgroundMusic = new AudioClip("src\\Assets\\audio\\backgroundMusic.wav");

    @Override
    public void setup() {
        AudioPlayer.playAudio(backgroundMusic, true);
    }
}
