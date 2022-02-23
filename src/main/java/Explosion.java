import processing.core.PApplet;
import processing.core.PVector;

public class Explosion extends Entity {
    PVector position;

    int range;

    float currentRange;

    PApplet applet;

    public Explosion(PVector position, int range, PApplet applet) {
        this.position = position;

        this.range = range;
        currentRange = 0;

        this.applet = applet;
    }

    public void update() {
        currentRange += 2;
    }

    public void show() {
        applet.noStroke();
        applet.fill(255, 200, 200);
        applet.ellipse(position.x, position.y, currentRange * 2, currentRange * 2);
    }

    public boolean completed() {
        return currentRange >= range;
    }
}
