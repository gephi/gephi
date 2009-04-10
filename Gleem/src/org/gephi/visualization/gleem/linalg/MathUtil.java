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

package org.gephi.visualization.gleem.linalg;

/** Utility math routines. */

public class MathUtil {
  /** Makes an arbitrary vector perpendicular to <B>src</B> and
      inserts it into <B>dest</B>. Returns false if the source vector
      was equal to (0, 0, 0). */
  public static boolean makePerpendicular(Vec3f src,
                                          Vec3f dest) {
    if ((src.x() == 0.0f) && (src.y() == 0.0f) && (src.z() == 0.0f)) {
      return false;
    }

    if (src.x() != 0.0f) {
      if (src.y() != 0.0f) {
	dest.set(-src.y(), src.x(), 0.0f);
      }	else {
	dest.set(-src.z(), 0.0f, src.x());
      }
    } else {
      dest.set(1.0f, 0.0f, 0.0f);
    }
    return true;
  }

  /** Returns 1 if the sign of the given argument is positive; -1 if
      negative; 0 if 0. */
  public static int sgn(float f) {
    if (f > 0) {
      return 1;
    } else if (f < 0) {
      return -1;
    }
    return 0;
  }

  /** Clamps argument between min and max values. */
  public static float clamp(float val, float min, float max) {
    if (val < min) return min;
    if (val > max) return max;
    return val;
  }

  /** Clamps argument between min and max values. */
  public static int clamp(int val, int min, int max) {
    if (val < min) return min;
    if (val > max) return max;
    return val;
  }
}
