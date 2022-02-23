import processing.core.PApplet;
import processing.core.PVector;

public class Player extends Entity {
    PVector position;

    PApplet applet;

    public Player(int x, int y, PApplet applet) {
        position = new PVector(x, y);

        this.applet = applet;
    }

    public void show() {
        applet.fill(0);
        applet.ellipse(position.x, position.y, 20, 20);
    }

    boolean completed() {
        return false;
    }

    public void update() {

    }
}
