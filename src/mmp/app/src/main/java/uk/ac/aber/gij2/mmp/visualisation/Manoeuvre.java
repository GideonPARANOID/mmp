/**
 * @created 2015-01-31
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.mmp.visualisation;

import android.opengl.Matrix;

import java.util.ArrayList;


public class Manoeuvre extends Shape implements Drawable {

   private Component[] components;
   private float[][] matrices;
   private boolean matricesCalculated = false, verticesCalculated = false;
   private float[] vertices;
   private String olan;


   public Manoeuvre(Component[] components, String olan) {
      super();

      this.components = components;
      this.olan = olan;
   }


   public void draw(float[] initialMatrix) {

      // setting up relies on an initial draw matrix, so have to do it in the draw loop
      if (!matricesCalculated) {
         calculateMatricesList(initialMatrix);
         matricesCalculated = true;

      // vertices are volatile for some reason & need to done after matrices calculated
      } else if (!verticesCalculated) {
         calculateVertices();
         super.setup();
         verticesCalculated = true;

      } else {
         super.draw(initialMatrix);
      }

/*      for (int i = 0; i < components.length; i++) {
         components[i].draw(matrices[i]);
      }*/
   }


   /**
    * builds a set of matrices summarising those of all its components
    */
   private void calculateVertices() {

      ArrayList<Float> verticesComplete = new ArrayList<>();

      // looping through all the vertices
      for (int i = 0; i < components.length; i++) {

         float[] componentVertices = components[i].getVertices();

         // looping through all the vertices in that component
         for (int j = 0; j < componentVertices.length; j += 3) {

            float[] newVertices = new float[4];

            // multiplying the component's points by its cumulative matrix
            Matrix.multiplyMV(newVertices, 0, matrices[i], 0, new float[] {
               componentVertices[j],      // x
               componentVertices[j + 1],  // y
               componentVertices[j + 2],  // z
               1f                         // w
            }, 0);

            // removing the w, which is one, so not really necessary, but this is maths so do it right
            verticesComplete.add(newVertices[0] / newVertices[3]);
            verticesComplete.add(newVertices[1] / newVertices[3]);
            verticesComplete.add(newVertices[2] / newVertices[3]);
         }
      }

      vertices = new float[verticesComplete.size()];

      // converting list into array of primitives
      for (int i = 0; i < vertices.length; i++) {
         vertices[i] = verticesComplete.get(i);
      }

      super.setVertices(vertices);
   }

   /**
    * builds an array of matrices corresponding to the components, each relative the last component
    *    as each component is a line, its matrix defines the transform from the beginning of that
    *    line to the end, components need to be drawn starting from the end of the last line
    * @param initialMatrix - the starting matrix
    */
   private void calculateMatricesList(float[] initialMatrix) {

      matrices = new float[components.length + 1][16];

      matrices[0] = initialMatrix;

      // each matrix is a multiplication of the last constructed one & the last drawn one
      for (int i = 1; i < components.length + 1; i++) {
         Matrix.multiplyMM(matrices[i], 0, matrices[i - 1], 0, components[i - 1].getMatrix(), 0);
      }
   }


   public String getOlan() {
      return olan;
   }


   /**
    * @return - the full matrix operation from the  beginning of the manoeuvre to the end
    */
   public float[] getMatrix() {

      // starting from scratch
      float[] blank = new float[16];
      Matrix.setIdentityM(blank, 0);
      calculateMatricesList(blank);

      return matrices[matrices.length - 1];
   }
}
