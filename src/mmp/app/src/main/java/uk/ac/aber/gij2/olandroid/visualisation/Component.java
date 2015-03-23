/**
 * @created 2015-01-28
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.olandroid.visualisation;

import android.opengl.Matrix;


public class Component extends Shape implements Drawable {

   // bounds for movement
   public static final int ZERO = 0, MIN = -1, MAX = 1;
   private static final float ANGLE = 1f / 24f, WIDTH = 0.5f;

   private int pitch, yaw, roll;
   private float length;
   private float[] matrix, vertices, colourFront, colourBack;


   /**
    * @param pitch - vertical amount
    * @param yaw - horizontal amount
    * @param roll - roll amount
    * @param length - length of the component
    * @param colourFront - argb colour for the front of the component
    * @param colourBack - argb colour for the back of the component
    */
   public Component(int pitch, int yaw, int roll, float length, float[] colourFront,
      float[] colourBack) {

      super();

      this.pitch = pitch;
      this.yaw = yaw;
      this.roll = roll;
      this.length = length;
      this.colourFront = colourFront;
      this.colourBack = colourBack;

      super.setColourFront(colourFront);
      super.setColourBack(colourBack);

      buildVertices();
      buildMatrix();
   }


   /**
    * copy constructor
    * @param component - instance of component to copy
    */
   public Component(Component component) {
      this(component.pitch, component.yaw, component.roll, component.length, component.colourFront,
         component.colourBack);
   }


   /**
    * builds the vertices array for a buffer
    */
   protected void buildVertices() {
      float x = 0f, y = 0f, z = length, xOffset = WIDTH, yOffset = 0, zOffset = 0;

      if (pitch != Component.ZERO || yaw != Component.ZERO) {
         // first & second triangles of euler
         double theta = ANGLE * Math.PI * 2, phi = Math.atan((double) pitch / (double) yaw);

         // does not like dividing by minus one, mathematically works but not programmatically
         if (yaw == MIN) {
            phi = Math.PI + phi;
         }

         x = (float) (length * Math.sin(theta) * Math.cos(phi));
         y = (float) (length * Math.sin(theta) * Math.sin(phi));
         z = (float) (length * Math.cos(theta));
      }

      // building the difference for a roll
      if (roll != Component.ZERO) {
         xOffset = (float) (WIDTH * Math.cos(roll * ANGLE * Math.PI * 2f));
         yOffset = (float) (WIDTH * Math.sin(roll * ANGLE * Math.PI * 2f));
      }

      if (yaw != Component.ZERO) {
         zOffset = (float) -(WIDTH * Math.sin(yaw * ANGLE * Math.PI * 2f));
      }

      vertices = new float[] {
         WIDTH, 0f, 0f,
         -WIDTH, 0f, 0f,
         x - xOffset, y - yOffset, z - zOffset,
         x + xOffset, y + yOffset, z + zOffset
      };

      super.buildVerticesBuffer(vertices);
      super.buildDrawOrderBuffer(new short[] {
         0, 1, 2,
         0, 2, 3
      });
   }


   /**
    * builds the matrix which describes the transform from the beginning of the component to its end
    */
   protected void buildMatrix() {
      float[] mPitch = new float[16], mYaw = new float[16], mRoll = new float[16],
         mTemp = new float[16];

      // matrix library uses degrees instead of radians, heaven knows why
      float factor = 360f * ANGLE;

      matrix = new float[16];
      Matrix.setIdentityM(matrix, 0);
      Matrix.setIdentityM(mPitch, 0);
      Matrix.setIdentityM(mYaw, 0);
      Matrix.setIdentityM(mRoll, 0);

      if (pitch != Component.ZERO) {
         Matrix.setRotateM(mPitch, 0, factor, (float) -pitch, 0f, 0f);
      }

      if (yaw != Component.ZERO) {
         Matrix.setRotateM(mYaw, 0, factor, 0f, (float) yaw, 0f);
      }

      if (roll != Component.ZERO) {
         Matrix.setRotateM(mRoll, 0, factor, 0f, 0f, (float) roll);
      }

      // combining the euler
      Matrix.multiplyMM(mTemp, 0, mPitch, 0, mYaw, 0);
      Matrix.multiplyMM(matrix, 0, mTemp, 0, mRoll, 0);
      Matrix.translateM(matrix, 0, 0f, 0f, length);
   }


   public float[] getCompleteMatrix() {
      return matrix;
   }


   public void animate(float progress) {
      if (progress == 0f) {
         super.buildVerticesBuffer(null);

      } else if (progress == 1f) {
         super.buildVerticesBuffer(vertices);

      } else {
         // extending the y & z distance (not x, as that's width)
         super.buildVerticesBuffer(new float[] {
            WIDTH, 0f, 0f,
            -WIDTH, 0f, 0f,
            vertices[6], vertices[7] * progress, vertices[8] * progress,
            vertices[9], vertices[10] * progress, vertices[11] * progress
         });
      }
   }


   public float getLength() {
      return length;
   }


   public void setLength(float length) {
      this.length = length;
      buildVertices();
      buildMatrix();
   }
}
