package com.tisza.tarock.gui;

import android.annotation.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.card.*;

@SuppressLint("AppCompatCustomView")
public class PlayedCardView extends ImageView
{
	private final Handler takeAnimationHandler;

	private final int width;
	private final int orientation;
	private boolean isTaking;
	private Card currentCard;
	private Card takenCard;
	private int takenDir;

	public PlayedCardView(Context context, int width, int orientation)
	{
		super(context);
		takeAnimationHandler = new Handler();
		this.orientation = orientation;
		this.width = width;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		setLayoutParams(lp);
		setAdjustViewBounds(true);
		reset();
	}

	public void reset()
	{
		currentCard = null;
		takenCard = null;
		isTaking = false;
		resetPosition();
		updateImage();
	}

	private Animation createPositionAnimation()
	{
		Animation rotateAnim = new RotateAnimation(0, (orientation % 2) * 90, RotateAnimation.RELATIVE_TO_SELF, 0.5F,RotateAnimation.RELATIVE_TO_SELF, 0.5F);

		float tx = 0;
		float ty = 0;
		switch (orientation)
		{
			case 0: ty = 1; break;
			case 1: tx = 1; break;
			case 2: ty = -1; break;
			case 3: tx = -1; break;
		}
		tx *= width * GameFragment.PLAYED_CARD_DISTANCE;
		ty *= width * GameFragment.PLAYED_CARD_DISTANCE;
		Animation translateAnim = new TranslateAnimation(0, tx, 0, ty);

		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(rotateAnim);
		animSet.addAnimation(translateAnim);

		return animSet;
	}

	private void resetPosition()
	{
		Animation currentAnimation = getAnimation();
		if (currentAnimation != null)
			clearAnimation();

		Animation animation = createPositionAnimation();
		animation.setDuration(0);
		animation.setInterpolator(new EndInterpolator());
		animation.setFillAfter(true);
		startAnimation(animation);
	}

	private void updateImage()
	{
		if (isTaking)
			setCardImage(takenCard);
		else if (currentCard != null)
			setCardImage(currentCard);
		else if (takenCard != null)
			setImageResource(R.drawable.card_back);
		else
			setCardImage(null);
	}

	private void setCardImage(Card card)
	{
		if (card == null)
			setImageBitmap(null);
		else
			setImageResource(ResourceMappings.getBitmapResForCard(card));
	}

	public void play(Card card)
	{
		currentCard = card;

		if (isTaking)
			return;

		updateImage();
		bringToFront();
		startTakePlayAnimation(true, orientation);
	}
	
	public void take(int dir)
	{
		takenCard = currentCard;
		takenDir = dir;
		currentCard = null;
		resetPosition();
		isTaking = true;
		takeAnimationHandler.postDelayed(() -> startTakePlayAnimation(false, dir), GameFragment.DELAY);
	}

	public void showTaken()
	{
		if (isTaking || takenCard == null)
			return;

		resetPosition();
		isTaking = true;
		updateImage();
		takeAnimationHandler.postDelayed(() -> startTakePlayAnimation(false, takenDir), GameFragment.DELAY);
	}

	private void startTakePlayAnimation(boolean play, int dir)
	{
		float tx = 0;
		float ty = 0;
		switch (dir)
		{
			case 0: ty = 0.5F; break;
			case 1: tx = 0.5F; break;
			case 2: ty = -0.5F; break;
			case 3: tx = -0.5F; break;
		}

		Interpolator interpolator = play ? new LinearInterpolator() : new ReverseInterpolator();
		int duration = play ? GameFragment.PLAY_DURATION : GameFragment.TAKE_DURATION;

		Animation moveAnim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, tx, TranslateAnimation.RELATIVE_TO_PARENT, 0, TranslateAnimation.RELATIVE_TO_PARENT, ty, TranslateAnimation.RELATIVE_TO_PARENT, 0);
		Animation positionAnim = createPositionAnimation();
		moveAnim.setInterpolator(interpolator);
		positionAnim.setInterpolator(interpolator);

		AnimationSet animSet = new AnimationSet(false);
		animSet.addAnimation(positionAnim);
		animSet.addAnimation(moveAnim);
		animSet.setDuration(duration);
		animSet.setFillAfter(true);

		animSet.setAnimationListener(new AnimationListener()
		{
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				if (!play)
					isTaking = false;

				if (currentCard != null)
					resetPosition();

				updateImage();
			}
		});

		startAnimation(animSet);
	}
	
	public boolean isTaking()
	{
		return isTaking;
	}
}
