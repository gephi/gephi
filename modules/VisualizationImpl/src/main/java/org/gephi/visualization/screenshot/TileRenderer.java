package org.gephi.visualization.screenshot;

import java.awt.Dimension;
import java.nio.Buffer;

import javax.media.opengl.*;
import javax.media.opengl.glu.gl2.*;

/**
 * Note: Code copied from JOGL 2.0.2 sources http://jogamp.org/deployment/maven/org/jogamp/jogl/jogl/2.0.2/
 * This class is no longer included since JOGL 2.1.0 and up (no clear reason why).
 * 
 * We keep this class here until a better solution is found, or TileRenderer is included again in a new JOGL release.
 */

/**
 * A fairly direct port of Brian Paul's tile rendering library, found
 * at <a href = "http://www.mesa3d.org/brianp/TR.html">
 * http://www.mesa3d.org/brianp/TR.html </a> . I've java-fied it, but
 * the functionality is the same.
 * 
 * Original code Copyright (C) 1997-2005 Brian Paul. Licensed under
 * BSD-compatible terms with permission of the author. See LICENSE.txt
 * for license information.
 * 
 * @author ryanm
 */
public class TileRenderer
{
  private static final int DEFAULT_TILE_WIDTH = 256;

  private static final int DEFAULT_TILE_HEIGHT = 256;

  private static final int DEFAULT_TILE_BORDER = 0;

  //
  // Enumeration flags for accessing variables
  //
  // @author ryanm
  //

  /**
   * The width of a tile
   */
  public static final int TR_TILE_WIDTH = 0;
  /**
   * The height of a tile
   */
  public static final int TR_TILE_HEIGHT = 1;
  /**
   * The width of the border around the tiles
   */
  public static final int TR_TILE_BORDER = 2;
  /**
   * The width of the final image
   */
  public static final int TR_IMAGE_WIDTH = 3;
  /**
   * The height of the final image
   */
  public static final int TR_IMAGE_HEIGHT = 4;
  /**
   * The number of rows of tiles
   */
  public static final int TR_ROWS = 5;
  /**
   * The number of columns of tiles
   */
  public static final int TR_COLUMNS = 6;
  /**
   * The current row number
   */
  public static final int TR_CURRENT_ROW = 7;
  /**
   * The current column number
   */
  public static final int TR_CURRENT_COLUMN = 8;
  /**
   * The width of the current tile
   */
  public static final int TR_CURRENT_TILE_WIDTH = 9;
  /**
   * The height of the current tile
   */
  public static final int TR_CURRENT_TILE_HEIGHT = 10;
  /**
   * The order that the rows are traversed
   */
  public static final int TR_ROW_ORDER = 11;


  /**
   * Indicates we are traversing rows from the top to the bottom
   */
  public static final int TR_TOP_TO_BOTTOM = 1;

  /**
   * Indicates we are traversing rows from the bottom to the top
   */
  public static final int TR_BOTTOM_TO_TOP = 2;

  /* Final image parameters */
  private Dimension imageSize = new Dimension();

  private int imageFormat, imageType;

  private Buffer imageBuffer;

  /* Tile parameters */
  private Dimension tileSize = new Dimension();

  private Dimension tileSizeNB = new Dimension();

  private int tileBorder;

  private int tileFormat, tileType;

  private Buffer tileBuffer;

  /* Projection parameters */
  private boolean perspective;

  private double left;

  private double right;

  private double bottom;

  private double top;

  private double near;

  private double far;

  /* Misc */
  private int rowOrder;

  private int rows, columns;

  private int currentTile;

  private int currentTileWidth, currentTileHeight;

  private int currentRow, currentColumn;

  private int[] viewportSave = new int[ 4 ];

  /**
   * Creates a new TileRenderer object
   */
  public TileRenderer()
  {
    tileSize.width = DEFAULT_TILE_WIDTH;
    tileSize.height = DEFAULT_TILE_HEIGHT;
    tileBorder = DEFAULT_TILE_BORDER;
    rowOrder = TR_BOTTOM_TO_TOP;
    currentTile = -1;
  }

  /**
   * Sets up the number of rows and columns needed
   */
  private void setup()
  {
    columns = ( imageSize.width + tileSizeNB.width - 1 ) / tileSizeNB.width;
    rows = ( imageSize.height + tileSizeNB.height - 1 ) / tileSizeNB.height;
    currentTile = 0;

    assert columns >= 0;
    assert rows >= 0;
  }

