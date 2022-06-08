package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

public class Game {
    private final Renderer renderer;
    private final Physics physics;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.initWorld();
    }

    private Point2D mousePressedAt;

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        this.mousePressedAt = new Point2D(x, y);
    }

    public void onMouseReleased(MouseEvent e) {
        this.renderer.setCuePoints(null);

        // start point
        double xStart = this.mousePressedAt.getX();
        double yStart = this.mousePressedAt.getY();
        double pXStart = this.renderer.screenToPhysicsX(xStart);
        double pYStart = this.renderer.screenToPhysicsY(yStart);
        Vector2 startPoint = new Vector2(pXStart, pYStart);

        // release point
        double endX = e.getX();
        double endY = e.getY();
        double pEndX = this.renderer.screenToPhysicsX(endX);
        double pEndY = this.renderer.screenToPhysicsY(endY);
        Vector2 direction = startPoint.difference(pEndX, pEndY);

        Ray ray = new Ray(startPoint, direction);
        ArrayList<RaycastResult> results = new ArrayList<>();
        this.physics.getWorld().raycast(ray, 0.2, false, true, results);

        for (RaycastResult result : results) {
            if (result.getBody().getUserData() instanceof Ball) {
                System.out.println("We hit a ball");
                result.getBody().applyForce(direction.multiply(400));
                break;
            }
        }
    }

    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

        this.renderer.setCuePoints(new Pair<>(this.mousePressedAt, new Point2D(x, y)));
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2*Ball.Constants.RADIUS*2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
        }
       
        this.placeBalls(balls);
        balls.forEach(ball -> physics.getWorld().addBody(ball.getBody()));

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);
        
        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }
}