package org.gephi.lib.gleem.linalg;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilTest {

	@Test
	public void testClamp() {
		Assert.assertEquals(0, MathUtil.clamp(0, 0, 0));
		Assert.assertEquals(0, MathUtil.clamp(1, 0, 0));
		Assert.assertEquals(0.0f,
			MathUtil.clamp(0.0f, 0.0f, 0.0f), 0.0f);
		Assert.assertEquals(0.0f,
			MathUtil.clamp(0x1p-149f, 0.0f, 0.0f), 0.0f);
		Assert.assertEquals(-0.0f,
			MathUtil.clamp(-0x1p-149f, -0.0f, -0.0f), 0.0f);
		Assert.assertEquals(-2_013_331_714,
			MathUtil.clamp(-2_030_108_929, -2_013_331_714, -2_030_108_930));
	}

	@Test
	public void testMakePerpendicular1() {
		Vec3f dest = new Vec3f(0.0f, 0.0f, 0.0f);

		Assert.assertTrue(MathUtil.makePerpendicular(new Vec3f(-2.0f, Float.NaN, -0.0f), dest));

		Assert.assertEquals(0.0f, dest.get(2), 0.0f);
		Assert.assertEquals(Float.NaN, dest.get(0), 0.0f);
		Assert.assertEquals(-2.0f, dest.get(1), 0.0f);
	}

	@Test
	public void testMakePerpendicular2() {
		Vec3f dest = new Vec3f(0.0f, 0.0f, 0.0f);

		Assert.assertTrue(MathUtil.makePerpendicular(new Vec3f(-2.0f, 0.0f, -0.0f), dest));

		Assert.assertEquals(-2.0f, dest.get(2), 0.0f);
		Assert.assertEquals(0.0f, dest.get(0), 0.0f);
		Assert.assertEquals(0.0f, dest.get(1), 0.0f);
	}

	@Test
	public void testMakePerpendicular3() {
		Vec3f dest = new Vec3f(0.0f, 0.0f, 0.0f);

		Assert.assertTrue(MathUtil.makePerpendicular(new Vec3f(0.0f, 0.0f, -0x1p-149f), dest));

		Assert.assertEquals(0.0f, dest.get(2), 0.0f);
		Assert.assertEquals(1.0f, dest.get(0), 0.0f);
		Assert.assertEquals(0.0f, dest.get(1), 0.0f);
	}

	@Test
	public void testMakePerpendicular4() {
		Assert.assertFalse(MathUtil.makePerpendicular(new Vec3f(0.0f, -0.0f, -0.0f), null));
	}

	@Test
	public void testSgn() {
		Assert.assertEquals(1, MathUtil.sgn(0x1.01p+0f));
		Assert.assertEquals(0, MathUtil.sgn(0.0f));
		Assert.assertEquals(-1, MathUtil.sgn(-0x1p-149f));
	}
}
