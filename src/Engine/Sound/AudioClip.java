package Engine.Sound;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;

/**
 * Audio clip that holds the audio information.
 */
public class AudioClip {
    private AudioFormat format;
    private byte[] audioData;
    private Clip clip;

    /**
     * Loads a new audio from given file.
     * @param filePath file path
     */
    public AudioClip(String filePath) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat baseFormat = stream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, stream);
            audioData = decodedStream.readAllBytes();
            format = decodedFormat;
            clip = createClip(false);
            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(volume.getMinimum());
            clip.start();
        } catch (Exception e) {
            System.err.println("Failed to load audio clip : " + e.getMessage());
        }

    }

    /**
     * Creates a new Clip from audioData.
     * @param loop if the audio should loop
     * @return Clip
     */
    public Clip createClip(boolean loop) throws LineUnavailableException, IOException {
        Clip clip = AudioSystem.getClip();
        clip.open(format, audioData, 0, audioData.length);
        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        return clip;
    }
}
