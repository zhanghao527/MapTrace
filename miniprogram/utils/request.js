const app = getApp();

let isRefreshing = false;
let pendingRequests = [];

/**
 * 封装 wx.request，自动携带 Token，401 自动重新登录
 */
function request(url, method, data) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${url}`,
      method: method || 'GET',
      data: data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : ''
      },
      success(res) {
        if (res.data.code === 200) {
          resolve(res.data);
        } else if (res.data.code === 401) {
          // Token 过期，重新登录
          handleTokenExpired(url, method, data, resolve, reject);
        } else {
          reject(res.data);
        }
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

function handleTokenExpired(url, method, data, resolve, reject) {
  if (isRefreshing) {
    // 正在刷新中，排队等待
    pendingRequests.push({ url, method, data, resolve, reject });
    return;
  }

  isRefreshing = true;
  wx.removeStorageSync('token');
  app.globalData.token = '';

  app.login()
    .then(() => {
      isRefreshing = false;
      // 重试原请求
      request(url, method, data).then(resolve).catch(reject);
      // 重试排队的请求
      pendingRequests.forEach((req) => {
        request(req.url, req.method, req.data).then(req.resolve).catch(req.reject);
      });
      pendingRequests = [];
    })
    .catch((err) => {
      isRefreshing = false;
      pendingRequests = [];
      reject(err);
    });
}

module.exports = { request };
