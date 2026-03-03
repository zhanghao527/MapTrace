const { request } = require('../../utils/request');

Page({
  data: {
    photo: {}
  },

  onLoad(options) {
    if (options.id) {
      // TODO: 根据 id 请求图片详情
    }
  },

  /** 预览大图 */
  previewImage() {
    wx.previewImage({
      current: this.data.photo.imageUrl,
      urls: [this.data.photo.imageUrl]
    });
  }
});
