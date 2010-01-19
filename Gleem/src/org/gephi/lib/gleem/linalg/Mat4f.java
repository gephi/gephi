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

package org.gephi.lib.gleem.linalg;

/** A (very incomplete) 4x4 matrix class. Representation assumes
    multiplication by column vectors on the right. */

public class Mat4f {
  private float[] data;

  /** Creates new matrix initialized to the zero matrix */
  public Mat4f() {
    data = new float[16];
  }

  /** Creates new matrix initialized to argument's contents */
  public Mat4f(Mat4f arg) {
    this();
    set(arg);
  }

  /** Sets this matrix to the identity matrix */
  public void makeIdent() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        if (i == j) {
          set(i, j, 1.0f);
        } else {
          set(i, j, 0.0f);
        }
      }
    }
  }

  /** Sets this matrix to be equivalent to the given one */
  public void set(Mat4f arg) {
    float[] mine = data;
    float[] yours = arg.data;
    for (int i = 0; i < mine.length; i++) {
      mine[i] = yours[i];
    }
  }

  /** Gets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public float get(int i, int j) {
    return data[4 * i + j];
  }

  /** Sets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public void set(int i, int j, float val) {
    data[4 * i + j] = val;
  }

  /** Sets the translation component of this matrix (i.e., the three
      top elements of the third column) without touching any of the
      other parts of the matrix */
  public void setTranslation(Vec3f trans) {
    set(0, 3, trans.x());
    set(1, 3, trans.y());
    set(2, 3, trans.z());
  }

  /** Sets the rotation component of this matrix (i.e., the upper left
      3x3) without touching any of the other parts of the matrix */
  public void setRotation(Rotf rot) {
    rot.toMatrix(this);
  }

  /** Sets the upper-left 3x3 of this matrix assuming that the given
      x, y, and z vectors form an orthonormal basis */
  public void setRotation(Vec3f x, Vec3f y, Vec3f z) {
    set(0, 0, x.x());
    set(1, 0, x.y());
    set(2, 0, x.z());

    set(0, 1, y.x());
    set(1, 1, y.y());
    set(2, 1, y.z());

    set(0, 2, z.x());
    set(1, 2, z.y());
    set(2, 2, z.z());
  }

  /** Gets the upper left 3x3 of this matrix as a rotation. Currently
      does not work if there are scales. Ignores translation
      component. */
  public void getRotation(Rotf rot) {
    rot.fromMatrix(this);
  }

  /** Sets the elements (0, 0), (1, 1), and (2, 2) with the
      appropriate elements of the given three-dimensional scale
      vector. Does not perform a full multiplication of the upper-left
      3x3; use this with an identity matrix in conjunction with
      <code>mul</code> for that. */
  public void setScale(Vec3f scale) {
    set(0, 0, scale.x());
    set(1, 1, scale.y());
    set(2, 2, scale.z());
  }

  /** Inverts this matrix assuming that it represents a rigid
      transform (i.e., some combination of rotations and
      translations). Assumes column vectors. Algorithm: transposes
      upper left 3x3; negates translation in rightmost column and
      transforms by inverted rotation. */
  public void invertRigid() {
    float t;
    // Transpose upper left 3x3
    t = get(0, 1);
    set(0, 1, get(1, 0));
    set(1, 0, t);
    t = get(0, 2);
    set(0, 2, get(2, 0));
    set(2, 0, t);
    t = get(1, 2);
    set(1, 2, get(2, 1));
    set(2, 1, t);
    // Transform negative translation by this
    Vec3f negTrans = new Vec3f(-get(0, 3), -get(1, 3), -get(2, 3));
    Vec3f trans = new Vec3f();
    xformDir(negTrans, trans);
    set(0, 3, trans.x());
    set(1, 3, trans.y());
    set(2, 3, trans.z());
  }

  /** Returns this * b; creates new matrix */
  public Mat4f mul(Mat4f b) {
    Mat4f tmp = new Mat4f();
    tmp.mul(this, b);
    return tmp;
  }

  /** this = a * b */
  public void mul(Mat4f a, Mat4f b) {
    for (int rc = 0; rc < 4; rc++)
      for (int cc = 0; cc < 4; cc++) {
        float tmp = 0.0f;
        for (int i = 0; i < 4; i++)
          tmp += a.get(rc, i) * b.get(i, cc);
        set(rc, cc, tmp);
      }
  }
  
  /** Transpose this matrix in place. */
  public void transpose() {
    float t;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < i; j++) {
        t = get(i, j);
        set(i, j, get(j, i));
        set(j, i, t);
      }
    }
  }

  /** Multiply a 4D vector by this matrix. NOTE: src and dest must be
      different vectors. */
  public void xformVec(Vec4f src, Vec4f dest) {
    for (int rc = 0; rc < 4; rc++) {
      float tmp = 0.0f;
      for (int cc = 0; cc < 4; cc++) {
        tmp += get(rc, cc) * src.get(cc);
      }
      dest.set(rc, tmp);
    }
  }

  /** Transforms a 3D vector as though it had a homogeneous coordinate
      and assuming that this matrix represents only rigid
      transformations; i.e., is not a full transformation. NOTE: src
      and dest must be different vectors. */
  public void xformPt(Vec3f src, Vec3f dest) {
    for (int rc = 0; rc < 3; rc++) {
      float tmp = 0.0f;
      for (int cc = 0; cc < 3; cc++) {
        tmp += get(rc, cc) * src.get(cc);
      }
      tmp += get(rc, 3);
      dest.set(rc, tmp);
    }
  }
  
  /** Transforms src using only the upper left 3x3. NOTE: src and dest
      must be different vectors. */
  public void xformDir(Vec3f src, Vec3f dest) {
    for (int rc = 0; rc < 3; rc++) {
      float tmp = 0.0f;
      for (int cc = 0; cc < 3; cc++) {
        tmp += get(rc, cc) * src.get(cc);
      }
      dest.set(rc, tmp);
    }
  }
  
  /** Copies data in column-major (OpenGL format) order into passed
      float array, which must have length 16 or greater. */
  public void getColumnMajorData(float[] out) {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out[4 * j + i] = get(i, j);
      }
    }
  }

  public Matf toMatf() {
    Matf out = new Matf(4, 4);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out.set(i, j, get(i, j));
      }
    }
    return out;
  }

  public String toString() {
    String endl = System.getProperty("line.separator");
    return "(" +
      get(0, 0) + ", " + get(0, 1) + ", " + get(0, 2) + ", " + get(0, 3) + endl +
      get(1, 0) + ", " + get(1, 1) + ", " + get(1, 2) + ", " + get(1, 3) + endl +
      get(2, 0) + ", " + get(2, 1) + ", " + get(2, 2) + ", " + get(2, 3) + endl +
      get(3, 0) + ", " + get(3, 1) + ", " + get(3, 2) + ", " + get(3, 3) + ")";
  }
}
