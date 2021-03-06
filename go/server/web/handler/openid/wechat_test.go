package openid

import (
	"github.com/jarcoal/httpmock"
	"github.com/stretchr/testify/assert"
	"github.com/xdean/miniboardgame/go/server/config"
	"testing"
)

const url = "http://wechat.com/auth"

func TestWechat(t *testing.T) {
	httpmock.Activate()
	defer httpmock.DeactivateAndReset()

	config.Instance.Wechat = config.Wechat{
		AuthUrl: url,
	}

	var openid string
	var err error

	httpmock.Reset()
	err = register(t, 0)
	assert.NoError(t, err)
	openid, err = wechatOpenIdProvider.Auth("")
	assert.NoError(t, err)
	assert.Equal(t, "openid", openid)

	httpmock.Reset()
	err = register(t, 40029)
	assert.NoError(t, err)
	openid, err = wechatOpenIdProvider.Auth("")
	assert.Error(t, err)

	httpmock.Reset()
	err = register(t, 45011)
	assert.NoError(t, err)
	openid, err = wechatOpenIdProvider.Auth("")
	assert.Error(t, err)

	httpmock.Reset()
	err = register(t, 10000000)
	assert.NoError(t, err)
	openid, err = wechatOpenIdProvider.Auth("")
	assert.Error(t, err)

	httpmock.Reset()
	err = register(t, -1)
	assert.NoError(t, err)
	openid, err = wechatOpenIdProvider.Auth("")
	assert.Error(t, err)

}

func register(t *testing.T, code int) error {
	responder, err := httpmock.NewJsonResponder(200, response(code))
	assert.NoError(t, err)
	httpmock.RegisterResponder("GET", url, responder)
	return err
}

func response(code int) map[string]interface{} {
	return map[string]interface{}{
		"openid":      "openid",
		"session_key": "session_key",
		"unionid":     "unionid",
		"errcode":     code,
		"errmsg":      "errmsg",
	}
}
