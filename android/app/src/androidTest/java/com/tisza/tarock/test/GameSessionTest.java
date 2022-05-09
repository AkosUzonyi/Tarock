package com.tisza.tarock.test;

import android.widget.*;
import androidx.test.espresso.*;
import androidx.test.ext.junit.rules.*;
import androidx.test.filters.*;
import androidx.test.runner.*;
import com.tisza.tarock.R;
import com.tisza.tarock.gui.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.tisza.tarock.test.CustomMatchers.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GameSessionTest
{
	private static final String userName = "Uzonyi Ákos";

	@Rule
	public ActivityScenarioRule<MainActivity> activityRule =
			new ActivityScenarioRule<>(MainActivity.class);

	@Test
	public void testCreateGameSession() {
		onView(withId(R.id.game_list)).check(doesNotExist());
		onView(withId(R.id.play_button)).perform(click());
		onView(withId(R.id.game_list)).check(matches(isDisplayed()));
		onView(withId(R.id.new_game_button)).perform(click());
		onView(withId(R.id.game_type_spinner)).perform(click());
		onData(is("Magas")).perform(click());
		onView(withId(R.id.create_game_button)).perform(click());
		onView(nthChildOf(withId(R.id.game_list), 0)).check(matches(allOf(hasDescendant(withText("Magas")), hasDescendant(withText(userName)))));
		onView(allOf(isDescendantOfA(nthChildOf(withId(R.id.game_list), 0)), withId(R.id.join_game_button))).perform(click());
		onView(withId(R.id.lobby_start_button)).perform(click());
		onView(withText("Indítás")).perform(click());

		while (true)
		{
			try
			{
				onView(withId(R.id.available_actions_list)).perform(waitView(instanceOf(Button.class), 7000));
			}
			catch (PerformException e)
			{
				break;
			}
			onView(nthChildOf(withId(R.id.available_actions_list), 0)).perform(click());
		}
		onView(withId(R.id.messages_text_view)).check(matches(withText(containsString("SKARTOLÁS"))));
	}

	@Test
	public void testDeleteGameSession() {
		onView(withId(R.id.game_list)).check(doesNotExist());
		onView(withId(R.id.play_button)).perform(click());
		onView(withId(R.id.game_list)).check(matches(isDisplayed()));
		onView(withId(R.id.new_game_button)).perform(click());
		onView(withId(R.id.create_game_button)).perform(click());

		onView(allOf(isDescendantOfA(nthChildOf(withId(R.id.game_list), 0)), withText("Törlés"))).perform(click());
		onView(withText("TÖRLÉS")).perform(click());
		while (true)
		{
			try
			{
				onView(allOf(isDescendantOfA(nthChildOf(withId(R.id.game_list), 0)), withText("Törlés"))).perform(click());
			}
			catch (NoMatchingViewException e)
			{
				break;
			}
			onView(withText("TÖRLÉS")).perform(click());
		}
	}
}
