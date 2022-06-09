package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import at.fhv.sysarch.lab4.game.Table;
import org.dyn4j.dynamics.*;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Physics implements ContactListener, StepListener {

    private World world;
    private BallPocketedListener ballPocketedListener;

    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void begin(Step step, World world) {

    }

    @Override
    public void updatePerformed(Step step, World world) {

    }

    @Override
    public void postSolve(Step step, World world) {

    }

    @Override
    public void end(Step step, World world) {

    }

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public boolean begin(ContactPoint point) {
        System.out.println("Initial contact");
        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        System.out.println("Persistent contact");

        if (point.isSensor()) {
            Object contactObj1 = Objects.requireNonNullElse(point.getBody1().getUserData(), point.getFixture1().getUserData());
            Object contactObj2 = Objects.requireNonNullElse(point.getBody2().getUserData(), point.getFixture2().getUserData());

            Ball ball = null;
            if (contactObj1 instanceof Ball && contactObj2.equals(Table.TablePart.POCKET)) {
                ball = (Ball) contactObj1;
            } else if (contactObj2 instanceof Ball && contactObj1.equals(Table.TablePart.POCKET)) {
                ball = (Ball) contactObj2;
            }

            if (ball != null && point.getDepth() >= 0.08) {
                System.out.println("Ball pocketed");
                if (this.ballPocketedListener != null) {
                    this.ballPocketedListener.onBallPocketed(ball);
                }
            }
        }

        return true;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }

    public void strikeBall(double xCueStart, double yCueStart, double xCueEnd, double yCueEnd) {
        Vector2 startPoint = new Vector2(xCueStart, yCueStart);
        Vector2 endPoint = new Vector2(xCueEnd, yCueEnd);
        Vector2 directedForce = startPoint.difference(endPoint);;

        Ray ray = new Ray(startPoint, directedForce);
        ArrayList<RaycastResult> results = new ArrayList<>();
        this.world.raycast(ray, 0.3, false, true, results);
        Collections.sort(results);

        System.out.println("Striking ball");

        for (RaycastResult result : results) {
            if (result.getBody().getUserData() instanceof Ball) {
                System.out.println("A ball has been hit");
                result.getBody().applyForce(directedForce.multiply(400));
                return;
            }
        }

        System.out.println("No ball has been hit");
    }
}
