package handler

import (
	"github.com/XDean/MiniBoardgame/model"
	"net/http"
	"strconv"
	"testing"
)

func TestGetPlayer(t *testing.T) {
	TestHttp{
		test:    t,
		handler: GetPlayer,
		response: Response{
			Body: J{
				"UserID":      USERID,
				"State":       model.HOST,
				"StateString": model.HOST.String(),
				"Seat":        0,
				"Room":        ROOM,
			},
		},
		setups: []Setup{
			WithUser(t, USER),
			WithLogin(t, USER),
			WithRoom(t, ROOM),
		},
	}.Run()

	TestHttp{
		test:    t,
		handler: GetPlayer,
		response: Response{
			Body: J{
				"UserID":      USERID,
				"State":       model.OUT_OF_GAME,
				"StateString": model.OUT_OF_GAME.String(),
				"Seat":        0,
				"Room":        nil,
			},
		},
		setups: []Setup{
			WithUser(t, USER),
			WithLogin(t, USER),
		},
	}.Run()
}

func TestGetPlayerByID(t *testing.T) {
	TestHttp{
		test:    t,
		handler: GetPlayerByID,
		request: Request{
			Params: Params{
				"id": strconv.Itoa(USERID),
			},
		},
		response: Response{
			Body: J{
				"UserID":      USERID,
				"State":       model.OUT_OF_GAME,
				"StateString": model.OUT_OF_GAME.String(),
				"Seat":        0,
				"Room":        nil,
			},
		},
		setups: []Setup{
			WithUser(t, USER),
			WithUser(t, ADMIN),
			WithLogin(t, ADMIN),
		},
	}.Run()

	TestHttp{
		test:    t,
		handler: GetPlayerByID,
		request: Request{
			Params: Params{
				"id": "wrong",
			},
		},
		response: Response{
			Code:  http.StatusBadRequest,
			Error: true,
		},
		setups: []Setup{
			WithUser(t, USER),
			WithUser(t, ADMIN),
			WithLogin(t, ADMIN),
		},
	}.Run()
}
