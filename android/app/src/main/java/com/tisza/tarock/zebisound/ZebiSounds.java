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
		zebiSounds.add(new Licit1(context));
		zebiSounds.add(new RandomSound(context, R.raw.aaa, R.raw.thisistherule, R.raw.csokoladetkivanok, R.raw.csanadipattanaatablara, R.raw.nemtiiranyitotok, R.raw.nincs, R.raw.szaramikororkenyszer, R.raw.beszarjatok));
		zebiSounds.add(new Kemenyenbelecsap(context));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(9), R.raw.kilences));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(1), R.raw.egyesvarhato));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(4), R.raw.negyes));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(16), R.raw.tizenhat));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(19), R.raw.tizenkilenckemeny));
		zebiSounds.add(new CardSound(context, Card.getTarockCard(20), R.raw.huszasmagasc));
		zebiSounds.add(new TarokkKi(context, 12, R.raw.tizenkettotarokkki));
		zebiSounds.add(new TarokkKi(context, 16, R.raw.tizenhattarokkkiultimegvan));
		zebiSounds.add(new Urlap(context));
		zebiSounds.add(new CatchXIX(context));
		zebiSounds.add(new AnnouncementSound(context, new Announcement("jatek", 1), R.raw.kontraparti));
		zebiSounds.add(new Indul(context));
		zebiSounds.add(new AnnouncementSound(context, new Announcement("ketkiralyok", 0), R.raw.ketkiralyok));
	}

	public Collection<ZebiSound> getZebiSounds()
	{
		return zebiSounds;
	}
}
