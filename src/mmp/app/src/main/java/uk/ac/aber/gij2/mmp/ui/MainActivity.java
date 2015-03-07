/**
 * @created 2015-01-25
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.mmp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import uk.ac.aber.gij2.mmp.R;


public class MainActivity extends ActionBarActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      TextView link = (TextView) findViewById(R.id.ma_intro_link);
      link.setMovementMethod(LinkMovementMethod.getInstance());
   }


   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return super.onCreateOptionsMenu(menu);
   }


   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.menu_a_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

         default:
            return super.onOptionsItemSelected(item);
      }
   }


   /**
    * listener on the start building a flight button
    * @param view - view element source
    */
   public void button_flight_manager(View view) {
      startActivity(new Intent(this, FlightManagerActivity.class));
   }
}
