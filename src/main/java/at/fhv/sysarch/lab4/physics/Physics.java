package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;

public class Physics implements ContactListener, StepListener {

    private World world;

    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
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
        System.out.println("Contact");
        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
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
        Vector2 directedForce = startPoint.difference(endPoint);

        Ray ray = new Ray(startPoint, directedForce);
        ArrayList<RaycastResult> results = new ArrayList<>();
        this.world.raycast(ray, 0.2, false, true, results);

        for (RaycastResult result : results) {
            if (result.getBody().getUserData() instanceof Ball) {
                System.out.println("We hit a ball");
                result.getBody().applyForce(directedForce.multiply(400));
                break;
            }
        }
    }
}
