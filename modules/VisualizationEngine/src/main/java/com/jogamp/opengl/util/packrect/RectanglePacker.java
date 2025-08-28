/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package com.jogamp.opengl.util.packrect;

import java.util.*;

/** Packs rectangles supplied by the user (typically representing
    image regions) into a larger backing store rectangle (typically
    representing a large texture). Supports automatic compaction of
    the space on the backing store, and automatic expansion of the
    backing store, when necessary. */

public class RectanglePacker {

  private static final float DEFAULT_EXPANSION_FACTOR = 0.5f;

  private final BackingStoreManager manager;
  private Object backingStore;
  private LevelSet levels;
  private final float EXPANSION_FACTOR;
  private static final float SHRINK_FACTOR = 0.3f;

  private final int initialWidth;
  private final int initialHeight;

  private int maxWidth  = -1;
  private int maxHeight = -1;

  static class RectHComparator implements Comparator<Rect> {
    @Override
    public int compare(final Rect r1, final Rect r2) {
      return r2.h() - r1.h();
    }

    @Override
    public boolean equals(final Object obj) {
      return this == obj;
    }
  }
  private static final Comparator<Rect> rectHComparator = new RectHComparator();

  public RectanglePacker(final BackingStoreManager manager,
                         final int initialWidth,
                         final int initialHeight) {
    this(manager, initialWidth, initialHeight, DEFAULT_EXPANSION_FACTOR);
  }

  public RectanglePacker(final BackingStoreManager manager,
                         final int initialWidth,
                         final int initialHeight,
                         final float expansionFactor) {
    this.manager = manager;
    levels = new LevelSet(initialWidth, initialHeight);
    this.initialWidth = initialWidth;
    this.initialHeight = initialHeight;
    EXPANSION_FACTOR = expansionFactor;
  }

  public Object getBackingStore() {
    if (backingStore == null) {
      backingStore = manager.allocateBackingStore(levels.w(), levels.h());
    }

    return backingStore;
  }

  /** Sets up a maximum width and height for the backing store. These
      are optional and if not specified the backing store will grow as
      necessary. Setting up a maximum width and height introduces the
      possibility that additions will fail; these are handled with the
      BackingStoreManager's allocationFailed notification. */
  public void setMaxSize(final int maxWidth, final int maxHeight) {
    this.maxWidth  = maxWidth;
    this.maxHeight = maxHeight;
  }

  /** Decides upon an (x, y) position for the given rectangle (leaving
      its width and height unchanged) and places it on the backing
      store. May provoke re-layout of other Rects already added. If
      the BackingStoreManager does not support compaction, and {@link
      BackingStoreManager#preExpand BackingStoreManager.preExpand}
      does not clear enough space for the incoming rectangle, then
      this method will throw a RuntimeException. */
  public void add(final Rect rect) throws RuntimeException {
    // Allocate backing store if we don't have any yet
    if (backingStore == null)
      backingStore = manager.allocateBackingStore(levels.w(), levels.h());

    int attemptNumber = 0;
    boolean tryAgain = false;

    do {
      // Try to allocate
      if (levels.add(rect))
        return;

      if (manager.canCompact()) {
        // Try to allocate with horizontal compaction
        if (levels.compactAndAdd(rect, backingStore, manager))
          return;
        // Let the manager have a chance at potentially evicting some entries
        tryAgain = manager.preExpand(rect, attemptNumber++);
      } else {
        tryAgain = manager.additionFailed(rect, attemptNumber++);
      }
    } while (tryAgain);

    if (!manager.canCompact()) {
      throw new RuntimeException("BackingStoreManager does not support compaction or expansion, and didn't clear space for new rectangle");
    }

    compactImpl(rect);

    // Retry the addition of the incoming rectangle
    add(rect);
    // Done
  }

  /** Removes the given rectangle from this RectanglePacker. */
  public void remove(final Rect rect) {
    levels.remove(rect);
  }

  /** Visits all Rects contained in this RectanglePacker. */
  public void visit(final RectVisitor visitor) {
    levels.visit(visitor);
  }

  /** Returns the vertical fragmentation ratio of this
      RectanglePacker. This is defined as the ratio of the sum of the
      heights of all completely empty Levels divided by the overall
      used height of the LevelSet. A high vertical fragmentation ratio
      indicates that it may be profitable to perform a compaction. */
  public float verticalFragmentationRatio() {
    return levels.verticalFragmentationRatio();
  }

  /** Forces a compaction cycle, which typically results in allocating
      a new backing store and copying all entries to it. */
  public void compact() {
    compactImpl(null);
  }

