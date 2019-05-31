package handler

import (
	"github.com/XDean/MiniBoardgame/model"
	"github.com/labstack/echo/v4"
	"net/http"
	"strconv"
)

func GetPlayer(c echo.Context) error {
	user, err := GetCurrentUser(c)
	MustNoError(err)

	player := new(model.Player)
	err = player.GetByUserID(GetDB(c), user.ID)
	MustNoError(err)

	return c.JSON(http.StatusOK, PlayerJson(player))
}

func GetPlayerByID(c echo.Context) error {
	idParam := c.Param("id")
	if id, err := strconv.Atoi(idParam); err == nil {
		player := new(model.Player)
		err = player.GetByUserID(GetDB(c), uint(id))
		MustNoError(err)
		return c.JSON(http.StatusOK, PlayerJson(player))
	} else {
		return echo.NewHTTPError(http.StatusBadRequest, "Unrecognized id: "+idParam)
	}
}

func PlayerJson(p *model.Player) interface{} {
	return J{
		"UserID":      p.UserID,
		"State":       p.State,
		"StateString": p.State.String(),
		"Seat":        p.Seat,
		"Room":        p.Room,
	}
}
