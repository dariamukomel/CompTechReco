package display;

import java.awt.*;

public class MarkedPoint extends Point {
    public final boolean isSignal;

    public MarkedPoint(int x, int y, boolean isSignal) {
        super(x, y);
        this.isSignal = isSignal;
    }
}
