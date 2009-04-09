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

package org.gephi.visualization.opengl.gleem.linalg;

/** This differs from the Plane class in that it maintains an origin
    and orthonormal U, V axes in the plane so that it can project a 3D
    point to a 2D one. U cross V = normal. U and V coordinates are
    computed with respect to the origin. */

public class PlaneUV {
  private Vec3f origin = new Vec3f();
  /** Normalized */
  private Vec3f normal = new Vec3f();
  private Vec3f uAxis  = new Vec3f();
  private Vec3f vAxis  = new Vec3f();

  /** Default constructor initializes normal to (0, 1, 0), origin to
      (0, 0, 0), U axis to (1, 0, 0) and V axis to (0, 0, -1). */
  public PlaneUV() {
    setEverything(new Vec3f(0, 1, 0),
                  new Vec3f(0, 0, 0),
                  new Vec3f(1, 0, 0),
                  new Vec3f(0, 0, -1));
  }

  /** Takes normal vector and a point which the plane goes through
      (which becomes the plane's "origin"). Normal does NOT have to be
      normalized, but may not be zero vector. U and V axes are
      initialized to arbitrary values. */
  public PlaneUV(Vec3f normal, Vec3f origin) {
    setOrigin(origin);
    setNormal(normal);
  }

  /** Takes normal vector, point which plane goes through, and the "u"
    axis in the plane. Computes the "v" axis by taking the cross
    product of the normal and the u axis. Axis must be perpendicular
    to normal. Normal and uAxis do NOT have to be normalized, but
    neither may be the zero vector. */
  public PlaneUV(Vec3f normal,
                 Vec3f origin,
                 Vec3f uAxis) {
    setOrigin(origin);
    setNormalAndU(normal, uAxis);
  }

  /** Takes normal vector, point which plane goes through, and both
    the u and v axes. u axis cross v axis = normal. Normal, uAxis, and
    vAxis do NOT have to be normalized, but none may be the zero
    vector. */
  public PlaneUV(Vec3f normal,
                 Vec3f origin,
                 Vec3f uAxis,
                 Vec3f vAxis) {
    setEverything(normal, origin, uAxis, vAxis);
  }

  /** Set the origin, through which this plane goes and with respect
      to which U and V coordinates are computed */
  public void setOrigin(Vec3f origin) {
    this.origin.set(origin);
  }

  public Vec3f getOrigin() {
    return new Vec3f(origin);
  }

  /** Normal, U and V axes must be orthogonal and satisfy U cross V =
      normal, do not need to be unit length but must not be the zero
      vector. */
  public void setNormalAndUV(Vec3f normal,
                             Vec3f uAxis,
                             Vec3f vAxis) {
    setEverything(normal, origin, uAxis, vAxis);
  }

  /** This version sets the normal vector and generates new U and V
      axes. */
  public void setNormal(Vec3f normal) {
    Vec3f uAxis = new Vec3f();
    MathUtil.makePerpendicular(normal, uAxis);
    Vec3f vAxis = normal.cross(uAxis);
    setEverything(normal, origin, uAxis, vAxis);
  }

  /** This version computes the V axis from (normal cross U). */
  public void setNormalAndU(Vec3f normal,
                            Vec3f uAxis) {
    Vec3f vAxis = normal.cross(uAxis);
    setEverything(normal, origin, uAxis, vAxis);
  }

  /** Normal, U and V axes are normalized internally, so, for example,
      <b>normal</b> is not necessarily equal to
      <code>plane.setNormal(normal); plane.getNormal();</code> */
  public Vec3f getNormal() {
    return normal;
  }

  public Vec3f getUAxis() {
    return uAxis;
  }

  public Vec3f getVAxis() {
    return vAxis;
  }

  /** Project a point onto the plane */
  public void projectPoint(Vec3f point,
                           Vec3f projPt,
                           Vec2f uvCoords) {
    // Using projPt as a temporary
    projPt.sub(point, origin);
    float dotp = normal.dot(projPt);
    // Component perpendicular to plane
    Vec3f tmpDir = new Vec3f();
    tmpDir.set(normal);
    tmpDir.scale(dotp);
    projPt.sub(projPt, tmpDir);
    // Take dot products with basis vectors
    uvCoords.set(projPt.dot(uAxis),
                 projPt.dot(vAxis));
    // Add on center to intersection point
    projPt.add(origin);
  }

  /** Intersect a ray with this plane, outputting not only the 3D
      intersection point but also the U, V coordinates of the
      intersection. Returns true if intersection occurred, false
      otherwise. This is a two-sided ray cast. */
  public boolean intersectRay(Vec3f rayStart,
                              Vec3f rayDirection,
                              IntersectionPoint intPt,
                              Vec2f uvCoords) {
    float denom = rayDirection.dot(normal);
    if (denom == 0.0f)
      return false;
    Vec3f tmpDir = new Vec3f();
    tmpDir.sub(origin, rayStart);
    float t = tmpDir.dot(normal) / denom;
    // Find intersection point
    Vec3f tmpPt = new Vec3f();
    tmpPt.set(rayDirection);
    tmpPt.scale(t);
    tmpPt.add(rayStart);
    intPt.setIntersectionPoint(tmpPt);
    intPt.setT(t);
    // Find UV coords
    tmpDir.sub(intPt.getIntersectionPoint(), origin);
    uvCoords.set(tmpDir.dot(uAxis), tmpDir.dot(vAxis));
    return true;
  }

  private void setEverything(Vec3f normal,
                             Vec3f origin,
                             Vec3f uAxis,
                             Vec3f vAxis) {
    this.normal.set(normal);
    this.origin.set(origin);
    this.uAxis.set(uAxis);
    this.vAxis.set(vAxis);
    this.normal.normalize();
    this.uAxis.normalize();
    this.vAxis.normalize();
  }
}
