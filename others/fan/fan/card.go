package fan

import (
	"fmt"
)

const (
	// type
	TIAO CardType = iota
	BING
	WAN
	ZI
)

const (
	// zi
	Z_DONG int = iota + 100
	Z_NAN
	Z_XI
	Z_BEI
	Z_ZHONG
	Z_FA
	Z_BAI
)

var (
	ALL_ZI = []int{Z_DONG, Z_NAN, Z_XI, Z_BEI, Z_ZHONG, Z_FA, Z_BAI}
)

type (
	CardType int
	Card     struct {
		Type  CardType
		Point int
	}
	Cards      map[Card]int
	CardFilter func(Card) bool
)

func (c Card) isTBW() bool {
	return c.Type == TIAO || c.Type == BING || c.Type == WAN
}

func (c Card) isZi() bool {
	return c.Type == ZI
}

func (c Card) Copy() Card {
	return c
}

func (c Card) NextPoint() Card {
	c.Point++
	return c
}

func (c Card) Next(i int) Card {
	c.Point += i
	return c
}

func (c Card) isValid() bool {
	switch c.Type {
	case TIAO:
		fallthrough
	case BING:
		fallthrough
	case WAN:
		return c.Point > 0 && c.Point < 10
	case ZI:
		return c.Point > 0 && c.Point < 8
	default:
		return false
	}
}

func (c Card) String() string {
	if !c.isValid() {
		return fmt.Sprintf("[无效牌 %d %d]", c.Type, c.Point)
	}
	switch c.Type {
	case TIAO:
		return fmt.Sprintf("%d条", c.Point)
	case BING:
		return fmt.Sprintf("%d筒", c.Point)
	case WAN:
		return fmt.Sprintf("%d万", c.Point)
	case ZI:
		switch c.Point {
		case Z_DONG:
			return "东"
		case Z_NAN:
			return "南"
		case Z_XI:
			return "西"
		case Z_BEI:
			return "北"
		case Z_ZHONG:
			return "中"
		case Z_FA:
			return "发"
		case Z_BAI:
			return "白"
		}
	}
	panic("never happen")
}

func (c Cards) Size() int {
	i := 0
	for _, v := range c {
		i += v
	}
	return i
}

func (c Cards) Find(filter CardFilter) Cards {
	result := make(Cards)
	for card, count := range c {
		if filter(card) {
			result[card] = count
		}
	}
	return result
}

func (c Cards) Copy() Cards {
	return c.Find(func(card Card) bool {
		return true
	})
}

func (c Cards) Remove(toRemove Cards) Cards {
	result := c.Copy()
	for card, count := range toRemove {
		c[card] -= count
		if c[card] < 0 {
			panic("Can't have card less than 0")
		}
	}
	return result
}

func (c Cards) MoveTo(target Cards, card Card, count int) {
	c[card] -= count
	target[card] += count
}

func PointIs(point int) CardFilter {
	return func(card Card) bool {
		return card.Point == point
	}
}

func PointNear(point, near int) CardFilter {
	return func(card Card) bool {
		return Abs(card.Point-point) <= near
	}
}

func TypeIs(t CardType) CardFilter {
	return func(card Card) bool {
		return card.Type == t
	}
}

func CardIs(c Card) CardFilter {
	return func(card Card) bool {
		return card == c
	}
}

func (c Cards) IsValid() bool {
	for _, v := range c {
		if v < 0 {
			return false
		}
	}
	return true
}
func Abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}