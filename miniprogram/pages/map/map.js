const { request } = require('../../utils/request');

Page({
  data: {
    latitude: 39.908823,
    longitude: 116.397470,
    markers: [],
    selectedDate: '',
    scale: 14
  },

  onLoad() {
    this.getCurrentLocation();
  },

  /** 获取当前位置 */
  getCurrentLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        this.setData({
          latitude: res.latitude,
          longitude: res.longitude
        });
      }
    });
  },

  /** 时间轴日期变化 */
  onDateChange(e) {
    this.setData({ selectedDate: e.detail.value });
    // TODO: 根据日期加载图片标记点
  },

  /** 点击标记点 */
  onMarkerTap(e) {
    const markerId = e.markerId;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${markerId}`
    });
  },

  /** 跳转上传页 */
  goUpload() {
    wx.navigateTo({ url: '/pages/upload/upload' });
  }
});
