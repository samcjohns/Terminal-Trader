package tetrad;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Dedicated audio class for starting and stopping audio playback.
 * 
 * @author Samuel Johns
 * Created: November 27, 2024
 */

public class SoundPlayer {
    private Clip clip;

    /**
     * Loads a sound file from the given file path.
     *
     * @param filePath The path to the sound file (.wav format).
     */
    public SoundPlayer(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.err.println("Error loading sound file: " + filePath);
        }
    }

    /**
     * Plays the sound from the beginning.
     * If the sound is already playing, it restarts it.
     */
    public void play() {
        if (clip != null) {
            stop(); // Stops any current playback before restarting
            clip.setFramePosition(0); // Rewind to the beginning
            clip.start();
        }
    }

    /**
     * Stops the sound if it is playing.
     */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * Loops the sound continuously.
     */
    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Releases system resources associated with this sound player.
     */
    public void close() {
        if (clip != null) {
            clip.close();
        }
    }

    /**
     * Checks if the sound is currently playing.
     *
     * @return True if the sound is playing, false otherwise.
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public static void main(String[] args) {
        // Example usage
        SoundPlayer player = new SoundPlayer("path_to_your_sound.wav");

        player.play(); // Play sound
        try {
            Thread.sleep(5000); // Wait for 5 seconds while sound plays
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        player.stop(); // Stop sound
    }
}

