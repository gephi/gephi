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

/** Arbitrary-size single-precision matrix class. Currently very
    simple and only supports a few needed operations. */

public class Matf {
  private float[] data;
  private int nCol; // number of columns
  private int nRow; // number of columns

  public Matf(int nRow, int nCol) {
    data = new float[nRow * nCol];
    this.nCol = nCol;
    this.nRow = nRow;
  }

  public Matf(Matf arg) {
    nRow = arg.nRow;
    nCol = arg.nCol;
    data = new float[nRow * nCol];
    System.arraycopy(arg.data, 0, data, 0, data.length);
  }

  public int nRow() {
    return nRow;
  }

  public int nCol() {
    return nCol;
  }

  /** Gets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public float get(int i, int j) {
    return data[nCol * i + j];
  }
  
  /** Sets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public void set(int i, int j, float val) {
    data[nCol * i + j] = val;
  }

  /** Returns transpose of this matrix; creates new matrix */
  public Matf transpose() {
    Matf tmp = new Matf(nCol, nRow);
    for (int i = 0; i < nRow; i++) {
      for (int j = 0; j < nCol; j++) {
        tmp.set(j, i, get(i, j));
      }
    }
    return tmp;
  }
  
  /** Returns this * b; creates new matrix */
  public Matf mul(Matf b) throws DimensionMismatchException {
    if (nCol() != b.nRow())
      throw new DimensionMismatchException();
    Matf tmp = new Matf(nRow(), b.nCol());
    for (int i = 0; i < nRow(); i++) {
      for (int j = 0; j < b.nCol(); j++) {
        float val = 0;
        for (int t = 0; t < nCol(); t++) {
          val += get(i, t) * b.get(t, j);
        }
        tmp.set(i, j, val);
      }
    }
    return tmp;
  }

  /** Returns this * v, assuming v is a column vector. */
  public Vecf mul(Vecf v) throws DimensionMismatchException {
    if (nCol() != v.length()) {
      throw new DimensionMismatchException();
    }
    Vecf out = new Vecf(nRow());
    for (int i = 0; i < nRow(); i++) {
      float tmp = 0;
      for (int j = 0; j < nCol(); j++) {
        tmp += get(i, j) * v.get(j);
      }
      out.set(i, tmp);
    }
    return out;
  }

  /** If this is a 2x2 matrix, returns it as a Mat2f. */
  public Mat2f toMat2f() throws DimensionMismatchException {
    if (nRow() != 2 || nCol() != 2) {
      throw new DimensionMismatchException();
    }
    Mat2f tmp = new Mat2f();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        tmp.set(i, j, get(i, j));
      }
    }
    return tmp;
  }

  /** If this is a 3x3 matrix, returns it as a Mat3f. */
  public Mat3f toMat3f() throws DimensionMismatchException {
    if (nRow() != 3 || nCol() != 3) {
      throw new DimensionMismatchException();
    }
    Mat3f tmp = new Mat3f();
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        tmp.set(i, j, get(i, j));
      }
    }
    return tmp;
  }

  /** If this is a 4x4 matrix, returns it as a Mat4f. */
  public Mat4f toMat4f() throws DimensionMismatchException {
    if (nRow() != 4 || nCol() != 4) {
      throw new DimensionMismatchException();
    }
    Mat4f tmp = new Mat4f();
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        tmp.set(i, j, get(i, j));
      }
    }
    return tmp;
  }
}
