package com.mobica.openday.android.activity;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.mobica.openday.R;
import com.mobica.openday.android.ui.ChangeColorTransition;


public class MainActivity extends Activity {

	private View title;
	private ViewGroup container;

	private Transition mTransition;
	private Scene[] mScenes;
	private int mCurrentScene = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		title = findViewById(R.id.mainTitle);
		container = (ViewGroup) findViewById(R.id.mainContainer);

		// We set up the Scenes here.
		mScenes = new Scene[]{
				Scene.getSceneForLayout(container, R.layout.activity_main, this),
				Scene.getSceneForLayout(container, R.layout.activity_main_2, this)
		};

		// This is the custom Transition.
		mTransition = new ChangeColorTransition();
		// Show the initial Scene.
		TransitionManager.go(mScenes[mCurrentScene % mScenes.length]);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onWearClick(View view) {
		startActivity(new Intent(this, MobicaFaceConfigurationActivity.class));
//		mCurrentScene = (mCurrentScene + 1) % mScenes.length;
		// Pass the custom Transition as second argument for TransitionManager.go
//		TransitionManager.go(mScenes[mCurrentScene], mTransition);
	}

	private void revealEffect() {
		final int width = title.getWidth();
		final int height = title.getHeight();
		Animator animator = ViewAnimationUtils.createCircularReveal(title, 0,
				height, 0, (float) Math.hypot(width, height));

		// Set a natural ease-in/ease-out interpolator.
		animator.setInterpolator(new AccelerateDecelerateInterpolator());

		animator.setDuration(600);

		// Finally start the animation
		animator.start();
	}
}
