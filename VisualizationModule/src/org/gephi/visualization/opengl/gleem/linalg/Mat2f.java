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

/** 2x2 matrix class useful for simple linear algebra. Representation
    is (as Mat4f) in row major order and assumes multiplication by
    column vectors on the right. */

public class Mat2f {
  private float[] data;

  /** Creates new matrix initialized to the zero matrix */
  public Mat2f() {
    data = new float[4];
  }

  /** Initialize to the identity matrix. */
  public void makeIdent() {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (i == j) {
          set(i, j, 1.0f);
        } else {
          set(i, j, 0.0f);
        }
      }
    }
  }
  
  /** Gets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public float get(int i, int j) {
    return data[2 * i + j];
  }

  /** Sets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public void set(int i, int j, float val) {
    data[2 * i + j] = val;
  }

  /** Set column i (i=[0..1]) to vector v. */
  public void setCol(int i, Vec2f v) {
    set(0, i, v.x());
    set(1, i, v.y());
  }

  /** Set row i (i=[0..1]) to vector v. */
  public void setRow(int i, Vec2f v) {
    set(i, 0, v.x());
    set(i, 1, v.y());
  }

  /** Transpose this matrix in place. */
  public void transpose() {
    float t = get(0, 1);
    set(0, 1, get(1, 0));
    set(1, 0, t);
  }

  /** Return the determinant. */
  public float determinant() {
    return (get(0, 0) * get(1, 1) - get(1, 0) * get(0, 1));
  }  

  /** Full matrix inversion in place. If matrix is singular, returns
      false and matrix contents are untouched. If you know the matrix
      is orthonormal, you can call transpose() instead. */
  public boolean invert() {
    float det = determinant();
    if (det == 0.0f)
      return false;

    // Create transpose of cofactor matrix in place
    float t = get(0, 0);
    set(0, 0, get(1, 1));
    set(1, 1, t);
    set(0, 1, -get(0, 1));
    set(1, 0, -get(1, 0));
  
    // Now divide by determinant
    for (int i = 0; i < 4; i++) {
      data[i] /= det;
    }
    return true;
  }

  /** Multiply a 2D vector by this matrix. NOTE: src and dest must be
      different vectors. */
  public void xformVec(Vec2f src, Vec2f dest) {
    dest.set(get(0, 0) * src.x() +
             get(0, 1) * src.y(),

             get(1, 0) * src.x() +
             get(1, 1) * src.y());
  }

  /** Returns this * b; creates new matrix */
  public Mat2f mul(Mat2f b) {
    Mat2f tmp = new Mat2f();
    tmp.mul(this, b);
    return tmp;
  }

  /** this = a * b */
  public void mul(Mat2f a, Mat2f b) {
    for (int rc = 0; rc < 2; rc++)
      for (int cc = 0; cc < 2; cc++) {
        float tmp = 0.0f;
        for (int i = 0; i < 2; i++)
          tmp += a.get(rc, i) * b.get(i, cc);
        set(rc, cc, tmp);
      }
  }

  public Matf toMatf() {
    Matf out = new Matf(2, 2);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        out.set(i, j, get(i, j));
      }
    }
    return out;
  }

  public String toString() {
    String endl = System.getProperty("line.separator");
    return "(" +
      get(0, 0) + ", " + get(0, 1) + endl +
      get(1, 0) + ", " + get(1, 1) + ")";
  }
}
