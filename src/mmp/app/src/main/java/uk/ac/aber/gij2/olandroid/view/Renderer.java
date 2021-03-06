/**
 * @created 2015-01-26
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.olandroid.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.ac.aber.gij2.olandroid.controller.OLANdroid;
import uk.ac.aber.gij2.olandroid.R;
import uk.ac.aber.gij2.olandroid.Util;
import uk.ac.aber.gij2.olandroid.controller.Scene;


public class Renderer implements GLSurfaceView.Renderer {

   public static int program;
   public static int[] textures;

   public static float DRAW_BOUNDS = 100f;

   // view & scene building tools
   private float[] mMVPMatrix, mProjectionMatrix, mViewMatrix;

   // for back referencing to the application
   private Context context;

   // shifting around the view
   private float viewRotationY, viewRotationX, viewTranslationZ, viewTranslationX, viewZoom;
   private float[] viewMatrix;

   // content
   private Scene scene;

   private int[] textureIds;

   private static Renderer instance;


   public static Renderer getInstance() {
      if (instance == null) {
         instance = new Renderer();
      }
      return instance;
   }


   private Renderer() {
      super();
   }


   /**
    * @param context - a context for access to file resources
    * @param textureIds - a list of texture references
    */
   public void initialise(Context context, final int[] textureIds) {
      this.context = context;

      mMVPMatrix = new float[16];
      mProjectionMatrix = new float[16];
      mViewMatrix = new float[16];

      viewRotationY = 45f;
      viewRotationX = 45f;
      viewTranslationZ = 0f;
      viewTranslationX = 0f;
      viewZoom = 1f;

      this.textureIds = textureIds;

      viewMatrix = new float[16];
      buildViewMatrix();
   }


   @Override
   public void onSurfaceCreated(GL10 gl, EGLConfig config) {

      // shader loading & compilation
      int vertexShader = 0, fragmentShader = 0;

      try {
         vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
            Util.inputStreamToString(context.getResources().openRawResource(R.raw.vertex)));
         fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
            Util.inputStreamToString(context.getResources().openRawResource(R.raw.fragment)));

      } catch (IOException exception) {
         Log.e(this.getClass().getName(), "error loading shader code");
      }

      program = GLES20.glCreateProgram();
      GLES20.glAttachShader(program, vertexShader);
      GLES20.glAttachShader(program, fragmentShader);
      GLES20.glLinkProgram(program);

      float[] colour = ((OLANdroid) context.getApplicationContext()).getColourTheme(
         R.array.colour_theme_background);

      GLES20.glClearColor(colour[0], colour[1], colour[2], colour[3]);

      GLES20.glEnable(GLES20.GL_CULL_FACE);
      GLES20.glEnable(GLES20.GL_DEPTH_TEST);

      // loading up the textures
      textures = new int[textureIds.length];
      for (int i = 0; i < textureIds.length; i++) {
         textures[i] = loadTexture(textureIds[i]);
      }

      Matrix.setIdentityM(mViewMatrix, 0);

      scene = ((OLANdroid) context.getApplicationContext()).getScene();

      buildViewMatrix();
   }


   @Override
   public void onSurfaceChanged(GL10 gl, int width, int height) {
      GLES20.glViewport(0, 0, width, height);

      float ratio = (float) width / height;

      // projection matrix is applied to object coordinates in the draw method, view distance is
      //    rough, two times the max draw bounds (distance from centre to corner)
      // left, right, bottom, top, near, far, defines the perspective as 2-high with a near of 1,
      //    thus giving a field of view triangle of pi / 2 radians
      Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f,
         2f * (float) Math.pow(Math.pow((double) Renderer.DRAW_BOUNDS, 2) * 2, 0.5));

      // refreshing colours
      float[] colour  = ((OLANdroid) context.getApplicationContext()).getColourTheme(
         R.array.colour_theme_background);

      GLES20.glClearColor(colour[0], colour[1], colour[2], colour[3]);

      buildViewMatrix();
   }


   @Override
   public void onDrawFrame(GL10 unused) {
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
      scene.draw(mMVPMatrix);
   }


   /**
    * refreshes the view matrix, showing where the camera is
    */
   public void buildViewMatrix() {
      float[] temp = new float[16], temp2 = new float[16];

      Matrix.translateM(viewMatrix, 0, mProjectionMatrix, 0, 0f, 0f, -20f * viewZoom);
      Matrix.rotateM(temp, 0, viewMatrix, 0, viewRotationX, 1f, 0f, 0f);
      Matrix.rotateM(temp2, 0, temp, 0, viewRotationY, 0f, 1f, 0f);
      Matrix.translateM(viewMatrix, 0, temp2, 0, viewTranslationX, 0f, viewTranslationZ);

      Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, mViewMatrix, 0);
   }


   /**
    * @return - the view's zoom level
    */
   public float getViewZoom() {
      return viewZoom;
   }


   /**
    * @param viewZoom - a value to set the view's zoom to
    */
   public void setViewZoom(float viewZoom) {
      this.viewZoom = viewZoom;
      buildViewMatrix();
   }


   /**
    * @param delta - a change to the view rotation on the y axis
    */
   public void viewRotationYDelta(float delta) {
      viewRotationY += delta;

      if (viewRotationY > 360f) {
         viewRotationY -= 360f;
      }

      buildViewMatrix();
   }


   /**
    * @return - view rotation on the y axis
    */
   public float getViewRotationY() {
      return viewRotationY;
   }


   /**
    * @param viewRotationY - a value to set the view rotation on the y axis to
    */
   public void setViewRotationY(float viewRotationY) {
      this.viewRotationY = viewRotationY;
      buildViewMatrix();
   }


   /**
    * @param delta - a change to the view rotation on the x axis
    */
   public void viewRotationXDelta(float delta) {
      float test = viewRotationX + delta;

      // view bounds - the floor
      if (test >= 0 && test <= 90) {
         viewRotationX += delta;
         buildViewMatrix();
      }
   }


   /**
    * movement in the direction faced
    * @param delta - amount of movement
    */
   public void viewParallelTranslationDelta(float delta) {
      viewTranslationZ -= Math.cos((viewRotationY / 360f) * Math.PI * 2) * delta;
      viewTranslationX += Math.sin((viewRotationY / 360f) * Math.PI * 2) * delta;

      // keeping the view within the drawing bounds
      float limit = DRAW_BOUNDS / 2f;
      if (viewTranslationZ > limit) {
         viewTranslationZ = limit;
      } else if (viewTranslationZ < -limit) {
         viewTranslationZ = -limit;
      }

      if (viewTranslationX > limit) {
         viewTranslationX = limit;
      } else if (viewTranslationX < -limit) {
         viewTranslationX = -limit;
      }

      buildViewMatrix();
   }


   /**
    * @param type - GLES20.GL_VERTEX_SHADER or GLES20.GL_FRAGMENT_SHADER
    * @param shaderCode - string of glsl
    * @return - reference to the shader program
    */
   public int loadShader(int type, String shaderCode) {
      int shader = GLES20.glCreateShader(type);

      // add the source code to the shader and compile it
      GLES20.glShaderSource(shader, shaderCode);
      GLES20.glCompileShader(shader);

      return shader;
   }


   /**
    * @param resourceId - reference to the texture resource
    * @return - a texture handle
    */
   public int loadTexture(final int resourceId) {
      final int[] textureHandle = new int[1];

      GLES20.glGenTextures(1, textureHandle, 0);

      if (textureHandle[0] != 0) {
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inScaled = false;   // no pre-scaling

         // Read in the resource
         final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId,
            options);

         // bind to the texture in opengl
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

         // set filtering
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST);
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST);

         // load the bitmap into the bound texture
         GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

         bitmap.recycle();
      }

      if (textureHandle[0] == 0) {
         throw new RuntimeException("error loading texture");
      }

      return textureHandle[0];
   }


   /**
    * utility method for debugging opengl calls, provide the name of the call just after making it
    *    if the operation is not successful, the check throws an error
    * @param operation - name of the opengl call to check.
    */
   public static void checkGlError(String operation) {
      int error;
      while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
         throw new RuntimeException(operation + ": gl error " + error);
      }
   }
}
