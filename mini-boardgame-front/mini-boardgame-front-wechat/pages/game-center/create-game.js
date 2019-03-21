// pages/index/create-game.js

import * as util from '../../utils/util.js'

const app = getApp()

Page({
  data: {
    userInfo: {},
    gameList: {},
    gameChoice: 0,
    roomName: ''
  },
  onLoad: function(options) {
    this.setData({
      userInfo: app.globalData.userInfo,
      gameList: app.globalData.gameList
    })
  },
  selectGame: function(e) {
    this.setData({
      gameChoice: e.detail.value
    })
  },
  inputRoomName: function(e) {
    this.setData({
      roomName: e.detail.value
    })
  },
  createGame: function(e) {
    var gameId = this.data.gameList[this.data.gameChoice].id
    console.log('To create game: ' + gameId)
    var config = this.selectComponent('#' + gameId + '-config').data.config
    console.log('With config: ' + config)
    util.request({
      url: 'game/room/create',
      method: 'POST',
      data: {
        'gameName': gameId,
        'roomName': this.data.roomName,
        'gameConfig': config
      }
    })
  }
})