  /**
   * Sets the size of the tiles to use in rendering. The actual
   * effective size of the tile depends on the border size, ie (
   * width - 2*border ) * ( height - 2 * border )
   * 
   * @param width
   *           The width of the tiles. Must not be larger than the GL
   *           context
   * @param height
   *           The height of the tiles. Must not be larger than the
   *           GL context
   * @param border
   *           The width of the borders on each tile. This is needed
   *           to avoid artifacts when rendering lines or points with
   *           thickness > 1.
   */
  public void setTileSize( int width, int height, int border )
  {
    assert ( border >= 0 );
    assert ( width >= 1 );
    assert ( height >= 1 );
    assert ( width >= 2 * border );
    assert ( height >= 2 * border );

    tileBorder = border;
    tileSize.width = width;
    tileSize.height = height;
    tileSizeNB.width = width - 2 * border;
    tileSizeNB.height = height - 2 * border;
    setup();
  }

  /**
   * Specify a buffer the tiles to be copied to. This is not
   * necessary for the creation of the final image, but useful if you
   * want to inspect each tile in turn.
   * 
   * @param format
   *           Interpreted as in glReadPixels
   * @param type
   *           Interpreted as in glReadPixels
   * @param image
   *           The buffer itself. Must be large enough to contain a
   *           tile, minus any borders
   */
  public void setTileBuffer( int format, int type, Buffer image )
  {
    tileFormat = format;
    tileType = type;
    tileBuffer = image;
  }

  /**
   * Sets the desired size of the final image
   * 
   * @param width
   *           The width of the final image
   * @param height
   *           The height of the final image
   */
  public void setImageSize( int width, int height )
  {
    imageSize.width = width;
    imageSize.height = height;
    setup();
  }

  /**
   * Sets the buffer in which to store the final image
   * 
   * @param format
   *           Interpreted as in glReadPixels
   * @param type
   *           Interpreted as in glReadPixels
   * @param image
   *           the buffer itself, must be large enough to hold the
   *           final image
   */
  public void setImageBuffer( int format, int type, Buffer image )
  {
    imageFormat = format;
    imageType = type;
    imageBuffer = image;
  }

  /**
   * Gets the parameters of this TileRenderer object
   * 
   * @param param
   *           The parameter that is to be retrieved
   * @return the value of the parameter
   */
  public int getParam( int param )
  {
    switch (param) {
      case TR_TILE_WIDTH:
        return tileSize.width;
      case TR_TILE_HEIGHT:
        return tileSize.height;
      case TR_TILE_BORDER:
        return tileBorder;
      case TR_IMAGE_WIDTH:
        return imageSize.width;
      case TR_IMAGE_HEIGHT:
        return imageSize.height;
      case TR_ROWS:
        return rows;
      case TR_COLUMNS:
        return columns;
      case TR_CURRENT_ROW:
        if( currentTile < 0 )
          return -1;
        else
          return currentRow;
      case TR_CURRENT_COLUMN:
        if( currentTile < 0 )
          return -1;
        else
          return currentColumn;
      case TR_CURRENT_TILE_WIDTH:
        return currentTileWidth;
      case TR_CURRENT_TILE_HEIGHT:
        return currentTileHeight;
      case TR_ROW_ORDER:
        return rowOrder;
      default:
        throw new IllegalArgumentException("Invalid enumerant as argument");
    }
  }

  /**
   * Sets the order of row traversal
   * 
   * @param order
   *           The row traversal order, must be
   *           eitherTR_TOP_TO_BOTTOM or TR_BOTTOM_TO_TOP
   */
  public void setRowOrder( int order )
  {
    if (order == TR_TOP_TO_BOTTOM || order == TR_BOTTOM_TO_TOP) {
      rowOrder = order;
    } else {
      throw new IllegalArgumentException("Must pass TR_TOP_TO_BOTTOM or TR_BOTTOM_TO_TOP");
    }
  }

  /**
   * Sets the context to use an orthographic projection. Must be
   * called before rendering the first tile
   * 
   * @param left
   *           As in glOrtho
   * @param right
   *           As in glOrtho
   * @param bottom
   *           As in glOrtho
   * @param top
   *           As in glOrtho
   * @param zNear
   *           As in glOrtho
   * @param zFar
   *           As in glOrtho
   */
  public void trOrtho( double left, double right, double bottom, double top, double zNear,
                       double zFar )
  {
    this.perspective = false;
    this.left = left;
    this.right = right;
    this.bottom = bottom;
    this.top = top;
    this.near = zNear;
    this.far = zFar;
  }

