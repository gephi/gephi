/*
 * gleem -- OpenGL Extremely Easy-To-Use Manipulators.
 * Copyright (C) 1998-2003 Kenneth B. Russell (kbrussel@alum.mit.edu)
 *
 * Copying, distribution and use of this software in source and binary
 * forms, with or without modification, is permitted provided that the
 * following conditions are met:
 *
 * Distributions of source code must reproduce the copyright notice,
 * this list of conditions and the following disclaimer in the source
 * code header files; and Distributions of binary code must reproduce
 * the copyright notice, this list of conditions and the following
 * disclaimer in the documentation, Read me file, license file and/or
 * other materials provided with the software distribution.
 *
 * The names of Sun Microsystems, Inc. ("Sun") and/or the copyright
 * holder may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," WITHOUT A WARRANTY OF ANY
 * KIND. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INTERFERENCE, ACCURACY OF
 * INFORMATIONAL CONTENT OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. THE
 * COPYRIGHT HOLDER, SUN AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL THE
 * COPYRIGHT HOLDER, SUN OR SUN'S LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES. YOU ACKNOWLEDGE THAT THIS SOFTWARE IS NOT
 * DESIGNED, LICENSED OR INTENDED FOR USE IN THE DESIGN, CONSTRUCTION,
 * OPERATION OR MAINTENANCE OF ANY NUCLEAR FACILITY. THE COPYRIGHT
 * HOLDER, SUN AND SUN'S LICENSORS DISCLAIM ANY EXPRESS OR IMPLIED
 * WARRANTY OF FITNESS FOR SUCH USES.
 */

package gephi.visualization.opengl.gleem.linalg;

/** Represents a rotation with single-precision components */

public class Rotf {
  private static float EPSILON = 1.0e-7f;

  // Representation is a quaternion. Element 0 is the scalar part (=
  // cos(theta/2)), elements 1..3 the imaginary/"vector" part (=
  // sin(theta/2) * axis).
  private float q0;
  private float q1;
  private float q2;
  private float q3;

  /** Default constructor initializes to the identity quaternion */
  public Rotf() {
    init();
  }

  public Rotf(Rotf arg) {
    set(arg);
  }

  /** Axis does not need to be normalized but must not be the zero
      vector. Angle is in radians. */
  public Rotf(Vec3f axis, float angle) {
    set(axis, angle);
  }

  /** Creates a rotation which will rotate vector "from" into vector
      "to". */
  public Rotf(Vec3f from, Vec3f to) {
    set(from, to);
  }

  /** Re-initialize this quaternion to be the identity quaternion "e"
      (i.e., no rotation) */
  public void init() {
    q0 = 1;
    q1 = q2 = q3 = 0;
  }

  /** Test for "approximate equality" -- performs componentwise test
      to see whether difference between all components is less than
      epsilon. */
  public boolean withinEpsilon(Rotf arg, float epsilon) {
    return ((Math.abs(q0 - arg.q0) < epsilon) &&
            (Math.abs(q1 - arg.q1) < epsilon) &&
            (Math.abs(q2 - arg.q2) < epsilon) &&
            (Math.abs(q3 - arg.q3) < epsilon));
  }

  /** Axis does not need to be normalized but must not be the zero
      vector. Angle is in radians. */
  public void set(Vec3f axis, float angle) {
    float halfTheta = angle / 2.0f;
    q0 = (float) Math.cos(halfTheta);
    float sinHalfTheta = (float) Math.sin(halfTheta);
    Vec3f realAxis = new Vec3f(axis);
    realAxis.normalize();
    q1 = realAxis.x() * sinHalfTheta;
    q2 = realAxis.y() * sinHalfTheta;
    q3 = realAxis.z() * sinHalfTheta;
  }

  public void set(Rotf arg) {
    q0 = arg.q0;
    q1 = arg.q1;
    q2 = arg.q2;
    q3 = arg.q3;
  }

  /** Sets this rotation to that which will rotate vector "from" into
      vector "to". from and to do not have to be the same length. */
  public void set(Vec3f from, Vec3f to) {
    Vec3f axis = from.cross(to);
    if (axis.lengthSquared() < EPSILON) {
      init();
      return;
    }
    float dotp = from.dot(to);
    float denom = from.length() * to.length();
    if (denom < EPSILON) {
      init();
      return;
    }
    dotp /= denom;
    set(axis, (float) Math.acos(dotp));
  }

  /** Returns angle (in radians) and mutates the given vector to be
      the axis. */
  public float get(Vec3f axis) {
    // FIXME: Is this numerically stable? Is there a better way to
    // extract the angle from a quaternion?
    // NOTE: remove (float) to illustrate compiler bug
    float retval = (float) (2.0f * Math.acos(q0));
    axis.set(q1, q2, q3);
    float len = axis.length();
    if (len == 0.0f) {
      axis.set(0, 0, 1);
    } else {
      axis.scale(1.0f / len);
    }
    return retval;
  }

  /** Returns inverse of this rotation; creates new rotation */
  public Rotf inverse() {
    Rotf tmp = new Rotf(this);
    tmp.invert();
    return tmp;
  }

  /** Mutate this quaternion to be its inverse. This is equivalent to
      the conjugate of the quaternion. */
  public void invert() {
    q1 = -q1;
    q2 = -q2;
    q3 = -q3;
  }
  
  /** Length of this quaternion in four-space */
  public float length() {
    return (float) Math.sqrt(lengthSquared());
  }

  /** This dotted with this */
  public float lengthSquared() {
    return (q0 * q0 +
            q1 * q1 +
            q2 * q2 +
            q3 * q3);
  }

