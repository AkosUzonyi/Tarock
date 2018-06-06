package com.tisza.tarock.gui;

import com.tisza.tarock.*;
import com.tisza.tarock.card.*;

import java.util.*;

public class UltimoSelector
{
	private List<AvailableUltimoEntry> ultimos = new ArrayList<>();
	private List<UltimoProperty> selectedProperties = new ArrayList<>();
	private List<List<UltimoProperty>> availableProperties = new ArrayList<>();
	private Announcement selectedAnnouncement = null;

	public void takeAnnouncements(Collection<Announcement> announcements)
	{
		ultimos.clear();
		selectedProperties.clear();
		availableProperties.clear();

		for (Iterator<Announcement> it = announcements.iterator(); it.hasNext();)
		{
			AvailableUltimoEntry ultimoEntry = AvailableUltimoEntry.createFromAnnouncement(it.next());
			if (ultimoEntry != null)
			{
				ultimos.add(ultimoEntry);
				it.remove();
			}
		}

		updateAvailableProperties();
	}

	public boolean hasAnyUltimo()
	{
		return !ultimos.isEmpty();
	}

	public Announcement getCurrentSelectedAnnouncement()
	{
		return selectedAnnouncement;
	}

	public boolean selectProperty(int pos, UltimoProperty property)
	{
		if (pos >= availableProperties.size())
			throw new IllegalArgumentException();

		if (pos < selectedProperties.size() && selectedProperties.get(pos).equals(property))
			return false;

		selectedProperties.subList(pos, selectedProperties.size()).clear();
		if (property != null)
			selectedProperties.add(property);

		updateAvailableProperties();

		return true;
	}

	public List<List<UltimoProperty>> getAvailableProperties()
	{
		return availableProperties;
	}

	private void updateAvailableProperties()
	{
		int selectedSize = selectedProperties.size();

		SortedSet<UltimoProperty> newAvailableProperties = new TreeSet<>();

		for (AvailableUltimoEntry ultimo : ultimos)
		{
			if (ultimo.doesMatch(selectedProperties))
			{
				int ultimoSize = ultimo.getProperties().size();

				if (selectedSize < ultimoSize)
				{
					newAvailableProperties.add(ultimo.getProperties().get(selectedSize));
				}
				else if (selectedSize == ultimoSize)
				{
					selectedAnnouncement = ultimo.getAnnouncement();
				}
			}
		}

		availableProperties.subList(selectedSize, availableProperties.size()).clear();
		if (!newAvailableProperties.isEmpty())
			availableProperties.add(new ArrayList<>(newAvailableProperties));
	}

	private static class AvailableUltimoEntry
	{
		private final List<UltimoProperty> properties;
		private final Announcement announcement;

		public AvailableUltimoEntry(List<UltimoProperty> properties, Announcement announcement)
		{
			this.properties = properties;
			this.announcement = announcement;
		}

		public boolean doesMatch(List<UltimoProperty> selectedProperties)
		{
			if (selectedProperties.size() > properties.size())
				return false;

			for (int i = 0; i < selectedProperties.size(); i++)
			{
				if (!properties.get(i).equals(selectedProperties.get(i)))
					return false;
			}

			return true;
		}

		public List<UltimoProperty> getProperties()
		{
			return properties;
		}

		public Announcement getAnnouncement()
		{
			return announcement;
		}

		public static AvailableUltimoEntry createFromAnnouncement(Announcement announcement)
		{
			if (announcement.getContraLevel() != 0)
				return null;

			List<UltimoProperty> properties = new ArrayList<>();
			switch (announcement.getName())
			{
				case "ultimo":
					if (!announcement.hasCard())
						throw new RuntimeException();

					Card card = announcement.getCard();
					if (card instanceof TarockCard)
					{
						properties.add(new TarockCardProperty(((TarockCard)card).getValue()));
					}
					else if (card instanceof SuitCard)
					{
						properties.add(new SuitCardValueProperty(((SuitCard)card).getValue()));
						properties.add(new SuitProperty(((SuitCard)card).getSuit()));
					}
					else
					{
						throw new RuntimeException();
					}
					break;
				case "kisszincsalad":
					properties.add(new KisszincsaladProperty());
					break;
				case "nagyszincsalad":
					properties.add(new NagyszincsaladProperty());
					break;
				default:
					return null;
			}

			if (announcement.hasSuit())
				properties.add(new SuitProperty(announcement.getSuit()));

			if (announcement.hasRound())
				properties.add(new RoundProperty(announcement.getRound()));

			return new AvailableUltimoEntry(properties, announcement);
		}
	}

	public interface UltimoProperty extends Comparable<UltimoProperty>
	{
		@Override
		public default int compareTo(UltimoProperty other)
		{
			return hashCode() - other.hashCode();
		}
	}

	private static class TarockCardProperty implements UltimoProperty
	{
		private final int value;

		public TarockCardProperty(int value)
		{
			this.value = value;
		}

		@Override
		public int hashCode()
		{
			return 0 + value;
		}

		@Override
		public boolean equals(Object other)
		{
			if (!(other instanceof TarockCardProperty))
				return false;

			return value == ((TarockCardProperty)other).value;
		}

		@Override
		public String toString()
		{
			return ResourceMappings.tarockNames[value - 1];
		}
	}

	private static class SuitCardValueProperty implements UltimoProperty
	{
		private final int value;

		public SuitCardValueProperty(int value)
		{
			this.value = value;
		}

		@Override
		public int hashCode()
		{
			return 100 - value;
		}

		@Override
		public boolean equals(Object other)
		{
			if (!(other instanceof SuitCardValueProperty))
				return false;

			return value == ((SuitCardValueProperty)other).value;
		}

		@Override
		public String toString()
		{
			return ResourceMappings.suitcardValueNames[value - 1];
		}
	}

	private static class KisszincsaladProperty implements UltimoProperty
	{
		@Override
		public int hashCode()
		{
			return 200 + 0;
		}

		@Override
		public boolean equals(Object other)
		{
			return other instanceof KisszincsaladProperty;
		}

		@Override
		public String toString()
		{
			return ResourceMappings.getAnnouncementNameText("kisszincsalad");
		}
	}

	private static class NagyszincsaladProperty implements UltimoProperty
	{
		@Override
		public int hashCode()
		{
			return 200 + 1;
		}

		@Override
		public boolean equals(Object other)
		{
			return other instanceof NagyszincsaladProperty;
		}

		@Override
		public String toString()
		{
			return ResourceMappings.getAnnouncementNameText("nagyszincsalad");
		}
	}

	private static class SuitProperty implements UltimoProperty
	{
		private final int suit;

		public SuitProperty(int suit)
		{
			this.suit = suit;
		}

		@Override
		public int hashCode()
		{
			return 300 + suit;
		}

		@Override
		public boolean equals(Object other)
		{
			if (!(other instanceof SuitProperty))
				return false;

			return suit == ((SuitProperty)other).suit;
		}

		@Override
		public String toString()
		{
			return ResourceMappings.suitNames[suit];
		}
	}

	private static class RoundProperty implements UltimoProperty
	{
		private final int round;

		public RoundProperty(int round)
		{
			this.round = round;
		}

		@Override
		public int hashCode()
		{
			return 400 - round;
		}

		@Override
		public boolean equals(Object other)
		{
			if (!(other instanceof RoundProperty))
				return false;

			return round == ((RoundProperty)other).round;
		}

		@Override
		public String toString()
		{
			return ResourceMappings.roundNames[round];
		}
	}
}
