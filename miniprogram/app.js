App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: '',
    userInfo: null
  },

  onLaunch() {
    this.autoLogin();
  },

  /** 自动登录 */
  autoLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      console.log('[TimeMap] 使用缓存Token登录');
      return;
    }
    console.log('[TimeMap] 无缓存Token，发起微信登录...');
    this.login()
      .then((data) => {
        console.log('[TimeMap] 登录成功', data);
      })
      .catch((err) => {
        console.error('[TimeMap] 登录失败', err);
      });
  },

  /** 微信登录 */
  login() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (!res.code) {
            reject(new Error('wx.login 失败'));
            return;
          }
          wx.request({
            url: `${this.globalData.baseUrl}/auth/login`,
            method: 'POST',
            data: { code: res.code },
            header: { 'Content-Type': 'application/json' },
            success: (resp) => {
              if (resp.data.code === 200) {
                const { token, userId } = resp.data.data;
                this.globalData.token = token;
                this.globalData.userInfo = { userId };
                wx.setStorageSync('token', token);
                resolve(resp.data.data);
              } else {
                reject(new Error(resp.data.message));
              }
            },
            fail: (err) => reject(err)
          });
        },
        fail: (err) => reject(err)
      });
    });
  }
});
