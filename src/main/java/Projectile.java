import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.EnumSet;
import java.util.Random;

public class Projectile extends Entity {
    PVector position;
    PVector velocity;

    PApplet applet;

    int range = new Random().nextInt(20) + 20;
    int extent = 10;

    public Projectile(PVector position, PVector velocity, PApplet applet) {
        this.position = position;
        this.velocity = velocity.rotate(PApplet.radians((new Random().nextFloat() - .5f) * 10));
        this.applet = applet;
    }

    public void update() {
        position.add(velocity);
        velocity.sub(0, -.05f);
    }

    public void show() {
        applet.pushStyle();
        applet.pushMatrix();
        applet.fill(200, 0, 0);
        applet.noStroke();
        applet.beginShape();
        applet.translate(position.x, position.y);
        applet.rotate(velocity.heading());
        applet.vertex(-2, 2);
        applet.vertex(-2, -2);
        applet.vertex(4, 0);
        applet.endShape(PConstants.CLOSE);
        applet.popStyle();
        applet.popMatrix();
    }

    public boolean completed() {
        return position.y > applet.height + 10 || position.x < -10 || position.x > applet.width + 10;
    }

    public boolean checkImpact(CellState[][] terrain, EnumSet<CellState> terrainTypes) {
        for (float i = position.y - extent / 2f; i < position.y + extent / 2f; i++) {
            for (float j = position.x - extent / 2f; j < position.x + extent / 2f; j++) {
                if (i >= 0 && i < applet.height && j > 0 && j < applet.width) {
                    if (terrainTypes.contains(terrain[(int) i][(int) j])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
