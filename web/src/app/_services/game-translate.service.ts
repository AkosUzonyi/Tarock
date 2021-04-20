import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class GameTranslateService {
  constructor(
    private translateService: TranslateService,
  ) { }

  private tsi(key: string): string {
    return this.translateService.instant(key);
  }

  translateCard(card: string): string | null {
    const type = card[0];
    const value = Number(card.substring(1));

    if (type == 't') {
      if (value < 1 || value >= 23)
        throw 'invalid card: ' + card;

      return this.tsi('tarock_array.' + (value - 1));
    }
    else {
      const suitIndex = type.charCodeAt(0) - 'a'.charCodeAt(0);
      if (suitIndex < 0 || suitIndex >= 4 || value < 1 || value >= 6)
        throw 'invalid card: ' + card;

      const suitKey = 'suit_array.' + suitIndex;
      const valueKey = 'suitcard_value_array.' + (value - 1);
      return this.tsi(suitKey) + ' ' + this.tsi(valueKey);
    }
  }

  translateAnnouncement(announcement: string): string | null {
    let contraLevel = 0;
    let suit: string | null = null;
    let card: string | null = null;
    let trick: number | null = null;

    let pos = 0;

    const name = announcement.substring(pos, pos = this.nextUppercase(announcement, pos));

    while (pos < announcement.length) {
      const c = announcement.charAt(pos++);
      const substr = announcement.substring(pos, pos = this.nextUppercase(announcement, pos));

      switch (c) {
        case 'S':
          suit = substr;
          break;
        case 'C':
          card = substr;
          break;
        case 'T':
          trick = Number(substr);
          break;
        case 'K':
          contraLevel = substr === 's' ? -1 : Number(substr);
          break;
        default:
          throw 'invalid announcement modifier: ' + c;
      }
    }

    const sentenceBuilder = new SentenceBuilder();

    if (contraLevel < 0)
      sentenceBuilder.appendWord(this.tsi('silent'));
    else
      sentenceBuilder.appendWord(this.tsi('contra_array.' + contraLevel));

    if (suit !== null)
      sentenceBuilder.appendWord(this.tsi('suit_array.' + (suit.charCodeAt(0) - 'a'.charCodeAt(0))));
    if (card !== null)
      sentenceBuilder.appendWord(this.translateCard(card));
    if (trick !== null)
      sentenceBuilder.appendWord(this.tsi('trick_array.' + trick));

    sentenceBuilder.appendWord(this.tsi('announcement.' + name));

    return sentenceBuilder.str;
  }

  private nextUppercase(str: string, pos: number): number {
    let i = 0;
    for (i = pos; i < str.length; i++)
      if (str.charCodeAt(i) >= 'A'.charCodeAt(0) && str.charCodeAt(i) <= 'Z'.charCodeAt(0))
        break;
    return i;
  }

  translateAction(action: string): string | null {
    const colonIndex = action.indexOf(':');
    const type = action.substring(0, colonIndex);
    const params = action.substring(colonIndex + 1);
    switch (type)
    {
      case 'bid':
        return this.tsi('bid.' + params);
      case 'call':
        return this.translateCard(params);
      case 'announce':
        if (params === 'passz')
          return this.tsi('passz');
        else
          return this.translateAnnouncement(params);
      case 'throw':
        return this.tsi('message_cards_thrown');
    }
    return null;
  }
}


class SentenceBuilder {
	str: string = '';

	appendWord(word: string | null) {
		if (!word)
			return;

		if (this.str.length == 0)
			this.str += word.substring(0, 1).toUpperCase() + word.substring(1);
		else
			this.str += ' ' + word;
	}
}
