package com.tisza.tarock.test;

import android.view.*;
import androidx.test.espresso.*;
import androidx.test.espresso.util.*;
import com.tisza.tarock.game.card.*;
import org.hamcrest.*;
import org.hamcrest.core.*;

import java.util.concurrent.*;

import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

public class CustomMatchers
{
	public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher,final int childPosition){
		return new TypeSafeMatcher<View>(){
			@Override public void describeTo(Description description){
				description.appendText("position "+childPosition+" of parent ");
				parentMatcher.describeTo(description);
			}

			@Override public boolean matchesSafely(View view){
				if(!(view.getParent()instanceof ViewGroup))return false;
				ViewGroup parent=(ViewGroup)view.getParent();

				return parentMatcher.matches(parent)
						&&parent.getChildCount()>childPosition
						&&parent.getChildAt(childPosition).equals(view);
			}
		};
	}

	public static ViewAction waitView(Matcher<? super View> viewMatcher, final long millis) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return instanceOf(View.class);
			}

			@Override
			public String getDescription() {
				return "wait for a specific view during " + millis + " millis.";
			}

			@Override
			public void perform(final UiController uiController, final View view) {
				uiController.loopMainThreadUntilIdle();
				final long startTime = System.currentTimeMillis();
				final long endTime = startTime + millis;

				do {
					for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
						// found view with required ID
						if (viewMatcher.matches(child)) {
							return;
						}
					}

					uiController.loopMainThreadForAtLeast(50);
				}
				while (System.currentTimeMillis() < endTime);

				// timeout happens
				throw new PerformException.Builder()
						.withActionDescription(this.getDescription())
						.withViewDescription(HumanReadables.describe(view))
						.withCause(new TimeoutException())
						.build();
			}
		};
	}

	public static Matcher<Object> isFoldableCard()
	{
		return new BaseMatcher<Object>()
		{
			@Override
			public boolean matches(Object card)
			{
				return card instanceof SuitCard && ((SuitCard)card).getValue() != 5 || card instanceof TarockCard && !((TarockCard)card).isHonor();
			}

			@Override
			public void describeTo(Description description)
			{
				description.appendText("card is foldable");
			}
		};
	}

}
