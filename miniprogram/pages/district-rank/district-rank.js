const { request } = require('../../utils/request');

Page({
  data: {
    rankList: [],
    districtCount: 0,
    totalPhotos: 0,
    loading: false,
    isEmpty: false
  },

  onLoad() {
    this.loadRanking();
  },

  loadRanking() {
    this.setData({ loading: true, isEmpty: false });
    request('/photo/district-ranking', 'GET', {
      sortBy: 'photoCount',
      limit: 50
    }).then(res => {
      const data = res.data || {};
      const list = data.list || [];
      this.setData({
        rankList: list,
        districtCount: data.districtCount || 0,
        totalPhotos: data.totalPhotos || 0,
        isEmpty: list.length === 0,
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false, isEmpty: true });
    });
  },

  /** 点击区县 → 跳转到该区县的社区页 */
  onDistrictTap(e) {
    const district = e.currentTarget.dataset.district;
    const city = e.currentTarget.dataset.city || '';
    wx.navigateTo({
      url: '/pages/community/community?district=' + encodeURIComponent(district) + '&city=' + encodeURIComponent(city)
    });
  }
});