  // The "cause" rect may be null
  private void compactImpl(final Rect cause) {
    // Have to either expand, compact or both. Need to figure out what
    // direction to go. Prefer to expand vertically. Expand
    // horizontally only if rectangle being added is too wide. FIXME:
    // may want to consider rebalancing the width and height to be
    // more equal if it turns out we keep expanding in the vertical
    // direction.
    boolean done = false;
    int newWidth = levels.w();
    int newHeight = levels.h();
    LevelSet nextLevelSet = null;
    int attemptNumber = 0;
    boolean needAdditionFailureNotification = false;

    while (!done) {
      if (cause != null) {
        if (cause.w() > newWidth) {
          newWidth = cause.w();
        } else {
          newHeight = (int) (newHeight * (1.0f + EXPANSION_FACTOR));
        }
      }

      // Clamp to maximum values
      needAdditionFailureNotification = false;
      if (maxWidth > 0 && newWidth > maxWidth) {
        newWidth = maxWidth;
        needAdditionFailureNotification = true;
      }
      if (maxHeight > 0 && newHeight > maxHeight) {
        newHeight = maxHeight;
        needAdditionFailureNotification = true;
      }

      nextLevelSet = new LevelSet(newWidth, newHeight);

      // Make copies of all existing rectangles
      final List<Rect> newRects = new ArrayList<Rect>();
      for (final Iterator<Level> i1 = levels.iterator(); i1.hasNext(); ) {
        final Level level = i1.next();
        for (final Iterator<Rect> i2 = level.iterator(); i2.hasNext(); ) {
          final Rect cur = i2.next();
          final Rect newRect = new Rect(0, 0, cur.w(), cur.h(), null);
          cur.setNextLocation(newRect);
          // Hook up the reverse mapping too for easier replacement
          newRect.setNextLocation(cur);
          newRects.add(newRect);
        }
      }
      // Sort them by decreasing height (note: this isn't really
      // guaranteed to improve the chances of a successful layout)
      Collections.sort(newRects, rectHComparator);
      // Try putting all of these rectangles into the new level set
      done = true;
      for (final Iterator<Rect> iter = newRects.iterator(); iter.hasNext(); ) {
        if (!nextLevelSet.add(iter.next())) {
          done = false;
          break;
        }
      }

      if (done && cause != null) {
        // Try to add the new rectangle as well
        if (nextLevelSet.add(cause)) {
          // We're OK
        } else {
          done = false;
        }
      }

      // Don't send addition failure notifications if we're only doing
      // a compaction
      if (!done && needAdditionFailureNotification && cause != null) {
        manager.additionFailed(cause, attemptNumber);
      }
      ++attemptNumber;
    }

    // See whether the implicit compaction that just occurred has
    // yielded excess empty space.
    if (nextLevelSet.getUsedHeight() > 0 &&
        nextLevelSet.getUsedHeight() < nextLevelSet.h() * SHRINK_FACTOR) {
      int shrunkHeight = Math.max(initialHeight,
                                  (int) (nextLevelSet.getUsedHeight() * (1.0f + EXPANSION_FACTOR)));
      if (maxHeight > 0 && shrunkHeight > maxHeight) {
        shrunkHeight = maxHeight;
      }
      nextLevelSet.setHeight(shrunkHeight);
    }

    // If we temporarily added the new rectangle to the new LevelSet,
    // take it out since we don't "really" add it here but in add(), above
    if (cause != null) {
      nextLevelSet.remove(cause);
    }

    // OK, now we have a new layout and a mapping from the old to the
    // new locations of rectangles on the backing store. Allocate a
    // new backing store, move the contents over and deallocate the
    // old one.
    final Object newBackingStore = manager.allocateBackingStore(nextLevelSet.w(),
                                                          nextLevelSet.h());
    manager.beginMovement(backingStore, newBackingStore);
    for (final Iterator<Level> i1 = levels.iterator(); i1.hasNext(); ) {
      final Level level = i1.next();
      for (final Iterator<Rect> i2 = level.iterator(); i2.hasNext(); ) {
        final Rect cur = i2.next();
        manager.move(backingStore, cur,
                     newBackingStore, cur.getNextLocation());
      }
    }
    // Replace references to temporary rectangles with original ones
    nextLevelSet.updateRectangleReferences();
    manager.endMovement(backingStore, newBackingStore);
    // Now delete the old backing store
    manager.deleteBackingStore(backingStore);
    // Update to new versions of backing store and LevelSet
    backingStore = newBackingStore;
    levels = nextLevelSet;
  }

  /** Clears all Rects contained in this RectanglePacker. */
  public void clear() {
    levels.clear();
  }

  /** Disposes the backing store allocated by the
      BackingStoreManager. This RectanglePacker may no longer be used
      after calling this method. */
  public void dispose() {
    if (backingStore != null)
      manager.deleteBackingStore(backingStore);
    backingStore = null;
    levels = null;
  }
}
