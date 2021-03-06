/**
 * @created 2015-01-26
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.olandroid.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import uk.ac.aber.gij2.olandroid.controller.OLANdroid;


public class SurfaceView extends GLSurfaceView {

   private Context context;
   private uk.ac.aber.gij2.olandroid.view.Renderer renderer;

   private Touch previous0, previous1;
   private int controlScheme;

   // control schemes
   private static final int TWO_FINGER_ROTATE = 0, ONE_FINGER_ROTATE = 1;

   // would be nice if were final, but can't be as view size is not determined on immediately
   private float ROTATION_SCALE, TRANSLATION_SCALE, euclidean;


   public SurfaceView(Context context, AttributeSet attributes) {
      super(context, attributes);
      setEGLConfigChooser(8, 8, 8, 8, 16, 0);

      renderer = uk.ac.aber.gij2.olandroid.view.Renderer.getInstance();

      setEGLContextClientVersion(2);
      setRenderer(renderer);

      this.context = context;

      controlScheme = TWO_FINGER_ROTATE;

      ROTATION_SCALE = 0.1f;
      TRANSLATION_SCALE = 0.125f;
   }


   /**
    * on startup of the element
    */
   public void onStart() {
      controlScheme = ((OLANdroid) context.getApplicationContext()).getControlScheme();
      previous0 = new Touch(0, 0);
      previous1 = new Touch(0, 0);
   }


   @Override
   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      super.onLayout(changed, left, top, right, bottom);

      // TODO: fine tune
      ROTATION_SCALE = (float) getWidth() / 500f;
      TRANSLATION_SCALE = 0.125f;
   }


   @Override
   public boolean onTouchEvent(@NonNull MotionEvent event) {
      switch (event.getActionMasked()) {
         case MotionEvent.ACTION_DOWN:
         case MotionEvent.ACTION_POINTER_DOWN:
            // if starting a new swipe, reset the start to prevent skipping
            previous0 = new Touch(event.getX(0), event.getY(0));

            if (event.getPointerCount() >= 2) {
               previous1 = new Touch(event.getX(1), event.getY(1));
               euclidean = previous0.euclidean(previous1);

            } else {
               euclidean = 0;
               previous1 = null;
            }
            break;

         case MotionEvent.ACTION_MOVE:
            Touch current0 = new Touch(event.getX(0), event.getY(0));

            // two finger rotate, one finger translate
            if (controlScheme == TWO_FINGER_ROTATE) {
               switch (event.getPointerCount()) {
                  case 1:
                     Touch delta = current0.delta(previous0);

                     renderer.viewParallelTranslationDelta(delta.y * TRANSLATION_SCALE);
                     break;

                  case 2:
                     Touch current1 = new Touch(event.getX(1), event.getY(1));

                     // zooming
                     float zoomFactor = current0.euclidean(current1) / euclidean,
                        previousZoom = renderer.getViewZoom();

                     if (previousZoom / zoomFactor >= 0.2f && previousZoom / zoomFactor < 30) {
                        renderer.setViewZoom(previousZoom / ((zoomFactor * 0.01f) + 0.99f));
                     }

                     // rotating, change as degrees
                     float angleDelta = (float) (((
                        Math.atan(current0.gradient(current1))
                           - Math.atan(previous0.gradient(previous1)))
                        / (2f * Math.PI)) * 360f);

                     // don't want huge changes (happens due to tan's occasional infiniteness)
                     renderer.viewRotationYDelta(
                        angleDelta > 90 || angleDelta < -90 ? 0f : -angleDelta * ROTATION_SCALE);

                     previous1 = current1;
                     break;
               }


            // one finger rotate, two finger translate
            } else if (controlScheme == ONE_FINGER_ROTATE) {

               Touch delta = current0.delta(previous0);

               switch (event.getPointerCount()) {
                  case 1:
                     renderer.viewRotationYDelta(delta.x * ROTATION_SCALE);
                     renderer.viewRotationXDelta(-delta.y * ROTATION_SCALE);
                     break;

                  case 2:
                     renderer.viewParallelTranslationDelta(delta.y * TRANSLATION_SCALE);
                     break;
               }
            }

            previous0 = current0;
            break;
      }

      return true;
   }


   /**
    * small utility class for encapsulating the coordinates of a point
    */
   private class Touch {
      public float x, y;


      /**
       * @param x - x coordinate
       * @param y - y coordinate
       */
      public Touch(float x, float y) {
         this.x = x;
         this.y = y;
      }


      /**
       * @param touch - another touch point
       * @return - length of a line between the two touches
       */
      public float euclidean(Touch touch) {
         return (float) Math.sqrt(Math.pow(x - touch.x, 2) + Math.pow(y - touch.y, 2));
      }


      /**
       *
       * @param touch - another touch point
       * @return - gradient of a line joining the two touches
       */
      public float gradient(Touch touch) {
         return (x - touch.x) / (y - touch.y);
      }


      /**
       * @param touch - another touch point
       * @return - coordinate difference between the two touches
       */
      public Touch delta(Touch touch) {
         return new Touch (x - touch.x, y - touch.y);
      }
   }
}