  /** Make this quaternion a unit quaternion again. If you are
      composing dozens of quaternions you probably should call this
      periodically to ensure that you have a valid rotation. */
  public void normalize() {
    float len = length();
    q0 /= len;
    q1 /= len;
    q2 /= len;
    q3 /= len;
  }

  /** Returns this * b, in that order; creates new rotation */
  public Rotf times(Rotf b) {
    Rotf tmp = new Rotf();
    tmp.mul(this, b);
    return tmp;
  }

  /** Compose two rotations: this = A * B in that order. NOTE that
      because we assume a column vector representation that this
      implies that a vector rotated by the cumulative rotation will be
      rotated first by B, then A. NOTE: "this" must be different than
      both a and b. */
  public void mul(Rotf a, Rotf b) {
    q0 = (a.q0 * b.q0 - a.q1 * b.q1 -
          a.q2 * b.q2 - a.q3 * b.q3);
    q1 = (a.q0 * b.q1 + a.q1 * b.q0 +
          a.q2 * b.q3 - a.q3 * b.q2);
    q2 = (a.q0 * b.q2 + a.q2 * b.q0 -
          a.q1 * b.q3 + a.q3 * b.q1);
    q3 = (a.q0 * b.q3 + a.q3 * b.q0 +
          a.q1 * b.q2 - a.q2 * b.q1);
  }

  /** Turns this rotation into a 3x3 rotation matrix. NOTE: only
      mutates the upper-left 3x3 of the passed Mat4f. Implementation
      from B. K. P. Horn's <u>Robot Vision</u> textbook. */
  public void toMatrix(Mat4f mat) {
    float q00 = q0 * q0;
    float q11 = q1 * q1;
    float q22 = q2 * q2;
    float q33 = q3 * q3;
    // Diagonal elements
    mat.set(0, 0, q00 + q11 - q22 - q33);
    mat.set(1, 1, q00 - q11 + q22 - q33);
    mat.set(2, 2, q00 - q11 - q22 + q33);
    // 0,1 and 1,0 elements
    float q03 = q0 * q3;
    float q12 = q1 * q2;
    mat.set(0, 1, 2.0f * (q12 - q03));
    mat.set(1, 0, 2.0f * (q03 + q12));
    // 0,2 and 2,0 elements
    float q02 = q0 * q2;
    float q13 = q1 * q3;
    mat.set(0, 2, 2.0f * (q02 + q13));
    mat.set(2, 0, 2.0f * (q13 - q02));
    // 1,2 and 2,1 elements
    float q01 = q0 * q1;
    float q23 = q2 * q3;
    mat.set(1, 2, 2.0f * (q23 - q01));
    mat.set(2, 1, 2.0f * (q01 + q23));
  }

  /** Turns the upper left 3x3 of the passed matrix into a rotation.
      Implementation from Watt and Watt, <u>Advanced Animation and
      Rendering Techniques</u>.
      @see gleem.linalg.Mat4f#getRotation */
  public void fromMatrix(Mat4f mat) {
    // FIXME: Should reimplement to follow Horn's advice of using
    // eigenvector decomposition to handle roundoff error in given
    // matrix.
    
    float tr, s;
    int i, j, k;
  
    tr = mat.get(0, 0) + mat.get(1, 1) + mat.get(2, 2);
    if (tr > 0.0) {
      s = (float) Math.sqrt(tr + 1.0f);
      q0 = s * 0.5f;
      s = 0.5f / s;
      q1 = (mat.get(2, 1) - mat.get(1, 2)) * s;
      q2 = (mat.get(0, 2) - mat.get(2, 0)) * s;
      q3 = (mat.get(1, 0) - mat.get(0, 1)) * s;
    } else {
      i = 0;
      if (mat.get(1, 1) > mat.get(0, 0))
        i = 1;
      if (mat.get(2, 2) > mat.get(i, i))
        i = 2;
      j = (i+1)%3;
      k = (j+1)%3;
      s = (float) Math.sqrt( (mat.get(i, i) - (mat.get(j, j) + mat.get(k, k))) + 1.0f);
      setQ(i+1, s * 0.5f);
      s = 0.5f / s;
      q0 = (mat.get(k, j) - mat.get(j, k)) * s;
      setQ(j+1, (mat.get(j, i) + mat.get(i, j)) * s);
      setQ(k+1, (mat.get(k, i) + mat.get(i, k)) * s);
    }
  }

  /** Rotate a vector by this quaternion. Implementation is from
      Horn's <u>Robot Vision</u>. NOTE: src and dest must be different
      vectors. */
  public void rotateVector(Vec3f src, Vec3f dest) {
    Vec3f qVec = new Vec3f(q1, q2, q3);
    Vec3f qCrossX = qVec.cross(src);
    Vec3f qCrossXCrossQ = qCrossX.cross(qVec);
    qCrossX.scale(2.0f * q0);
    qCrossXCrossQ.scale(-2.0f);
    dest.add(src, qCrossX);
    dest.add(dest, qCrossXCrossQ);
  }

  /** Rotate a vector by this quaternion, returning newly-allocated result. */
  public Vec3f rotateVector(Vec3f src) {
    Vec3f tmp = new Vec3f();
    rotateVector(src, tmp);
    return tmp;
  }

  public String toString() {
    return "(" + q0 + ", " + q1 + ", " + q2 + ", " + q3 + ")";
  }

  private void setQ(int i, float val) {
    switch (i) {
    case 0: q0 = val; break;
    case 1: q1 = val; break;
    case 2: q2 = val; break;
    case 3: q3 = val; break;
    default: throw new IndexOutOfBoundsException();
    }
  }
}
