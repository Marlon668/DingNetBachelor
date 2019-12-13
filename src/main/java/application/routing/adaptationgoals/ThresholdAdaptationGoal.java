package application.routing.adaptationgoals;

/**
 * An adaptation goal with a threshold.
 */
public class ThresholdAdaptationGoal extends AdaptationGoal {
    /**
     * The threshold of the goal.
     */
    private final double threshold;

    /**
     * A constructor generating a ThresholdAdaptationGoal with given threshold.
     * This is a treshold to determine if we must switch the path
     * @param threshold
     */
    public ThresholdAdaptationGoal(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the threshhold.
     * @return
     */
    public double getThreshold() {
        return threshold;
    }


    public String toString() {
        return Double.toString(this.threshold);
    }
}
