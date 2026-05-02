package org.gephi.appearance;

import org.gephi.appearance.api.Interpolator;
import org.junit.Assert;
import org.junit.Test;

public class InterpolatorTest {

    // ---- toString ----

    @Test
    public void testLinearToString() {
        Assert.assertEquals("LINEAR", Interpolator.LINEAR.toString());
    }

    @Test
    public void testLog2ToString() {
        Assert.assertEquals("LOG2", Interpolator.LOG2.toString());
    }

    @Test
    public void testBezierToString() {
        Interpolator.BezierInterpolator bi = new Interpolator.BezierInterpolator(0.1f, 0.2f, 0.8f, 0.9f);
        Assert.assertEquals("BEZIER:0.1,0.2,0.8,0.9", bi.toString());
    }

    // ---- fromString ----

    @Test
    public void testFromStringLinear() {
        Assert.assertSame(Interpolator.LINEAR, Interpolator.fromString("LINEAR"));
    }

    @Test
    public void testFromStringLog2() {
        Assert.assertSame(Interpolator.LOG2, Interpolator.fromString("LOG2"));
    }

    @Test
    public void testFromStringNull() {
        Assert.assertSame(Interpolator.LINEAR, Interpolator.fromString(null));
    }

    @Test
    public void testFromStringEmpty() {
        Assert.assertSame(Interpolator.LINEAR, Interpolator.fromString(""));
    }

    @Test
    public void testFromStringUnknown() {
        Assert.assertSame(Interpolator.LINEAR, Interpolator.fromString("UNKNOWN"));
    }

    @Test
    public void testFromStringMalformedBezier() {
        Assert.assertSame(Interpolator.LINEAR, Interpolator.fromString("BEZIER:not,valid"));
    }

    @Test
    public void testFromStringBezier() {
        Interpolator result = Interpolator.fromString("BEZIER:0.1,0.2,0.8,0.9");
        Assert.assertTrue(result instanceof Interpolator.BezierInterpolator);
        Interpolator.BezierInterpolator bi = (Interpolator.BezierInterpolator) result;
        Assert.assertEquals(new java.awt.geom.Point2D.Float(0.1f, 0.2f), bi.getControl1());
        Assert.assertEquals(new java.awt.geom.Point2D.Float(0.8f, 0.9f), bi.getControl2());
    }

    // ---- Round-trip ----

    @Test
    public void testLinearRoundTrip() {
        Assert.assertSame(Interpolator.LINEAR, Interpolator.fromString(Interpolator.LINEAR.toString()));
    }

    @Test
    public void testLog2RoundTrip() {
        Assert.assertSame(Interpolator.LOG2, Interpolator.fromString(Interpolator.LOG2.toString()));
    }

    @Test
    public void testBezierRoundTrip() {
        Interpolator.BezierInterpolator original = new Interpolator.BezierInterpolator(0.25f, 0.1f, 0.75f, 0.9f);
        Interpolator restored = Interpolator.fromString(original.toString());
        Assert.assertEquals(original, restored);
    }

    @Test
    public void testBezierRoundTripInterpolation() {
        Interpolator.BezierInterpolator original = new Interpolator.BezierInterpolator(0.25f, 0.1f, 0.75f, 0.9f);
        Interpolator restored = Interpolator.fromString(original.toString());
        Assert.assertEquals(original.interpolate(0.5f), restored.interpolate(0.5f), 1e-6f);
    }
}
