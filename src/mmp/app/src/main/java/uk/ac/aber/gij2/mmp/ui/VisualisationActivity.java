/**
 * @created 2015-01-26
 * @author gideon mw jones.
 */

package uk.ac.aber.gij2.mmp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import java.util.Observable;
import java.util.Observer;

import uk.ac.aber.gij2.mmp.AnimationManager;
import uk.ac.aber.gij2.mmp.MMPApplication;
import uk.ac.aber.gij2.mmp.R;


public class VisualisationActivity extends ActionBarActivity implements Observer,
   SeekBar.OnSeekBarChangeListener {

   private AnimationManager animationManager;
   private SeekBar animationSeek;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_visualisation);

      // animation listening
      animationManager = ((MMPApplication) getApplication()).getAnimationManager();
      animationManager.addObserver(this);

      animationSeek = ((SeekBar) findViewById(R.id.va_seek));

      // resetting animation & seek
      animationManager.setAnimationProgress(1f);
      animationSeek.setProgress(100);

      animationSeek.setOnSeekBarChangeListener(this);
   }


   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_visualisation, menu);

      menu.findItem(R.id.menu_va_play).setIcon(R.drawable.ic_action_play).setTitle(
         R.string.va_play);

      return super.onCreateOptionsMenu(menu);
   }


   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.menu_va_play:
            boolean playing = item.getTitle().equals(getString(R.string.va_play));

            animationManager.animationPlayToggle(playing);
            item.setTitle(playing ? R.string.va_stop : R.string.va_play);
            item.setIcon(playing ? R.drawable.ic_action_stop : R.drawable.ic_action_play);
            return true;

         case R.id.menu_va_save:
            new FlightTitleDialogFragment().show(getFragmentManager(), "flight_title");
            return true;

         case R.id.menu_a_help:
            new HelpDialogFragment(R.string.va_help).show(getFragmentManager(), "help");
            return true;

         case R.id.menu_a_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

         default:
            return super.onOptionsItemSelected(item);
      }
   }


   // seekbar
   @Override
   public void onProgressChanged(SeekBar seekBar, int progress, boolean human) {
      if (human) {
         animationManager.setAnimationProgress((float) progress / 100f);
      }
   }


   // seekbar
   @Override
   public void onStopTrackingTouch(SeekBar seekBar) {}


   // seekbar
   @Override
   public void onStartTrackingTouch(SeekBar seekBar) {}


   // animation
   @Override
   public void update(Observable observable, Object data) {
      animationSeek.setProgress((int) (100f * animationManager.getAnimationProgress()));

      if (animationManager.getAnimationProgress() == 1f) {
         invalidateOptionsMenu();
      }
   }
}
