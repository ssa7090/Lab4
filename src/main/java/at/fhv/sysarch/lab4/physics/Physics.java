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
import java.util.concurrent.atomic.AtomicInteger;

public class Physics implements ContactListener, StepListener {

    private static double WORLD_STEP_FREQUENCY_MOD_FACTOR = 1.5;

    private World world;

    private BallStrikeListener ballStrikeListener;
    private BallsCollisionListener ballsCollisionListener;
    private BallPocketedListener ballPocketedListener;
    private ObjectsRestListener objectsRestListener;

    public Physics() {
        this.world = new World();

        Settings worldSettings = new Settings();
        worldSettings.setStepFrequency(worldSettings.getStepFrequency() * WORLD_STEP_FREQUENCY_MOD_FACTOR);
        this.world.setSettings(worldSettings);

        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    public void setBallStrikeListener(BallStrikeListener ballStrikeListener) {
        this.ballStrikeListener = ballStrikeListener;
    }

    public void setBallsCollisionListener(BallsCollisionListener ballsCollisionListener) {
        this.ballsCollisionListener = ballsCollisionListener;
    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }

    public void setObjectsRestListener(ObjectsRestListener objectsRestListener) {
        this.objectsRestListener = objectsRestListener;
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

    private boolean objectsRested = true;

    @Override
    public void end(Step step, World world) {
        AtomicInteger activeBodies = new AtomicInteger();
        world.getBodies().stream()
                .forEach(body -> {
                    if (body.getLinearVelocity().getMagnitude() != 0) {
                        activeBodies.getAndIncrement();
                    }
                });

        if (activeBodies.get() != 0) {
            // animation active
            if (objectsRested) {
                this.objectsRestListener.onEndAllObjectsRest();
                objectsRested = false;
            }
        } else {
            // animation inactive
            if (!objectsRested) {
                this.objectsRestListener.onStartAllObjectsRest();
                objectsRested = true;
            }
        }
    }

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public boolean begin(ContactPoint point) {
        System.out.println("Contact");

        Object contactObj1 = point.getBody1().getUserData();
        Object contactObj2 = point.getBody2().getUserData();

        if (contactObj1 instanceof Ball && contactObj2 instanceof Ball) {
            this.ballsCollisionListener.onBallsCollide((Ball) contactObj1, (Ball) contactObj2);
        }

        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
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
                this.ballStrikeListener.onBallStrike((Ball) result.getBody().getUserData());
                return;
            }
        }

        System.out.println("No ball has been hit");
    }
}
