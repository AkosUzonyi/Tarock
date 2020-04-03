package com.tisza.tarock.gui;

import android.animation.*;
import android.annotation.*;
import android.content.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.card.*;

@SuppressLint("AppCompatCustomView")
public class PlayedCardView extends ImageView
{
	private final int width;
	private final Orientation orientation;

	private boolean isTaking;
	private Card currentCard;
	private Card takenCard;
	private Orientation takenDir;
	private Animator currentAnimator;

	public PlayedCardView(Context context, int width, int orientation)
	{
		super(context);
		this.orientation = Orientation.fromInt(orientation);
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

	private void resetPosition()
	{
		if (currentAnimator != null)
		{
			currentAnimator.cancel();
			currentAnimator = null;
		}

		setTranslationX(width * GameFragment.PLAYED_CARD_DISTANCE * orientation.getX());
		setTranslationY(width * GameFragment.PLAYED_CARD_DISTANCE * orientation.getY());
		setRotation(orientation.isHorizontal() ? 90 : 0);
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
		startTakePlayAnimation(true);
	}
	
	public void take(int dir)
	{
		takenCard = currentCard;
		takenDir = Orientation.fromInt(dir);
		currentCard = null;
		isTaking = true;
		startTakePlayAnimation(false);
	}

	public void showTaken()
	{
		if (isTaking || takenCard == null)
			return;

		isTaking = true;
		updateImage();
		startTakePlayAnimation(false);
	}

	private void startTakePlayAnimation(boolean play)
	{
		resetPosition();

		Orientation dir = play ? orientation : takenDir;
		float tx = (((View)getParent()).getWidth() * 0.5F + width * 0.25F) * dir.getX();
		float ty = (((View)getParent()).getHeight() * 0.5F + width * 0.25F) * dir.getY();

		Interpolator interpolator = play ? new ReverseInterpolator() : new LinearInterpolator();
		int duration = play ? GameFragment.PLAY_DURATION : GameFragment.TAKE_DURATION;

		ObjectAnimator moveXAnim = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, getTranslationX(), tx);
		ObjectAnimator moveYAnim = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, getTranslationY(), ty);
		ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(this, View.ROTATION, getRotation(), 0);

		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(moveXAnim, moveYAnim, rotateAnim);
		animSet.setInterpolator(interpolator);
		animSet.setDuration(duration);
		animSet.setStartDelay(play ? 0 : GameFragment.DELAY);
		animSet.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				currentAnimator = null;

				if (!play)
				{
					isTaking = false;

					if (currentCard == null)
					{
						setTranslationX(tx);
						setTranslationY(ty);
						setRotation(0);
					}
					else
					{
						resetPosition();
					}
				}

				updateImage();
			}
		});
		animSet.start();
		currentAnimator = animSet;
	}

	public boolean isTaking()
	{
		return isTaking;
	}

	public boolean isTaken()
	{
		return currentCard == null && takenCard != null;
	}

	enum Orientation
	{
		DOWN(0, 1), RIGHT(1, 0), UP(0, -1), LEFT(-1, 0);

		private final int x, y;

		Orientation(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		public static Orientation fromInt(int n)
		{
			return values()[n];
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public boolean isHorizontal()
		{
			return ordinal() % 2 == 1;
		}
	}
}