  /**
   * Sets the perspective projection frustrum. Must be called before
   * rendering the first tile
   * 
   * @param left
   *           As in glFrustrum
   * @param right
   *           As in glFrustrum
   * @param bottom
   *           As in glFrustrum
   * @param top
   *           As in glFrustrum
   * @param zNear
   *           As in glFrustrum
   * @param zFar
   *           As in glFrustrum
   */
  public void trFrustum( double left, double right, double bottom, double top, double zNear,
                         double zFar )
  {
    this.perspective = true;
    this.left = left;
    this.right = right;
    this.bottom = bottom;
    this.top = top;
    this.near = zNear;
    this.far = zFar;
  }

  /**
   * Convenient way to specify a perspective projection
   * 
   * @param fovy
   *           As in gluPerspective
   * @param aspect
   *           As in gluPerspective
   * @param zNear
   *           As in gluPerspective
   * @param zFar
   *           As in gluPerspective
   */
  public void trPerspective( double fovy, double aspect, double zNear, double zFar )
  {
    double xmin, xmax, ymin, ymax;
    ymax = zNear * Math.tan( fovy * 3.14159265 / 360.0 );
    ymin = -ymax;
    xmin = ymin * aspect;
    xmax = ymax * aspect;
    trFrustum( xmin, xmax, ymin, ymax, zNear, zFar );
  }

  /**
   * Begins rendering a tile. The projection matrix stack should be
   * left alone after calling this
   * 
   * @param gl
   *           The gl context
   */
  public void beginTile( GL2 gl )
  {
    if (currentTile <= 0) {
      setup();
      /*
       * Save user's viewport, will be restored after last tile
       * rendered
       */
      gl.glGetIntegerv( GL2.GL_VIEWPORT, viewportSave, 0 );
    }

    /* which tile (by row and column) we're about to render */
    if (rowOrder == TR_BOTTOM_TO_TOP) {
      currentRow = currentTile / columns;
      currentColumn = currentTile % columns;
    } else {
      currentRow = rows - ( currentTile / columns ) - 1;
      currentColumn = currentTile % columns;
    }
    assert ( currentRow < rows );
    assert ( currentColumn < columns );

    int border = tileBorder;

    int th, tw;

    /* Compute actual size of this tile with border */
    if (currentRow < rows - 1) {
      th = tileSize.height;
    } else {
      th = imageSize.height - ( rows - 1 ) * ( tileSizeNB.height  ) + 2 * border;
    }

    if (currentColumn < columns - 1) {
      tw = tileSize.width;
    } else {
      tw = imageSize.width - ( columns - 1 ) * ( tileSizeNB.width  ) + 2 * border;
    }

    /* Save tile size, with border */
    currentTileWidth = tw;
    currentTileHeight = th;

    gl.glViewport( 0, 0, tw, th );

    /* save current matrix mode */
    int[] matrixMode = new int[ 1 ];
    gl.glGetIntegerv( GL2.GL_MATRIX_MODE, matrixMode, 0 );
    gl.glMatrixMode( GL2.GL_PROJECTION );
    gl.glLoadIdentity();

    /* compute projection parameters */
    double l =
      left + ( right - left ) * ( currentColumn * tileSizeNB.width - border )
      / imageSize.width;
    double r = l + ( right - left ) * tw / imageSize.width;
    double b =
      bottom + ( top - bottom ) * ( currentRow * tileSizeNB.height - border )
      / imageSize.height;
    double t = b + ( top - bottom ) * th / imageSize.height;

    if( perspective ) {
      gl.glFrustum( l, r, b, t, near, far );
    } else {
      gl.glOrtho( l, r, b, t, near, far );
    }

    /* restore user's matrix mode */
    gl.glMatrixMode( matrixMode[ 0 ] );
  }

