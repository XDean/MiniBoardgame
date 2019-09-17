package handler

import (
	"fmt"
	"github.com/jinzhu/gorm"
	"github.com/labstack/echo/v4"
	"github.com/xdean/miniboardgame/go/server/config"
	"github.com/xdean/miniboardgame/go/server/log"
	"github.com/xdean/miniboardgame/go/server/model"
	"io/ioutil"
	"os"
	"testing"
)

var echoContext *echo.Echo
var dbContext *gorm.DB

func TestMain(m *testing.M) {
	config.Debug = true
	tmp, err := ioutil.TempFile("", "mini-bg-*.db")
	if err != nil {
		panic(err)
	}
	defer os.Remove(tmp.Name())
	fmt.Println("Temp database file:", tmp.Name())
	db, err := gorm.Open("sqlite3", tmp.Name())
	if err != nil {
		panic(err)
	}
	defer db.Close()

	db, err = model.Configure(db)
	if err != nil {
		panic(err)
	}
	db.SetLogger(log.GormStdLogger{})

	dbContext = db
	echoContext = echo.New()
	echoContext.Validator = NewValidator()

	result := m.Run()

	os.Exit(result)
}