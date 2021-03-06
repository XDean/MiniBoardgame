package guobiao

import (
	"fmt"
	"github.com/pkg/errors"
	"strings"
)

const (
	mode_public = iota
	mode_private
	mode_last
)

func Parse(str string) (Hand, error) {
	cardType := ZI

	mode := mode_public

	publicCards := Cards{}
	publicType := GT_QI_XING_BU_KAO

	newPublicType := GT_QI_XING_BU_KAO
	newCard := NIL_CARD

	hand := Hand{
		Public:  []Group{},
		Private: Cards{},
		Last:    NIL_CARD,
	}

	count := 0
	for _, c := range str {
		count++
		switch c {
		case ' ', '\t':
			continue
		case ',', '，':
			switch mode {
			case mode_public:
				if publicCards.Size() != 0 {
					return hand, parseError(count, "未完成的吃碰杠")
				}
				mode = mode_private
			case mode_private:
				mode = mode_last
			case mode_last:
				return hand, parseError(count, "多余的块")
			}
		case '条':
			cardType = TIAO
		case '饼', '筒':
			cardType = BING
		case '万':
			cardType = WAN
		case '吃':
			newPublicType = GT_CHI
		case '碰':
			newPublicType = GT_PENG
		case '暗':
			newPublicType = GT_AN_GANG
		case '明':
			newPublicType = GT_MING_GANG
		case '摸':
			if mode != mode_last {
				return hand, parseError(count, "非法的自摸")
			}
			if hand.ZiMo {
				return hand, parseError(count, "冗余的自摸")
			}
			hand.ZiMo = true
		case '东':
			newCard = DONG_CARD
		case '南':
			newCard = NAN_CARD
		case '西':
			newCard = XI_CARD
		case '北':
			newCard = BEI_CARD
		case '中':
			newCard = ZHONG_CARD
		case '发':
			newCard = FA_CARD
		case '白':
			newCard = BAI_CARD
		case '1', '2', '3', '4', '5', '6', '7', '8', '9':
			if cardType == ZI {
				return hand, parseError(count, "未指定牌类型")
			}
			newCard = Card{Type: cardType, Point: int(c) - '0'}
		}
		if newPublicType.Public {
			if mode != mode_public {
				return hand, parseError(count, "只能在第一部分吃碰杠")
			}
			if publicCards.Size() != 0 {
				return hand, parseError(count, "前一组吃碰杠未完成")
			}
			publicType = newPublicType
			newPublicType = GT_QI_XING_BU_KAO
		}
		if newCard != NIL_CARD {
			switch mode {
			case mode_public:
				if publicType.Public {
					if publicType == GT_CHI {
						publicCards[newCard] += 1
					} else {
						publicCards[newCard] += publicType.CardCount
					}
					match := false
					if publicCards.Size() == publicType.CardCount {
						for card, _ := range publicCards {
							if ok, group, _ := publicType.Find(publicCards, card); ok {
								hand.Public = append(hand.Public, group)
								match = true
								publicCards = Cards{}
							}
						}
						if !match {
							return hand, parseError(count, "错误的牌组合")
						}
					}
				} else {
					return hand, parseError(count, "未指定吃碰杠类型")
				}
			case mode_private:
				hand.Private[newCard] += 1
			case mode_last:
				if hand.Last == NIL_CARD {
					hand.Last = newCard
				} else {
					return hand, parseError(count, "多余的胡牌")
				}
			}
			newCard = NIL_CARD
		}
	}
	if mode != mode_last {
		return hand, parseError(len(str), "缺少胡牌块")
	}
	if hand.Last == NIL_CARD {
		return hand, parseError(len(str), "缺少胡牌")
	}
	if hand.CardCount() != 14 {
		return hand, parseError(len(str), "牌数应为14张")
	}
	if !hand.Private.HasCard(hand.Last) {
		return hand, parseError(len(str), "胡牌不存在")
	}
	return hand, nil
}

func Format(hand Hand) string {
	builder := strings.Builder{}
	for _, g := range hand.Public {
		switch g.Type {
		case GT_CHI:
			builder.WriteRune('吃')
		case GT_PENG:
			builder.WriteRune('碰')
		case GT_MING_GANG:
			builder.WriteRune('明')
		case GT_AN_GANG:
			builder.WriteRune('暗')
		}

		ac := g.Cards.Any()
		if ac.Type != ZI {
			builder.WriteString(ac.Type.String())
		}
		if g.Type == GT_CHI {
			for _, card := range g.Cards.ToSortedArray() {
				builder.WriteRune(card.FormatPoint())
			}
		} else {
			builder.WriteRune(ac.FormatPoint())
		}
		builder.WriteRune(' ')
	}

	builder.WriteString(",")

	currentType := ZI
	for _, card := range hand.Private.ToSortedArray() {
		if card.Type != currentType {
			builder.WriteRune(' ')
			if card.Type != ZI {
				builder.WriteString(card.Type.String())
			}
			currentType = card.Type
		}
		builder.WriteRune(card.FormatPoint())
	}
	builder.WriteString(" , ")
	if hand.ZiMo {
		builder.WriteRune('摸')
	}
	if hand.Last.Type != ZI {
		builder.WriteString(hand.Last.Type.String())
	}
	builder.WriteRune(hand.Last.FormatPoint())
	return builder.String()
}

func parseError(index int, message string) error {
	return errors.New(fmt.Sprintf("解析失败{%d}: %s", index, message))
}
