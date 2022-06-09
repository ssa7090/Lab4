package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.fhv.sysarch.lab4.physics.*;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

public class Game implements BallStrikeListener, BallsCollisionListener, BallPocketedListener, ObjectsRestListener {
    private final Renderer renderer;
    private final Physics physics;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.initWorld();

        this.physics.setBallStrikeListener(this);
        this.physics.setBallPocketedListener(this);
        this.physics.setObjectsRestListener(this);
        this.physics.setBallsCollisionListener(this);
    }

    private Point2D mousePressedAt;

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        this.mousePressedAt = new Point2D(x, y);
    }

    public void onMouseReleased(MouseEvent e) {
        // press point
        double xStart = this.mousePressedAt.getX();
        double yStart = this.mousePressedAt.getY();
        double pXStart = this.renderer.screenToPhysicsX(xStart);
        double pYStart = this.renderer.screenToPhysicsY(yStart);

        // release point
        double endX = e.getX();
        double endY = e.getY();
        double pEndX = this.renderer.screenToPhysicsX(endX);
        double pEndY = this.renderer.screenToPhysicsY(endY);

        this.renderer.setCuePoints(null);

        if (!moveActive) {
            this.physics.strikeBall(pXStart, pYStart, pEndX, pEndY);
        }
    }

    public void onMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

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

    private int player1Score = 0;
    private int player2Score = 0;
    private boolean player1Turn = true;
    private boolean moveActive = false;
    private boolean foul = false;
    private int score = 0;

    @Override
    public void onBallStrike(Ball b) {
        if (foul) {
            return;
        }

        if (!b.isWhite()) {
            foul("Non-white ball has been stricken");
        }
    }

    @Override
    public boolean onBallPocketed(Ball b) {
        this.physics.getWorld().removeBody(b.getBody());
        this.renderer.removeBall(b);

        if (foul) {
            return true;
        }

        if (b.isWhite()) {
            foul("White ball pocketed");

        } else {
            score++;
        }

        return true;
    }

    private void foul(String message) {
        this.renderer.setFoulMessage(message);
        foul = true;
    }

    @Override
    public void onBallsCollide(Ball b1, Ball b2) {
        System.out.println("Collision of balls");
    }

    @Override
    public void onEndAllObjectsRest() {
        System.out.println("Simulation step started");
        moveActive = true;
    }

    @Override
    public void onStartAllObjectsRest() {
        System.out.println("Simulation step ended");
        moveActive = false;

        if (foul) {
            score = -1;
            player1Turn = !player1Turn;
        } else {
            if (score == 0) {
                player1Turn = !player1Turn;
            }
        }

        if (this.player1Turn) {
            this.player1Score += score;
            this.renderer.setPlayer1Score(player1Score);
        } else {
            this.player2Score += score;
            this.renderer.setPlayer2Score(player2Score);
        }

        foul = false;
        score = 0;
    }
}