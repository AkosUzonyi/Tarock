package com.tisza.tarock.gui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.tisza.tarock.R;
import com.tisza.tarock.card.Card;

import java.util.LinkedList;

public class PlayedCardView extends ImageView
{
	private int orientation;
	
	private LinkedList<Integer> imgResourcesQueue = new LinkedList<Integer>();

	private boolean isAnimating = false;

	public PlayedCardView(Context context, int width, int orientation)
	{
		super(context);
		this.orientation = orientation;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		setLayoutParams(lp);
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if (!isAnimating)
		{
			startAnimation(createPositionAnimation());
		}
	}
	
	public void addCard(Card c)
	{
		int res = getBitmapResForCard(c);
		imgResourcesQueue.add(res);
		setImageResource(res);
	}
	
	public void removeFirstCard()
	{
		if (!imgResourcesQueue.isEmpty())
		{
			imgResourcesQueue.remove();
			if (imgResourcesQueue.isEmpty())
			{
				setImageBitmap(null);
			}
		}
		else
		{
			Log.e(GameActivtiy.LOG_TAG, "Tried to remove a card from an empty PlayedCardView");
		}
	}
	
	public Animation createPositionAnimation()
	{
		int w = getWidth();
		int h = getHeight();
		
		Animation rotateAnim = new RotateAnimation(0, orientation * 90, w / 2, h / 2);
		
		float tx = 0;
		float ty = 0;
		if (orientation == 0)
		{
			ty = 1;
		}
		else if (orientation == 1)
		{
			tx = 1;
		}
		else if (orientation == 2)
		{
			ty = -1;
		}
		else if (orientation == 3)
		{
			tx = -1;
		}
		tx *= h / 4;
		ty *= h / 4;
		Animation translateAnim = new TranslateAnimation(0, tx, 0, ty);
		System.out.println(tx);
		
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(rotateAnim);
		animSet.addAnimation(translateAnim);
		animSet.setDuration(0);
		animSet.setInterpolator(new EndInterpolator());
		animSet.setFillAfter(true);
		
		return animSet;
	}
	
	public void animatePlay()
	{
		startTakePlayAnimation(true, orientation);
	}
	
	public void animateTake(int dir)
	{
		startTakePlayAnimation(false, dir);
	}
	
	private void startTakePlayAnimation(final boolean play, int dir)
	{
		Animation currentAnimation = getAnimation();
		if (currentAnimation != null)
		{
			currentAnimation.cancel();
		}
		
		isAnimating = true;
		
		final Animation positionAnim = createPositionAnimation();
		positionAnim.setInterpolator(play ? new EndInterpolator() : new ReverseInterpolator());
		positionAnim.setDuration(play ? 0 : 800);
		
		View parent = (View)getParent();
		float tx = 0;
		float ty = 0;
		if (dir == 0)
		{
			ty = parent.getHeight() / 2;
		}
		else if (dir == 1)
		{
			tx = parent.getWidth() / 2;
		}
		else if (dir == 2)
		{
			ty = -parent.getHeight() / 2;
		}
		else if (dir == 3)
		{
			tx = -parent.getWidth() / 2;
		}
		float fromX = play ? tx : 0;
		float toX =   play ? 0  : tx;
		float fromY = play ? ty : 0;
		float toY =   play ? 0  : ty;
		Animation takeAnim = new TranslateAnimation(fromX, toX, fromY, toY);
		takeAnim.setDuration(800);
		
		AnimationSet animSet = new AnimationSet(false);
		animSet.addAnimation(positionAnim);
		animSet.addAnimation(takeAnim);
		startAnimation(animSet);
		
		animSet.setAnimationListener(new AnimationListener()
		{
			public void onAnimationStart(Animation animation)
			{
			}
			
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			public void onAnimationEnd(Animation animation)
			{
				if (!play)
				{
					removeFirstCard();
				}
				startAnimation(createPositionAnimation());
				isAnimating = false;
			}
		});
	}
	
	public boolean isAnimating()
	{
		return isAnimating;
	}
	
	public static int getBitmapResForCard(Card card)
	{
		if (ResourceMappings.cardToImageResource.containsKey(card))
		{
			return ResourceMappings.cardToImageResource.get(card);
		}
		else
		{
			Log.e(GameActivtiy.LOG_TAG, card + " has no image");
			return R.drawable.a1;
		}
	}
}
