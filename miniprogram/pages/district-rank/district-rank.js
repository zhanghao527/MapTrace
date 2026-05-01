const { request } = require('../../utils/request');

Page({
  data: {
    sortBy: 'photoCount',
    sortOptions: [
      { value: 'photoCount', label: '照片数' },
      { value: 'createTime', label: '上传时间' },
      { value: 'photoDate', label: '拍摄时间' },
      { value: 'likeCount', label: '点赞数' },
      { value: 'commentCount', label: '评论数' }
    ],
    sortIndex: 0,
    rankList: [],
    districtCount: 0,
    totalPhotos: 0,
    loading: false,
    isEmpty: false
  },

  onLoad() {
    this.loadRanking();
  },

  onSortChange(e) {
    const idx = parseInt(e.detail.value);
    const sortBy = this.data.sortOptions[idx].value;
    if (sortBy === this.data.sortBy) return;
    this.setData({ sortIndex: idx, sortBy, rankList: [] });
    this.loadRanking();
  },

  loadRanking() {
    this.setData({ loading: true, isEmpty: false });
    request('/photo/district-ranking', 'GET', {
      sortBy: this.data.sortBy,
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

  onDistrictTap(e) {
    const district = e.currentTarget.dataset.district;
    const city = e.currentTarget.dataset.city || '';
    wx.navigateTo({
      url: '/pages/community/community?district=' + encodeURIComponent(district) + '&city=' + encodeURIComponent(city)
    });
  }
});
