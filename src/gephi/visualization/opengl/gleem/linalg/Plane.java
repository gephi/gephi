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

/** Represents a plane in 3D space. */

public class Plane {
  /** Normalized */
  private Vec3f normal;
  private Vec3f point;
  /** Constant for faster projection and intersection */
  float c;

  /** Default constructor initializes normal to (0, 1, 0) and point to
      (0, 0, 0) */
  public Plane() {
    normal = new Vec3f(0, 1, 0);
    point = new Vec3f(0, 0, 0);
    recalc();
  }

  /** Sets all parameters of plane. Plane has normal <b>normal</b> and
      goes through the point <b>point</b>. Normal does not need to be
      unit length but must not be the zero vector. */
  public Plane(Vec3f normal, Vec3f point) {
    this.normal = new Vec3f(normal);
    this.normal.normalize();
    this.point = new Vec3f(point);
    recalc();
  }

  /** Setter does some work to maintain internal caches. Normal does
      not need to be unit length but must not be the zero vector. */
  public void setNormal(Vec3f normal) {
    this.normal.set(normal);
    this.normal.normalize();
    recalc();
  }

  /** Normal is normalized internally, so <b>normal</b> is not
      necessarily equal to <code>plane.setNormal(normal);
      plane.getNormal();</code> */
  public Vec3f getNormal() {
    return normal;
  }

  /** Setter does some work to maintain internal caches */
  public void setPoint(Vec3f point) {
    this.point.set(point);
    recalc();
  }

  public Vec3f getPoint() {
    return point;
  }

  /** Project a point onto the plane */
  public void projectPoint(Vec3f pt,
                           Vec3f projPt) {
    float scale = normal.dot(pt) - c;
    projPt.set(pt.minus(normal.times(normal.dot(point) - c)));
  }

  /** Intersect a ray with the plane. Returns true if intersection occurred, false
      otherwise. This is a two-sided ray cast. */
  public boolean intersectRay(Vec3f rayStart,
                              Vec3f rayDirection,
                              IntersectionPoint intPt) {
    float denom = normal.dot(rayDirection);
    if (denom == 0)
      return false;
    intPt.setT((c - normal.dot(rayStart)) / denom);
    intPt.setIntersectionPoint(rayStart.plus(rayDirection.times(intPt.getT())));
    return true;
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //
  
  private void recalc() {
    c = normal.dot(point);
  }
}
