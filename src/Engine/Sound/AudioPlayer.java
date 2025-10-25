package Engine.Sound;

import java.util.ArrayList;
import javax.sound.sampled.Clip;

/**
 * Audio player class, to play audio in game.
 */
public class AudioPlayer {
    static ArrayList<Clip> activeClips = new ArrayList<>();

    /**
     * Plays the given AudioClip.
     * @param audioClip audio clip
     * @param loop true if it should loop
     * @return Clip
     */
    public static Clip playAudio(AudioClip audioClip, boolean loop) {
        AudioPlayer.cleanUp();
        try {
            Clip clip = audioClip.createClip(loop);
            
            clip.start();

            activeClips.add(clip);

            return clip;
        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cleans up any finished clips in activeClips array.
     */
    public static void cleanUp() {
        ArrayList<Clip> toRemoveClip = new ArrayList<>();
        for (int i = 0; i < activeClips.size(); i++) {
            Clip clip = activeClips.get(i);
            if (!clip.isActive()) {
                clip.close();
                toRemoveClip.add(clip);
            }
        }

        for (int i = 0; i < toRemoveClip.size(); i++) {
            activeClips.remove(toRemoveClip.get(i));
        }
    }

    /**
     * Stops playing the given clip.
     * @param audioClip clip
     */
    public static void stopAudio(Clip audioClip) {
        audioClip.stop();
        audioClip.close();
        activeClips.remove(audioClip);
    }

    /**
     * Stops all active clips.
     */
    public static void stopAll() {
        for (Clip clip : activeClips) {
            clip.stop();
            clip.close();
        }
        activeClips.clear();
    }
}
