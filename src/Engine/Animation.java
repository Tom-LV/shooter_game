package Engine;

import java.util.ArrayList;

/**
 * Animation class that keeps track of all the animation frames.
 */
public class Animation {
    public ArrayList<String> frameNames = new ArrayList<>();
    private ArrayList<Float> frameDelays = new ArrayList<>();

    /**
     * Adds a new animation frame at the end.
     * @param name sprite name
     * @param delay delay before changing sprite
     */
    public void addFrame(String name, float delay) {
        frameNames.add(name);
        frameDelays.add(delay);
    }

    public String getFrameName(int index) {
        return frameNames.get(index);
    }

    public float getFrameDelay(int index) {
        return frameDelays.get(index);
    }

    public boolean hasNextFrame(int index) {
        return index < frameNames.size() - 1;
    }
}