  /**
   * Must be called after rendering the scene
   * 
   * @param gl
   *           the gl context
   * @return true if there are more tiles to be rendered, false if
   *         the final image is complete
   */
  public boolean endTile( GL2 gl )
  {
    int[] prevRowLength = new int[ 1 ], prevSkipRows = new int[ 1 ], prevSkipPixels = new int[ 1 ], prevAlignment =
      new int[ 1 ];

    assert ( currentTile >= 0 );

    // be sure OpenGL rendering is finished
    gl.glFlush();

    // save current glPixelStore values
    gl.glGetIntegerv( GL2.GL_PACK_ROW_LENGTH, prevRowLength, 0 );
    gl.glGetIntegerv( GL2.GL_PACK_SKIP_ROWS, prevSkipRows, 0 );
    gl.glGetIntegerv( GL2.GL_PACK_SKIP_PIXELS, prevSkipPixels, 0 );
    gl.glGetIntegerv( GL2.GL_PACK_ALIGNMENT, prevAlignment, 0 );

    if( tileBuffer != null ) {
      int srcX = tileBorder;
      int srcY = tileBorder;
      int srcWidth = tileSizeNB.width;
      int srcHeight = tileSizeNB.height;
      gl.glReadPixels( srcX, srcY, srcWidth, srcHeight, tileFormat, tileType, tileBuffer );
    }

    if( imageBuffer != null ) {
      int srcX = tileBorder;
      int srcY = tileBorder;
      int srcWidth = currentTileWidth - 2 * tileBorder;
      int srcHeight = currentTileHeight - 2 * tileBorder;
      int destX = tileSizeNB.width * currentColumn;
      int destY = tileSizeNB.height * currentRow;

      /* setup pixel store for glReadPixels */
      gl.glPixelStorei( GL2.GL_PACK_ROW_LENGTH, imageSize.width );
      gl.glPixelStorei( GL2.GL_PACK_SKIP_ROWS, destY );
      gl.glPixelStorei( GL2.GL_PACK_SKIP_PIXELS, destX );
      gl.glPixelStorei( GL2.GL_PACK_ALIGNMENT, 1 );

      /* read the tile into the final image */
      gl.glReadPixels( srcX, srcY, srcWidth, srcHeight, imageFormat, imageType, imageBuffer );
    }

    /* restore previous glPixelStore values */
    gl.glPixelStorei( GL2.GL_PACK_ROW_LENGTH, prevRowLength[ 0 ] );
    gl.glPixelStorei( GL2.GL_PACK_SKIP_ROWS, prevSkipRows[ 0 ] );
    gl.glPixelStorei( GL2.GL_PACK_SKIP_PIXELS, prevSkipPixels[ 0 ] );
    gl.glPixelStorei( GL2.GL_PACK_ALIGNMENT, prevAlignment[ 0 ] );

    /* increment tile counter, return 1 if more tiles left to render */
    currentTile++;
    if( currentTile >= rows * columns ) {
      /* restore user's viewport */
      gl.glViewport( viewportSave[ 0 ], viewportSave[ 1 ], viewportSave[ 2 ], viewportSave[ 3 ] );
      currentTile = -1; /* all done */
      return false;
    } else {
      return true;
    }
  }

  /**
   * Tile rendering causes problems with using glRasterPos3f, so you
   * should use this replacement instead
   * 
   * @param x
   *           As in glRasterPos3f
   * @param y
   *           As in glRasterPos3f
   * @param z
   *           As in glRasterPos3f
   * @param gl
   *           The gl context
   * @param glu
   *           A GLUgl2 object
   */
  public void trRasterPos3f( float x, float y, float z, GL2 gl, GLUgl2 glu )
  {
    if (currentTile < 0) {
      /* not doing tile rendering right now. Let OpenGL do this. */
      gl.glRasterPos3f( x, y, z );
    } else {
      double[] modelview = new double[ 16 ], proj = new double[ 16 ];
      int[] viewport = new int[ 4 ];
      double[] win = new double[3];

      /* Get modelview, projection and viewport */
      gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, modelview, 0 );
      gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, proj, 0 );
      viewport[ 0 ] = 0;
      viewport[ 1 ] = 0;
      viewport[ 2 ] = currentTileWidth;
      viewport[ 3 ] = currentTileHeight;

      /* Project object coord to window coordinate */
      if( glu.gluProject( x, y, z, modelview, 0, proj, 0, viewport, 0, win, 0 ) ) {

        /* set raster pos to window coord (0,0) */
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho( 0.0, currentTileWidth, 0.0, currentTileHeight, 0.0, 1.0 );
        gl.glRasterPos3d( 0.0, 0.0, -win[ 2 ] );

        /*
         * Now use empty bitmap to adjust raster position to
         * (winX,winY)
         */
        {
          byte[] bitmap = { 0 };
          gl.glBitmap( 1, 1, 0.0f, 0.0f, ( float ) win[ 0 ], ( float ) win[ 1 ], bitmap , 0 );
        }

        /* restore original matrices */
        gl.glPopMatrix(); /* proj */
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glPopMatrix();
      }
    }
  }
}
