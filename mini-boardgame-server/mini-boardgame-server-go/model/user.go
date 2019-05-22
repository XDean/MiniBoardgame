package model

import (
	"errors"
	"github.com/dgrijalva/jwt-go"
	"github.com/jinzhu/gorm"
	"github.com/labstack/echo/v4"
	"golang.org/x/crypto/bcrypt"
	"net/http"
	"time"
)

type User struct {
	ID       uint   `gorm:"primary_key"`
	Username string `gorm:"unique;not null"`
	Password string
	Roles    []Role
}

type Role struct {
	ID     uint `gorm:"primary_key"`
	UserID uint
	Name   string
}

type Profile struct {
	ID        uint `gorm:"primary_key"`
	UserID    uint `gorm:"unique;not null"`
	User      User
	Nickname  string
	Male      bool
	AvatarURL string
}

type Claims struct {
	jwt.StandardClaims
	User User
}

func (user *User) FindByID(db *gorm.DB, id uint) error {
	if err := db.Where("id = ?", id).Find(user).Error; err != nil {
		return err
	}
	return nil
}

func (user *User) FindByUsername(db *gorm.DB, username string) error {
	if err := db.Where("username = ?", username).Find(user).Error; err != nil {
		return err
	}
	return nil
}

func (user *User) Save(db *gorm.DB) error {
	return db.Save(user).Error
}

func (profile *Profile) Save(db *gorm.DB) error {
	return db.Save(profile).Error
}

func (user *User) CreateAccount(db *gorm.DB) error {
	if encoded, err := bcrypt.GenerateFromPassword([]byte(user.Password), 10); err == nil {
		user.Password = string(encoded)
		result := db.FirstOrCreate(user, User{Username: user.Username})
		if result.Error != nil {
			return result.Error
		} else if result.RowsAffected > 0 {
			return nil
		} else {
			return echo.NewHTTPError(http.StatusBadRequest, "Username existed")
		}
	} else {
		return err
	}
}

func (user *User) MatchPassword(pwd string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(pwd))
	return err == nil
}

func (user *User) ChangePassword(db *gorm.DB, old, new string) error {
	if user.MatchPassword(old) {
		return errors.New("Password not correct")
	}
	if newPassword, err := bcrypt.GenerateFromPassword([]byte(new), 10); err == nil {
		user.Password = string(newPassword)
		return user.Save(db)
	} else {
		return err
	}
}

func UserExistById(db *gorm.DB, id uint) (bool, error) {
	user := new(User)
	if err := user.FindByID(db, id); gorm.IsRecordNotFoundError(err) {
		return false, nil
	} else {
		return false, err
	}
	return true, nil
}

func UserExistByUsername(db *gorm.DB, username string) (bool, error) {
	user := new(User)
	if err := user.FindByUsername(db, username); gorm.IsRecordNotFoundError(err) {
		return false, nil
	} else {
		return false, err
	}
	return true, nil
}

func (user *User) GenerateToken(key string) (string, error) {
	return jwt.NewWithClaims(jwt.SigningMethodHS256, Claims{
		User: *user,
		StandardClaims: jwt.StandardClaims{
			Subject:   "access token",
			ExpiresAt: time.Now().Add(time.Hour * 24).Unix(),
		},
	}).SignedString([]byte(key))
}
