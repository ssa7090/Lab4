package at.fhv.sysarch.lab4.rendering;

@FunctionalInterface
public interface FrameListener {
    void onFrame(double dt);
}