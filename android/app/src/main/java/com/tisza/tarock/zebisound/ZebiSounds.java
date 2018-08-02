package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

public class ZebiSounds
{
	private Collection<ZebiSound> zebiSounds = new ArrayList<>();

	public ZebiSounds(Context context)
	{
		zebiSounds.add(new Ezisamienk(context));
		zebiSounds.add(new DuplaFail(context));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(10), R.raw.tizes));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(14), R.raw.tizennegy));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(15), R.raw.tizenot));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(17), R.raw.tizenhetes, R.raw.tizenhetesisvolt));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(21), R.raw.huszonegy));
		zebiSounds.add(new CardSound(context, Card.getSuitCard(0, 5), R.raw.korkiraly));
		zebiSounds.add(new CardSound(context, Card.getSuitCard(3, 5), R.raw.treffkiraly));
		zebiSounds.add(new CardSound(context, Card.getSuitCard(3, 1), R.raw.trefftizestamad));
		zebiSounds.add(new DamaMienk(context));
		zebiSounds.add(new Huszonegyfogasveszely(context));
		zebiSounds.add(new Nanezzuk(context));
		zebiSounds.add(new PagatMentikATrult(context));
		zebiSounds.add(new Piciharom(context));
		zebiSounds.add(new Plicit(context));
		zebiSounds.add(new ProbaljFacant(context));
		zebiSounds.add(new SoloInvitGondolkodik(context));
		zebiSounds.add(new TarokkFekszik(context));
		zebiSounds.add(new PikkKiralyIndul(context));
		zebiSounds.add(new AnnouncementSound(context, new Announcement("banda", 1).setSuit(3), R.raw.kontrapikkbandadisznohus));
		zebiSounds.add(new AnnouncementSound(context, new Announcement("banda", 1).setSuit(4), R.raw.kontratreffbanda));
		zebiSounds.add(new Jobbfelni(context));
		zebiSounds.add(new Kontranemkontra(context));
		zebiSounds.add(new Skizultimo(context));
		zebiSounds.add(new Szuperlicit(context));
	}

	public Collection<ZebiSound> getZebiSounds()
	{
		return zebiSounds;
	}
}
